package com.chatapp.chat.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatMessageDto(
    val id: String,
    val chatId: String,
    val content: String,
    val type: String = "TEXT",
    val payload: JsonElement? = null,
    val createdAt: String,
    val senderId: String
)