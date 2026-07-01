package com.chatapp.chat.domain.message

import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.chat.domain.models.ChatMessageDeliveryStatus
import com.chatapp.chat.domain.models.MessageWithSender
import com.chatapp.chat.domain.models.OutgoingNewMessage
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import com.chatapp.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local>

    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<ChatMessage>, DataError>

    suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError>

    fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>>
}