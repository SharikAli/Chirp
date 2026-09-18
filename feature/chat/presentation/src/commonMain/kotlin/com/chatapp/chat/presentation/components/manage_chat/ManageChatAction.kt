package com.chatapp.chat.presentation.components.manage_chat

import com.chatapp.core.designsystem.components.avatar.ChatParticipantUi

sealed interface ManageChatAction {
    data object OnAddClick: ManageChatAction
    data object OnDismissDialog: ManageChatAction
    data object OnPrimaryActionClick: ManageChatAction
    data class OnRemoveParticipantClick(val participant: ChatParticipantUi): ManageChatAction
    data object OnConfirmRemoveParticipant: ManageChatAction
    data object OnDismissRemoveParticipantDialog: ManageChatAction

    sealed interface ChatParticipants: ManageChatAction {
        data class OnSelectChat(val chatId: String?): ManageChatAction
    }
}