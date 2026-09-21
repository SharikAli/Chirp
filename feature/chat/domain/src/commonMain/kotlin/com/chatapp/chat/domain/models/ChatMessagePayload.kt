package com.chatapp.chat.domain.models

sealed interface ChatMessagePayload {
    data class Image(
        val imageUrls: List<String>
    ) : ChatMessagePayload

    data class ParticipantsJoined(
        val joinedUserIds: List<String>
    ) : ChatMessagePayload

    data class ParticipantsRemoved(
        val removedUserIds: List<String>
    ) : ChatMessagePayload

    data class ParticipantLeft(
        val userId: String
    ) : ChatMessagePayload
}
