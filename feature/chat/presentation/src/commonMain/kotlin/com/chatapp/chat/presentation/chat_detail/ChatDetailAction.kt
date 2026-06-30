package com.chatapp.chat.presentation.chat_detail

import com.chatapp.chat.presentation.model.MessageUi

sealed interface ChatDetailAction {
    data object OnSendMessageClick: ChatDetailAction
    data object OnScrollToTop: ChatDetailAction
    data class OnSelectChat(val chatId: String?): ChatDetailAction
    data class OnDeleteMessageClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data class OnMessageLongClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data object OnDismissMessageMenu: ChatDetailAction
    data class OnRetryClick(val message: MessageUi.LocalUserMessage): ChatDetailAction
    data object OnBackClick: ChatDetailAction
    data object OnChatOptionsClick: ChatDetailAction
    data object OnChatMembersClick: ChatDetailAction
    data object OnLeaveChatClick: ChatDetailAction
    data object OnDismissChatOptions: ChatDetailAction
}