package com.chatapp.chat.domain.message

import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.chat.domain.models.ProfilePictureUploadUrls
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import com.chatapp.core.domain.util.Result

interface ChatMessageService {
    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<ChatMessage>, DataError.Remote>

    suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote>

    // Plain REST send, as opposed to the WebSocket-based send used by MessageRepository.sendMessage.
    // Used where a live socket connection can't be assumed, e.g. sending a reply from a
    // notification action while the app isn't open.
    suspend fun sendMessage(
        chatId: String,
        content: String,
        messageId: String? = null,
        imageUrls: List<String>? = null
    ): Result<ChatMessage, DataError.Remote>

    // Same signed-URL pattern as ChatParticipantService's profile picture upload - the
    // client uploads directly to storage, then includes the returned publicUrl in sendMessage.
    suspend fun getImageUploadUrl(
        chatId: String,
        mimeType: String
    ): Result<ProfilePictureUploadUrls, DataError.Remote>

    suspend fun uploadImage(
        uploadUrl: String,
        imageBytes: ByteArray,
        headers: Map<String, String>
    ): EmptyResult<DataError.Remote>

    suspend fun downloadImage(url: String): Result<ByteArray, DataError.Remote>
}