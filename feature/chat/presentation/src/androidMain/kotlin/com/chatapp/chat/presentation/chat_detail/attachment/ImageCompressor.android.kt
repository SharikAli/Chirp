package com.chatapp.chat.presentation.chat_detail.attachment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min
import androidx.core.graphics.scale

actual suspend fun compressImage(
    bytes: ByteArray,
    maxDimension: Int,
    quality: Int
): ByteArray = withContext(Dispatchers.Default) {
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: return@withContext bytes

    val scale = min(
        1f,
        maxDimension.toFloat() / maxOf(original.width, original.height)
    )

    val scaled = if (scale < 1f) {
        original.scale(
            (original.width * scale).toInt().coerceAtLeast(1),
            (original.height * scale).toInt().coerceAtLeast(1)
        )
    } else original

    ByteArrayOutputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        output.toByteArray()
    }
}
