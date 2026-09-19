package com.chatapp.chat.domain.message

import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.chat.domain.models.ChatMessageDeliveryStatus
import com.chatapp.chat.domain.models.MessageWithSender
import com.chatapp.chat.domain.models.OutgoingNewMessage
import com.chatapp.chat.domain.models.OutgoingUserTyping
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

    // Sends over plain REST instead of the WebSocket connection sendMessage() uses. For
    // callers that can't assume a live socket - e.g. a notification reply action's
    // background handler, which never opens the app UI.
    suspend fun sendMessageViaRest(
        chatId: String,
        content: String,
        messageId: String
    ): EmptyResult<DataError>

    suspend fun retryMessage(messageId: String): EmptyResult<DataError>

    suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote>

    fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>>

    suspend fun sendTypingIndicator(message: OutgoingUserTyping): EmptyResult<DataError>
}