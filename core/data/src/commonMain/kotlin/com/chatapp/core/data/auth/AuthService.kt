package com.chatapp.core.data.auth

import com.chatapp.core.data.dto.requests.RegisterRequest
import com.chatapp.core.data.network.post
import com.chatapp.core.domain.auth.AuthService
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorAuthService(
    private val httpClient: HttpClient
) : AuthService {

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/register",
            body = RegisterRequest(
                email = email,
                username = username,
                password = password
            )
        )
    }
}