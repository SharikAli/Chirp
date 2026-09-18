package com.chatapp.chat.presentation.model

import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.core.designsystem.components.avatar.ChatParticipantUi

data class ChatUi(
    val id: String,
    val localParticipant: ChatParticipantUi,
    val otherParticipants: List<ChatParticipantUi>,
    val lastMessage: ChatMessage?,
    val lastMessageSenderUsername: String?,
    val creatorId: String = ""
) {
    val isLocalParticipantAdmin: Boolean
        get() = creatorId == localParticipant.id
}