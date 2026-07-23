package com.chatapp.chat.data.mappers

import com.chatapp.chat.data.dto.response.UserSettingResponse
import com.chatapp.chat.domain.models.UserSetting

fun UserSettingResponse.toUserSetting(): UserSetting {
    return UserSetting(
        userId = userId,
        typingIndicatorEnabled = typingIndicatorEnabled
    )
}