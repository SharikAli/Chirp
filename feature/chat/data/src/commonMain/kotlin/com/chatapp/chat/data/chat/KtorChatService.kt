package com.chatapp.chat.data.chat

import com.chatapp.chat.data.dto.ChatDto
import com.chatapp.chat.data.dto.request.CreateChatRequest
import com.chatapp.chat.data.mappers.toDomain
import com.chatapp.chat.domain.chat.ChatService
import com.chatapp.chat.domain.models.Chat
import com.chatapp.core.data.network.post
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.Result
import com.chatapp.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatService(
    private val httpClient: HttpClient
) : ChatService {

    override suspend fun createChat(otherUserIds: List<String>): Result<Chat, DataError.Remote> {
        return httpClient.post<CreateChatRequest, ChatDto>(
            route = "/chat",
            body = CreateChatRequest(
                otherUserIds = otherUserIds
            )
        ).map { it.toDomain() }
    }
}