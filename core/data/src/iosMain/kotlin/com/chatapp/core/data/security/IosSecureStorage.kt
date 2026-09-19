@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.chatapp.core.data.security

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCKeySizeAES256
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.getBytes
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecRandomDefault
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Backed by the iOS Keychain (Security framework), which stores a random AES-256 master key.
 * Encryption itself is performed with CommonCrypto (AES-CBC), so tokens are encrypted before
 * they ever reach disk, and the key material never leaves the Keychain's secure storage.
 */
class IosSecureStorage(
    private val service: String = "com.chatapp.chirp.securestorage"
) : SecureStorage {

    private companion object {
        const val BLOCK_SIZE = 16
    }

    private val keyAccount = "chirp_master_key"

    private val masterKey: ByteArray by lazy { loadOrCreateMasterKey() }

    override fun encrypt(plainText: String): String {
        val iv = randomBytes(BLOCK_SIZE)
        val plainBytes = plainText.encodeToByteArray()
        val encrypted = crypt(kCCEncrypt, plainBytes, iv)
        val payload = iv + encrypted
        return payload.toNSData().base64EncodedStringWithOptions(0u)
    }

    override fun decrypt(cipherText: String): String? {
        return runCatching {
            val payload = cipherText.base64ToByteArray()
            val iv = payload.copyOfRange(0, BLOCK_SIZE)
            val encrypted = payload.copyOfRange(BLOCK_SIZE, payload.size)
            crypt(kCCDecrypt, encrypted, iv).decodeToString()
        }.getOrNull()
    }

    private fun crypt(operation: UInt, input: ByteArray, iv: ByteArray): ByteArray {
        val outputSize = input.size + BLOCK_SIZE
        val output = ByteArray(outputSize)
        var moved = 0uL

        memScoped {
            val movedVar = alloc<platform.posix.size_tVar>()
            input.usePinned { inputPinned ->
                iv.usePinned { ivPinned ->
                    masterKey.usePinned { keyPinned ->
                        output.usePinned { outputPinned ->
                            val status = CCCrypt(
                                operation,
                                kCCAlgorithmAES,
                                kCCOptionPKCS7Padding,
                                keyPinned.addressOf(0),
                                kCCKeySizeAES256.toULong(),
                                ivPinned.addressOf(0),
                                inputPinned.addressOf(0),
                                input.size.toULong(),
                                outputPinned.addressOf(0),
                                outputSize.toULong(),
                                movedVar.ptr
                            )
                            check(status == kCCSuccess) { "CCCrypt failed with status $status" }
                        }
                    }
                }
            }
            moved = movedVar.value
        }

        return output.copyOfRange(0, moved.toInt())
    }

    private fun randomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            check(SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0)) == 0) {
                "Failed to generate random bytes"
            }
        }
        return bytes
    }

    private fun loadOrCreateMasterKey(): ByteArray {
        readKeyFromKeychain()?.let { return it }
        val newKey = randomBytes(kCCKeySizeAES256.toInt())
        writeKeyToKeychain(newKey)
        return newKey
    }

    private fun readKeyFromKeychain(): ByteArray? {
        val query = baseQuery()
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        return memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            CFBridgingRelease(query)
            if (status != errSecSuccess) return@memScoped null
            val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
            data.toByteArray()
        }
    }

    private fun writeKeyToKeychain(key: ByteArray) {
        SecItemDelete(baseQuery())
        val query = baseQuery()
        CFDictionaryAddValue(query, kSecValueData, CFBridgingRetain(key.toNSData()))
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
        val status = SecItemAdd(query, null)
        CFBridgingRelease(query)
        check(status == errSecSuccess) { "Keychain write failed with status $status" }
    }

    private fun baseQuery(): CFMutableDictionaryRef? {
        val dictionary = CFDictionaryCreateMutable(
            null,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionaryAddValue(dictionary, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(dictionary, kSecAttrService, CFBridgingRetain(service))
        CFDictionaryAddValue(dictionary, kSecAttrAccount, CFBridgingRetain(keyAccount))
        return dictionary
    }

    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.dataWithBytes(it.addressOf(0), size.toULong())
    }

    private fun NSData.toByteArray(): ByteArray {
        val bytes = ByteArray(length.toInt())
        bytes.usePinned {
            getBytes(it.addressOf(0), length)
        }
        return bytes
    }

    private fun String.base64ToByteArray(): ByteArray {
        val data = requireNotNull(
            NSData.create(base64EncodedString = this, options = 0u)
        ) { "Invalid base64 payload" }
        return data.toByteArray()
    }
}
