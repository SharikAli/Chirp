package com.chatapp.chat.domain.models

data class UserTypingEvent(
    val chatId: String,
    val userId: String,
    val isTyping: Boolean,
)
