package com.chatapp.chat.data.mappers

import com.chatapp.chat.data.dto.ChatParticipantDto
import com.chatapp.chat.domain.models.ChatParticipant

fun ChatParticipantDto.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}