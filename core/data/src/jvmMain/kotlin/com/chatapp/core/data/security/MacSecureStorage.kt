package com.chatapp.core.data.security

import java.util.Base64

/**
 * Protects the AES master key in the macOS Keychain via the `security` command-line tool,
 * which is bundled with every macOS install.
 */
class MacKeyProtector(
    private val service: String = "com.chatapp.chirp.securestorage",
    private val account: String = "chirp_master_key"
) : KeyProtector {

    override fun loadKey(): ByteArray? {
        val output = runSecurity(
            "find-generic-password",
            "-a", account,
            "-s", service,
            "-w"
        ) ?: return null
        return Base64.getDecoder().decode(output.trim())
    }

    override fun storeKey(key: ByteArray) {
        runSecurity(
            "delete-generic-password",
            "-a", account,
            "-s", service
        )
        runSecurity(
            "add-generic-password",
            "-a", account,
            "-s", service,
            "-w", Base64.getEncoder().encodeToString(key),
            "-U"
        )
    }

    private fun runSecurity(vararg args: String): String? {
        val process = ProcessBuilder("/usr/bin/security", *args)
            .redirectErrorStream(false)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        return if (exitCode == 0) output else null
    }
}
