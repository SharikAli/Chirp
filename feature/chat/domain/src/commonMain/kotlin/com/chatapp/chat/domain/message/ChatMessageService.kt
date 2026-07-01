package com.chatapp.chat.domain.message

import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.Result

interface ChatMessageService {
    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<ChatMessage>, DataError.Remote>
}