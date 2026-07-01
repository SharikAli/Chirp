package com.chatapp.chat.data.message

import com.chatapp.chat.data.dto.ChatMessageDto
import com.chatapp.chat.data.mappers.toDomain
import com.chatapp.chat.domain.message.ChatMessageService
import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.core.data.network.get
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.Result
import com.chatapp.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatMessageService(
    private val httpClient: HttpClient
) : ChatMessageService {

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError.Remote> {
        return httpClient.get<List<ChatMessageDto>>(
            route = "/chat/$chatId/messages",
            queryParams = buildMap {
                this["pageSize"] = ChatMessageConstants.PAGE_SIZE
                if (before != null) {
                    this["before"] = before
                }
            }
        ).map { it.map { it.toDomain() } }
    }
}