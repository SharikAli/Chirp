package com.chatapp.chat.domain.models

data class UserSetting(
    val userId: String,
    val typingIndicatorEnabled: Boolean
)