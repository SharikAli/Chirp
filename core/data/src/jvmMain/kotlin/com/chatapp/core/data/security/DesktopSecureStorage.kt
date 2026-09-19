package com.chatapp.core.data.security

import com.chatapp.core.data.util.DesktopOs
import com.chatapp.core.data.util.currentOs
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Protects a random AES-256 master key using the desktop OS's native secure storage
 * (Windows DPAPI, macOS Keychain, or the Linux Secret Service), then performs the actual
 * AES-GCM encryption of values with that key so plaintext never touches disk unprotected.
 */
class DesktopSecureStorage private constructor(
    private val keyProtector: KeyProtector
) : SecureStorage {

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH_BITS = 128
    }

    constructor() : this(
        when (currentOs) {
            DesktopOs.WINDOWS -> WindowsKeyProtector()
            DesktopOs.MACOS -> MacKeyProtector()
            DesktopOs.LINUX -> LinuxKeyProtector()
        }
    )

    private val secretKey: SecretKeySpec by lazy {
        val keyBytes = keyProtector.loadKey() ?: generateKey().also { keyProtector.storeKey(it) }
        SecretKeySpec(keyBytes, "AES")
    }

    override fun encrypt(plainText: String): String {
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        }
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + encrypted)
    }

    override fun decrypt(cipherText: String): String? {
        return runCatching {
            val payload = Base64.getDecoder().decode(cipherText)
            val iv = payload.copyOfRange(0, GCM_IV_LENGTH)
            val encrypted = payload.copyOfRange(GCM_IV_LENGTH, payload.size)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrNull()
    }

    private fun generateKey(): ByteArray = ByteArray(32).also { SecureRandom().nextBytes(it) }
}