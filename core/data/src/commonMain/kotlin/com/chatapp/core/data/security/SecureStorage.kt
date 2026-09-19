package com.chatapp.core.data.security

/**
 * Encrypts/decrypts values using a key managed by each platform's built-in secure storage
 * (Android Keystore, iOS Keychain, Windows DPAPI, macOS Keychain, Linux Secret Service).
 * The resulting ciphertext is safe to persist in ordinary, non-secure storage (e.g. DataStore).
 */
interface SecureStorage {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String?
}
