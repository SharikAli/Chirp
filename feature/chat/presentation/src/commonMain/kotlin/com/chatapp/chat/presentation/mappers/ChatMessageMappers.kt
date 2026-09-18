package com.chatapp.chat.presentation.mappers

import com.chatapp.chat.domain.models.ChatMessagePayload
import com.chatapp.chat.domain.models.ChatMessageType
import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.chat.domain.models.MessageWithSender
import com.chatapp.chat.presentation.model.MessageUi
import com.chatapp.chat.presentation.util.DateUtils
import com.chatapp.core.designsystem.components.avatar.ChatParticipantUi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun List<MessageWithSender>.toUiList(
    localUserId: String,
    knownParticipants: List<ChatParticipant> = emptyList()
): List<MessageUi> {
    return this
        .sortedByDescending { it.message.createdAt }
        .groupBy {
            it.message.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        .flatMap { (date, messages) ->
            messages.map { it.toUi(localUserId, knownParticipants) } + MessageUi.DateSeparator(
                id = date.toString(),
                date = DateUtils.formatDateSeparator(date)
            )
        }
}

fun MessageWithSender.toUi(
    localUserId: String,
    knownParticipants: List<ChatParticipant> = emptyList()
): MessageUi {
    val systemMessage = toSystemMessageUiOrNull(knownParticipants)
    if (systemMessage != null) {
        return systemMessage
    }

    val isFromLocalUser = this.sender.userId == localUserId
    return if (isFromLocalUser) {
        MessageUi.LocalUserMessage(
            id = message.id,
            content = message.content,
            deliveryStatus = message.deliveryStatus,
            formattedSentTime = DateUtils.formatMessageTime(instant = message.createdAt)
        )
    } else {
        MessageUi.OtherUserMessage(
            id = message.id,
            content = message.content,
            formattedSentTime = DateUtils.formatMessageTime(instant = message.createdAt),
            sender = sender.toUi()
        )
    }
}

private fun MessageWithSender.toSystemMessageUiOrNull(
    knownParticipants: List<ChatParticipant>
): MessageUi? {
    val participantsById = (knownParticipants + sender).associateBy { it.userId }

    fun resolve(userId: String): ChatParticipantUi {
        return participantsById[userId]?.toUi() ?: ChatParticipantUi(
            id = userId,
            username = userId,
            initials = userId.take(2).uppercase()
        )
    }

    return when (message.type) {
        ChatMessageType.PARTICIPANTS_JOINED -> {
            val payload = message.payload as? ChatMessagePayload.ParticipantsJoined ?: return null
            MessageUi.ParticipantAdded(
                id = message.id,
                addedBy = resolve(sender.userId),
                addedUsers = payload.joinedUserIds.map { resolve(it) }
            )
        }

        ChatMessageType.PARTICIPANTS_REMOVED -> {
            val payload = message.payload as? ChatMessagePayload.ParticipantsRemoved ?: return null
            MessageUi.ParticipantRemoved(
                id = message.id,
                removedBy = resolve(sender.userId),
                removedUsers = payload.removedUserIds.map { resolve(it) }
            )
        }

        ChatMessageType.PARTICIPANT_LEFT -> {
            val payload = message.payload as? ChatMessagePayload.ParticipantLeft ?: return null
            MessageUi.ParticipantLeft(
                id = message.id,
                participant = resolve(payload.userId)
            )
        }

        ChatMessageType.TEXT, ChatMessageType.IMAGE -> null
    }
}