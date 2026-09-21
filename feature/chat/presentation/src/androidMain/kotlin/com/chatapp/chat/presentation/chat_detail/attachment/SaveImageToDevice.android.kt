package com.chatapp.chat.presentation.chat_detail.attachment

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual suspend fun saveImageToDevice(bytes: ByteArray, fileName: String): Boolean {
    return withContext(Dispatchers.IO) {
        val context = AndroidSaveImageComponent.context

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Chirp")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = context.contentResolver.insert(collection, values) ?: return@withContext false

        try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: return@withContext false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
            true
        } catch (_: Exception) {
            context.contentResolver.delete(uri, null, null)
            false
        }
    }
}

private object AndroidSaveImageComponent : KoinComponent {
    val context: Context get() = get()
}
