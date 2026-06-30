package com.chatapp.chat.domain.chat

import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.Result

interface ChatParticipantService {
    suspend fun searchParticipant(query: String): Result<ChatParticipant, DataError.Remote>
}