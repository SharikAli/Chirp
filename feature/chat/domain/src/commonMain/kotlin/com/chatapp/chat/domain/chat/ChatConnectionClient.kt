package com.chatapp.chat.domain.chat

import com.chatapp.chat.domain.error.ConnectionError
import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.chat.domain.models.ConnectionState
import com.chatapp.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatConnectionClient {
    val chatMessages: Flow<ChatMessage>
    val connectionState: StateFlow<ConnectionState>
    suspend fun sendChatMessage(message: ChatMessage): EmptyResult<ConnectionError>
}