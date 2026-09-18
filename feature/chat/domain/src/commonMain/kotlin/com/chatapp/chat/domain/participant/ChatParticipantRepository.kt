package com.chatapp.chat.domain.participant

import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import com.chatapp.core.domain.util.Result

interface ChatParticipantRepository {
    suspend fun fetchLocalParticipant(): Result<ChatParticipant, DataError>
    suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        mimeType: String
    ): EmptyResult<DataError.Remote>

    suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote>

    suspend fun removeParticipants(
        chatId: String,
        userIds: List<String>
    ): EmptyResult<DataError.Remote>
}