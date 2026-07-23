package com.chatapp.chat.domain.models

data class OutgoingUserTyping(
    val chatId: String,
    val isTyping: Boolean
)