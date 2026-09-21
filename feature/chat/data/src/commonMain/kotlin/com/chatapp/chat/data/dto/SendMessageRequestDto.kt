package com.chatapp.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequestDto(
    val content: String,
    val messageId: String? = null,
    val imageUrls: List<String>? = null
)
