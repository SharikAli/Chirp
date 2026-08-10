package com.chatapp.chat.data.user_setting

import com.chatapp.chat.data.dto.request.UserSettingRequest
import com.chatapp.chat.data.dto.response.UserSettingResponse
import com.chatapp.chat.data.mappers.toUserSetting
import com.chatapp.chat.domain.models.UserSetting
import com.chatapp.chat.domain.user_setting.UserSettingService
import com.chatapp.core.data.network.get
import com.chatapp.core.data.network.post
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import com.chatapp.core.domain.util.Result
import com.chatapp.core.domain.util.map
import io.ktor.client.HttpClient

class KtorUserSettingService(
    private val httpClient: HttpClient
) : UserSettingService {

    override suspend fun changeTypingIndicatorState(
        typingIndicatorEnabled: Boolean
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/user-setting",
            body = UserSettingRequest(
                typingIndicatorEnabled = typingIndicatorEnabled
            )
        )
    }

    override suspend fun fetchUserSetting(): Result<UserSetting, DataError.Remote> {
        return httpClient.get<UserSettingResponse>(
            route = "/user-setting"
        ).map { it.toUserSetting() }
    }
}