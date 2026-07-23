package com.chatapp.chat.data.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingResponse(
    val userId: String,
    val typingIndicatorEnabled: Boolean
)
