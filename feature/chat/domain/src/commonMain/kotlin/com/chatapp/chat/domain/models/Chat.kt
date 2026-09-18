package com.chatapp.chat.domain.models

import kotlin.time.Instant

data class Chat(
    val id: String,
    val participants: List<ChatParticipant>,
    val lastActivityAt: Instant,
    val lastMessage: ChatMessage?,
    val creatorId: String,
    val lastMessageSenderUsername: String? = null
)