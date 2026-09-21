package com.chatapp.chat.presentation.chat_detail.attachment

/** Saves [bytes] to the platform's shared photo storage (gallery/Photos/Pictures). */
expect suspend fun saveImageToDevice(bytes: ByteArray, fileName: String): Boolean
