package com.chatapp.chat.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UserSettingRequest(
    val typingIndicatorEnabled: Boolean
)
