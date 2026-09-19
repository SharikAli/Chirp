package com.chatapp.core.data.security

import com.chatapp.core.data.util.appDataDirectory
import java.io.File
import java.util.Base64

/**
 * Protects the AES master key with the Windows Data Protection API (DPAPI), invoked via
 * PowerShell's `System.Security.Cryptography.ProtectedData`, scoping it to the current
 * Windows user account.
 */
class WindowsKeyProtector : KeyProtector {

    private val keyFile: File by lazy {
        File(appDataDirectory, "secure").apply { mkdirs() }.resolve("master.key")
    }

    override fun loadKey(): ByteArray? {
        if (!keyFile.exists()) return null
        val protectedBase64 = keyFile.readText().trim()
        val plainBase64 = runProtect(protectedBase64, unprotect = true) ?: return null
        return Base64.getDecoder().decode(plainBase64)
    }

    override fun storeKey(key: ByteArray) {
        val plainBase64 = Base64.getEncoder().encodeToString(key)
        val protectedBase64 = requireNotNull(runProtect(plainBase64, unprotect = false)) {
            "DPAPI protect failed for master key"
        }
        keyFile.writeText(protectedBase64)
    }

    private fun runProtect(inputBase64: String, unprotect: Boolean): String? {
        val method = if (unprotect) "Unprotect" else "Protect"
        val script = $$"""
            $bytes = [Convert]::FromBase64String('$$inputBase64')
            $result = [System.Security.Cryptography.ProtectedData]::$$method($bytes, $null, [System.Security.Cryptography.DataProtectionScope]::CurrentUser)
            [Convert]::ToBase64String($result)
        """.trimIndent()

        val process = ProcessBuilder(
            "powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script
        ).start()
        val output = process.inputStream.bufferedReader().readText().trim()
        return if (process.waitFor() == 0) output else null
    }
}
