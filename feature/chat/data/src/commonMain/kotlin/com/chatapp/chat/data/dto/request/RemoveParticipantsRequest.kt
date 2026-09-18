package com.chatapp.chat.data.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class RemoveParticipantsRequest(
    val chatId: String,
    val userIds: List<String>
)
