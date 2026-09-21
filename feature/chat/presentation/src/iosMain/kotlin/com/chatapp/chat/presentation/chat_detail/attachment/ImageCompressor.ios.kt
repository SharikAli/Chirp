@file:OptIn(ExperimentalForeignApi::class)

package com.chatapp.chat.presentation.chat_detail.attachment

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.math.min

actual suspend fun compressImage(
    bytes: ByteArray,
    maxDimension: Int,
    quality: Int
): ByteArray = withContext(Dispatchers.Default) {
    val data = bytes.toNSData()
    val original = UIImage(data = data)

    val originalSize = original.size
    val scale = min(
        1.0,
        maxDimension.toDouble() / maxOf(
            originalSize.useContents { width },
            originalSize.useContents { height })
    )

    val jpegData = if (scale < 1.0) {
        val targetSize = originalSize.useContents {
            CGSizeMake(width * scale, height * scale)
        }

        UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
        original.drawInRect(
            platform.CoreGraphics.CGRectMake(
                0.0,
                0.0,
                targetSize.useContents { width },
                targetSize.useContents { height })
        )
        val resized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        UIImageJPEGRepresentation(resized ?: original, quality / 100.0)
    } else {
        UIImageJPEGRepresentation(original, quality / 100.0)
    }

    jpegData?.toByteArray() ?: bytes
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return bytes
}
