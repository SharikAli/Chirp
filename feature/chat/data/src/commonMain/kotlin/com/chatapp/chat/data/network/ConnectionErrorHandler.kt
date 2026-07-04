package com.chatapp.chat.data.network

import com.chatapp.chat.domain.models.ConnectionState

expect class ConnectionErrorHandler {
    fun getConnectionStateForError(cause: Throwable): ConnectionState
    fun transformException(exception: Throwable): Throwable
    fun isRetriableError(cause: Throwable): Boolean
}