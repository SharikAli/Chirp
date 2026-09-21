package com.chatapp.chat.presentation.chat_detail.attachment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.min

actual suspend fun compressImage(
    bytes: ByteArray,
    maxDimension: Int,
    quality: Int
): ByteArray = withContext(Dispatchers.IO) {
    val original = ImageIO.read(ByteArrayInputStream(bytes)) ?: return@withContext bytes

    val scale = min(1f, maxDimension.toFloat() / maxOf(original.width, original.height))
    val targetWidth = (original.width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (original.height * scale).toInt().coerceAtLeast(1)

    val scaled = if (scale < 1f) {
        val resized = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        resized.createGraphics().apply {
            drawImage(original, 0, 0, targetWidth, targetHeight, null)
            dispose()
        }
        resized
    } else original

    ByteArrayOutputStream().use { output ->
        val writer = ImageIO.getImageWritersByFormatName("jpg").next()
        val params = writer.defaultWriteParam.apply {
            compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality / 100f
        }
        ImageIO.createImageOutputStream(output).use { imageOutputStream ->
            writer.output = imageOutputStream
            writer.write(null, javax.imageio.IIOImage(scaled, null, null), params)
        }
        writer.dispose()
        output.toByteArray()
    }
}
