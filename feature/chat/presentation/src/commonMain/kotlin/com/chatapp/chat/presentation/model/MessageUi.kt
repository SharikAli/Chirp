package com.chatapp.chat.presentation.model

import com.chatapp.chat.domain.models.ChatMessageDeliveryStatus
import com.chatapp.core.designsystem.components.avatar.ChatParticipantUi
import com.chatapp.core.presentation.util.UiText

sealed class MessageUi(open val id: String) {
    data class LocalUserMessage(
        override val id: String,
        val content: String,
        val deliveryStatus: ChatMessageDeliveryStatus,
        val isMenuOpen: Boolean,
        val formattedSentTime: UiText
    ): MessageUi(id)

    data class OtherUserMessage(
        override val id: String,
        val content: String,
        val formattedSentTime: UiText,
        val sender: ChatParticipantUi
    ): MessageUi(id)

    data class DateSeparator(
        override val id: String,
        val date: UiText,
    ): MessageUi(id)
}