package com.chatapp.chat.presentation.create_chat

import com.chatapp.chat.domain.models.Chat

sealed interface CreateChatEvent {
    data class OnChatCreated(val chat: Chat): CreateChatEvent
}