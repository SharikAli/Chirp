package com.chatapp.chat.data.chat

import com.chatapp.chat.data.dto.ChatParticipantDto
import com.chatapp.chat.data.mappers.toDomain
import com.chatapp.chat.domain.chat.ChatParticipantService
import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.core.data.network.get
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.Result
import com.chatapp.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatParticipantService(
    private val httpClient: HttpClient
) : ChatParticipantService {

    override suspend fun searchParticipant(query: String): Result<ChatParticipant, DataError.Remote> {
        return httpClient.get<ChatParticipantDto>(
            route = "/participants",
            queryParams = mapOf(
                "query" to query
            )
        ).map { it.toDomain() }
    }
}