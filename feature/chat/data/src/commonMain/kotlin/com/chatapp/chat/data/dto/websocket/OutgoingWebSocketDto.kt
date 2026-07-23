package com.chatapp.chat.data.dto.websocket

import kotlinx.serialization.Serializable

enum class OutgoingWebSocketType {
    NEW_MESSAGE,
    TYPING
}

@Serializable
sealed class OutgoingWebSocketDto(
    val type: OutgoingWebSocketType
) {

    @Serializable
    data class NewMessage(
        val chatId: String,
        val messageId: String,
        val content: String
    ): OutgoingWebSocketDto(OutgoingWebSocketType.NEW_MESSAGE)

    @Serializable
    data class UserTyping(
        val chatId: String,
        val isTyping: Boolean
    ): OutgoingWebSocketDto(OutgoingWebSocketType.TYPING)
}