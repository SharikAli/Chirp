package com.chatapp.chat.data.participant

import com.chatapp.chat.data.dto.ChatParticipantDto
import com.chatapp.chat.data.dto.request.ConfirmProfilePictureRequest
import com.chatapp.chat.data.dto.request.RemoveParticipantsRequest
import com.chatapp.chat.data.dto.response.ProfilePictureUploadUrlsResponse
import com.chatapp.chat.data.mappers.toDomain
import com.chatapp.chat.domain.models.ChatParticipant
import com.chatapp.chat.domain.models.ProfilePictureUploadUrls
import com.chatapp.chat.domain.participant.ChatParticipantService
import com.chatapp.core.data.network.delete
import com.chatapp.core.data.network.get
import com.chatapp.core.data.network.post
import com.chatapp.core.data.network.safeCall
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import com.chatapp.core.domain.util.Result
import com.chatapp.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url

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

    override suspend fun getLocalParticipant(): Result<ChatParticipant, DataError.Remote> {
        return httpClient.get<ChatParticipantDto>(
            route = "/participants"
        ).map { it.toDomain() }
    }

    override suspend fun getProfilePictureUploadUrl(mimeType: String): Result<ProfilePictureUploadUrls, DataError.Remote> {
        return httpClient.post<Unit, ProfilePictureUploadUrlsResponse>(
            route = "/participants/profile-picture-upload",
            queryParams = mapOf(
                "mimeType" to mimeType
            ),
            body = Unit
        ).map { it.toDomain() }
    }

    override suspend fun uploadProfilePicture(
        uploadUrl: String,
        imageBytes: ByteArray,
        headers: Map<String, String>
    ): EmptyResult<DataError.Remote> {
        return safeCall {
            httpClient.put {
                url(uploadUrl)
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                setBody(imageBytes)
            }
        }
    }

    override suspend fun confirmProfilePictureUpload(publicUrl: String): EmptyResult<DataError.Remote> {
        return httpClient.post<ConfirmProfilePictureRequest, Unit>(
            route = "/participants/confirm-profile-picture",
            body = ConfirmProfilePictureRequest(publicUrl)
        )
    }

    override suspend fun deleteProfilePicture(): EmptyResult<DataError.Remote> {
        return httpClient.delete(
            route = "/participants/profile-picture"
        )
    }

    override suspend fun removeParticipants(
        chatId: String,
        userIds: List<String>
    ): EmptyResult<DataError.Remote> {
        return httpClient.delete<RemoveParticipantsRequest, Unit>(
            route = "/chat/remove/participants",
            body = RemoveParticipantsRequest(
                chatId = chatId,
                userIds = userIds
            )
        )
    }
}