package com.chatapp.chat.presentation.mappers

import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.core.designsystem.components.avatar.ChatParticipantUi

fun ChatParticipant.toUi(): ChatParticipantUi {
    return ChatParticipantUi(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )
}