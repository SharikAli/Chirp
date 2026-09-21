package com.chatapp.chat.presentation.chat_detail.attachment

/**
 * Downscales and re-encodes [bytes] as JPEG so chat image attachments stay small over the
 * wire. [maxDimension] bounds the longer side in pixels; [quality] is the JPEG quality (0-100).
 */
expect suspend fun compressImage(
    bytes: ByteArray,
    maxDimension: Int = 1920,
    quality: Int = 80
): ByteArray
