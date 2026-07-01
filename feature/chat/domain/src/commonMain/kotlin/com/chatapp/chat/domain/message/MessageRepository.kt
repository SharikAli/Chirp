package com.chatapp.chat.domain.message

import com.chatapp.chat.domain.models.ChatMessageDeliveryStatus
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult

interface MessageRepository {
    suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local>
}