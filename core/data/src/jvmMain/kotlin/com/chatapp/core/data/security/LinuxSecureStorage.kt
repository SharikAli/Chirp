package com.chatapp.core.data.security

import java.util.Base64

/**
 * Protects the AES master key using the freedesktop Secret Service API via `secret-tool`
 * (the lib-secret CLI shipped by GNOME Keyring / KWallet's Secret Service implementation on
 * most Linux desktops).
 */
class LinuxKeyProtector(
    private val service: String = "com.chatapp.chirp.securestorage",
    private val account: String = "chirp_master_key"
) : KeyProtector {

    override fun loadKey(): ByteArray? {
        val process = ProcessBuilder(
            "secret-tool", "lookup",
            "service", service,
            "account", account
        ).start()
        val output = process.inputStream.bufferedReader().readText()
        if (process.waitFor() != 0) return null
        return Base64.getDecoder().decode(output.trim())
    }

    override fun storeKey(key: ByteArray) {
        val process = ProcessBuilder(
            "secret-tool", "store",
            "--label=Chirp secure storage master key",
            "service", service,
            "account", account
        ).start()
        process.outputStream.bufferedWriter().use { it.write(Base64.getEncoder().encodeToString(key)) }
        check(process.waitFor() == 0) { "secret-tool store failed for master key" }
    }
}
