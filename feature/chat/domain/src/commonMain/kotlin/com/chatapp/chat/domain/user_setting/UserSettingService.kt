package com.chatapp.chat.domain.user_setting

import com.chatapp.chat.domain.models.UserSetting
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import com.chatapp.core.domain.util.Result

interface UserSettingService {
    suspend fun changeTypingIndicatorState(
        typingIndicatorEnabled: Boolean
    ): EmptyResult<DataError.Remote>

    suspend fun fetchUserSetting(): Result<UserSetting, DataError.Remote>
}