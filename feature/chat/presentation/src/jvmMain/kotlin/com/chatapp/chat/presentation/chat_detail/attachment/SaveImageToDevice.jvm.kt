package com.chatapp.chat.presentation.chat_detail.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual suspend fun saveImageToDevice(bytes: ByteArray, fileName: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val picturesDir = File(System.getProperty("user.home"), "Pictures/Chirp")
            picturesDir.mkdirs()
            File(picturesDir, fileName).writeBytes(bytes)
            true
        } catch (_: Exception) {
            false
        }
    }
}
