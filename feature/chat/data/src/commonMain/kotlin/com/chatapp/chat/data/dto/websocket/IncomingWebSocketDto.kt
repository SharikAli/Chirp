package com.chatapp.chat.data.dto.websocket

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

enum class IncomingWebSocketType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED,
    CHAT_PARTICIPANTS_REMOVED,
    CHAT_DELETED,
    USER_TYPING
}

@Serializable
sealed interface IncomingWebSocketDto {

    @Serializable
    data class NewMessageDto(
        val id: String,
        val chatId: String,
        val content: String,
        val senderId: String,
        val createdAt: String,
        val type: String = "TEXT",
        val payload: JsonElement? = null
    ): IncomingWebSocketDto

    @Serializable
    data class MessageDeletedDto(
        val messageId: String,
        val chatId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.MESSAGE_DELETED
    ): IncomingWebSocketDto

    @Serializable
    data class ProfilePictureUpdated(
        val userId: String,
        val newUrl: String?,
        val type: IncomingWebSocketType = IncomingWebSocketType.PROFILE_PICTURE_UPDATED
    ): IncomingWebSocketDto

    @Serializable
    data class ChatParticipantsChangedDto(
        val chatId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.CHAT_PARTICIPANTS_CHANGED
    ): IncomingWebSocketDto

    @Serializable
    data class ChatParticipantsRemovedDto(
        val chatId: String,
        val removedBy: String,
        val removedUserIds: Set<String>,
        val type: IncomingWebSocketType = IncomingWebSocketType.CHAT_PARTICIPANTS_REMOVED
    ): IncomingWebSocketDto

    @Serializable
    data class ChatDeletedDto(
        val chatId: String,
        val type: IncomingWebSocketType = IncomingWebSocketType.CHAT_DELETED
    ): IncomingWebSocketDto

    @Serializable
    data class UserTypingDto(
        val chatId: String,
        val userId: String,
        val isTyping: Boolean,
        val type: IncomingWebSocketType = IncomingWebSocketType.USER_TYPING
    ): IncomingWebSocketDto
}