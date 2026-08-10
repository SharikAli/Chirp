package com.chatapp.chat.data.mappers

import com.chatapp.chat.data.dto.websocket.IncomingWebSocketDto
import com.chatapp.chat.domain.models.UserTypingEvent

fun IncomingWebSocketDto.UserTypingDto.toUserTyping(): UserTypingEvent {
    return UserTypingEvent(
        chatId = chatId,
        userId = userId,
        isTyping = isTyping
    )
}