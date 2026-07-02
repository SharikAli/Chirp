package com.chatapp.chat.domain.notification

import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult

interface DeviceTokenService {

    suspend fun registerToken(
        token: String,
        platform: String
    ): EmptyResult<DataError.Remote>

    suspend fun unregisterToken(
        token: String
    ): EmptyResult<DataError.Remote>
}