package com.chatapp.core.data.security

/**
 * Stores/retrieves the raw master-key bytes using an OS-native secure-storage mechanism.
 */
internal interface KeyProtector {
    fun loadKey(): ByteArray?
    fun storeKey(key: ByteArray)
}