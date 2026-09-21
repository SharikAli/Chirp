package com.chatapp.chat.presentation.chat_detail

import com.chatapp.chat.presentation.model.MessageUi
import com.chatapp.chat.presentation.profile.mediapicker.PickedImageData

sealed interface ChatDetailAction {
    data class OnImagesPicked(val images: List<PickedImageData>): ChatDetailAction
    data class OnRemoveAttachment(val id: String): ChatDetailAction
    data class OnImageClick(val imageUrls: List<String>, val index: Int): ChatDetailAction
    data object OnDismissImageViewer: ChatDetailAction
    data class OnSaveImageClick(val url: String): ChatDetailAction
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
    data object OnConfirmLeaveChat: ChatDetailAction
    data object OnDismissLeaveChatDialog: ChatDetailAction
    data object OnDismissChatOptions: ChatDetailAction
    data object OnRetryPaginationClick: ChatDetailAction
    data object OnHideBanner: ChatDetailAction
    data class OnFirstVisibleIndexChanged(val index: Int): ChatDetailAction
    data class OnTopVisibleIndexChanged(val topVisibleIndex: Int): ChatDetailAction
}