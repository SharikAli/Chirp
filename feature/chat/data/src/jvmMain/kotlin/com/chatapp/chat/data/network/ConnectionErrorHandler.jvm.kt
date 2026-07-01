package com.chatapp.chat.data.network

import com.chatapp.chat.domain.models.ConnectionState

actual class ConnectionErrorHandler {
    actual fun getConnectionStateForError(cause: Throwable): ConnectionState {
        return ConnectionState.CONNECTED
    }

    actual fun transformException(exception: Throwable): Throwable {
        return Throwable("No implementation in JVM ConnectionErrorHandler")
    }

    actual fun isRetriableError(cause: Throwable): Boolean {
        return false
    }
}