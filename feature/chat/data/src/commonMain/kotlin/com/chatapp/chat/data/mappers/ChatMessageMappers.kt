package com.chatapp.chat.data.mappers

import com.chatapp.chat.data.dto.ChatMessageDto
import com.chatapp.chat.data.dto.websocket.IncomingWebSocketDto
import com.chatapp.chat.data.dto.websocket.OutgoingWebSocketDto
import com.chatapp.chat.database.entities.ChatMessageEntity
import com.chatapp.chat.database.view.LastMessageView
import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.chat.domain.models.ChatMessageDeliveryStatus
import com.chatapp.chat.domain.models.ChatMessagePayload
import com.chatapp.chat.domain.models.ChatMessageType
import com.chatapp.chat.domain.models.OutgoingNewMessage
import com.chatapp.chat.domain.models.OutgoingUserTyping
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock
import kotlin.time.Instant

private val payloadJson = Json { ignoreUnknownKeys = true }

private fun parseMessageType(rawType: String): ChatMessageType {
    return ChatMessageType.entries.find { it.name == rawType } ?: ChatMessageType.TEXT
}

private fun parsePayload(type: ChatMessageType, jsonElement: JsonElement?): ChatMessagePayload? {
    if (jsonElement == null) return null

    return try {
        val jsonObject = jsonElement.jsonObject
        when (type) {
            ChatMessageType.PARTICIPANTS_JOINED -> ChatMessagePayload.ParticipantsJoined(
                joinedUserIds = jsonObject["joinedUserIds"]?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                    .orEmpty()
            )

            ChatMessageType.PARTICIPANTS_REMOVED -> ChatMessagePayload.ParticipantsRemoved(
                removedUserIds = jsonObject["removedUserIds"]?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                    .orEmpty()
            )

            ChatMessageType.PARTICIPANT_LEFT -> ChatMessagePayload.ParticipantLeft(
                userId = jsonObject["userId"]?.jsonPrimitive?.content.orEmpty()
            )

            ChatMessageType.TEXT, ChatMessageType.IMAGE -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun parsePayload(type: ChatMessageType, rawPayload: String?): ChatMessagePayload? {
    val jsonElement = rawPayload?.let { payloadJson.parseToJsonElement(it) }
    return parsePayload(type, jsonElement)
}

private fun ChatMessagePayload.toRawJson(): String {
    return when (this) {
        is ChatMessagePayload.ParticipantsJoined -> payloadJson.encodeToString(
            kotlinx.serialization.json.JsonObject(
                mapOf(
                    "joinedUserIds" to kotlinx.serialization.json.JsonArray(
                        joinedUserIds.map { kotlinx.serialization.json.JsonPrimitive(it) }
                    )
                )
            )
        )

        is ChatMessagePayload.ParticipantsRemoved -> payloadJson.encodeToString(
            kotlinx.serialization.json.JsonObject(
                mapOf(
                    "removedUserIds" to kotlinx.serialization.json.JsonArray(
                        removedUserIds.map { kotlinx.serialization.json.JsonPrimitive(it) }
                    )
                )
            )
        )

        is ChatMessagePayload.ParticipantLeft -> payloadJson.encodeToString(
            kotlinx.serialization.json.JsonObject(
                mapOf("userId" to kotlinx.serialization.json.JsonPrimitive(userId))
            )
        )
    }
}

fun ChatMessageDto.toDomain(): ChatMessage {
    val messageType = parseMessageType(type)
    return ChatMessage(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = Instant.parse(createdAt),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.SENT,
        type = messageType,
        payload = parsePayload(messageType, payload)
    )
}

fun ChatMessageEntity.toDomain(): ChatMessage {
    val messageType = parseMessageType(type)
    return ChatMessage(
        id = messageId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(deliveryStatus),
        type = messageType,
        payload = parsePayload(messageType, payload)
    )
}

fun LastMessageView.toDomain(): ChatMessage {
    val messageType = parseMessageType(type)
    return ChatMessage(
        id = messageId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(this.deliveryStatus),
        type = messageType,
        payload = parsePayload(messageType, payload)
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name,
        type = type.name,
        payload = payload?.toRawJson()
    )
}

fun ChatMessage.toLastMessageView(): LastMessageView {
    return LastMessageView(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name,
        senderUsername = null
    )
}

fun ChatMessage.toNewMessage(): OutgoingWebSocketDto.NewMessage {
    return OutgoingWebSocketDto.NewMessage(
        messageId = id,
        chatId = chatId,
        content = content,
    )
}

fun IncomingWebSocketDto.NewMessageDto.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = Instant.parse(createdAt).toEpochMilliseconds(),
        deliveryStatus = ChatMessageDeliveryStatus.SENT.name,
        type = type,
        payload = payload?.let { payloadJson.encodeToString(JsonElement.serializer(), it) }
    )
}

fun OutgoingNewMessage.toWebSocketDto(): OutgoingWebSocketDto.NewMessage {
    return OutgoingWebSocketDto.NewMessage(
        chatId = chatId,
        messageId = messageId,
        content = content
    )
}

fun OutgoingWebSocketDto.NewMessage.toEntity(
    senderId: String,
    deliveryStatus: ChatMessageDeliveryStatus
): ChatMessageEntity {
    return ChatMessageEntity(
        messageId = messageId,
        chatId = chatId,
        content = content,
        senderId = senderId,
        deliveryStatus = deliveryStatus.name,
        timestamp = Clock.System.now().toEpochMilliseconds()
    )
}

fun OutgoingUserTyping.toWebSocketDto(): OutgoingWebSocketDto.UserTyping {
    return OutgoingWebSocketDto.UserTyping(
        chatId = chatId,
        isTyping = isTyping
    )
}