package com.chatapp.chat.domain.chat

import com.chatapp.chat.domain.models.Chat
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.Result

interface ChatService {
    suspend fun createChat(
        otherUserIds: List<String>
    ): Result<Chat, DataError.Remote>
}