package com.chatapp.core.domain.auth

import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult

interface AuthService {
    suspend fun register(
        email: String,
        username: String,
        password: String
    ): EmptyResult<DataError.Remote>
}