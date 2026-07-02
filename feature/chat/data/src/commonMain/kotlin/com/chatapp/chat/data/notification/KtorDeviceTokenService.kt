package com.chatapp.chat.data.notification

import com.chatapp.chat.data.dto.request.RegisterDeviceTokenRequest
import com.chatapp.chat.domain.notification.DeviceTokenService
import com.chatapp.core.data.network.delete
import com.chatapp.core.data.network.post
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorDeviceTokenService(
    private val httpClient: HttpClient
) : DeviceTokenService {

    override suspend fun registerToken(
        token: String,
        platform: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/notification/register",
            body = RegisterDeviceTokenRequest(
                token = token,
                platform = platform
            )
        )
    }

    override suspend fun unregisterToken(token: String): EmptyResult<DataError.Remote> {
        return httpClient.delete(
            route = "/notification/$token"
        )
    }
}