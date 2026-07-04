package com.chatapp.chat.domain.error

import com.chatapp.core.domain.util.Error

enum class ConnectionError : Error {
    NOT_CONNECTED,
    MESSAGE_SEND_FAILED
}