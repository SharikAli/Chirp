package com.chatapp.chat.presentation.chat_detail.model

import com.chatapp.chat.domain.models.ChatInfo
import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.chat.presentation.chat_detail.ChatDetailState
import com.chatapp.core.domain.auth.AuthInfo

data class StateWithMessagesInput(
    val currentState: ChatDetailState,
    val chatInfo: ChatInfo,
    val authInfo: AuthInfo?,
    val typingUsers: Map<String, Long>,
    val chatParticipants: List<ChatParticipant>
)