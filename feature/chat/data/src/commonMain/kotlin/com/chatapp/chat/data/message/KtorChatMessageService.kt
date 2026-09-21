package com.chatapp.chat.data.message

import com.chatapp.chat.data.dto.ChatMessageDto
import com.chatapp.chat.data.dto.SendMessageRequestDto
import com.chatapp.chat.data.dto.response.ProfilePictureUploadUrlsResponse
import com.chatapp.chat.data.mappers.toDomain
import com.chatapp.chat.domain.message.ChatMessageService
import com.chatapp.chat.domain.models.ChatMessage
import com.chatapp.chat.domain.models.ProfilePictureUploadUrls
import com.chatapp.core.data.network.delete
import com.chatapp.core.data.network.get
import com.chatapp.core.data.network.post
import com.chatapp.core.data.network.safeCall
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import com.chatapp.core.domain.util.Result
import com.chatapp.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.get as ktorGet
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url

class KtorChatMessageService(
    private val httpClient: HttpClient
): ChatMessageService {

    override suspend fun deleteMessage(messageId: String): EmptyResult<DataError.Remote> {
        return httpClient.delete(
            route = "/messages/$messageId"
        )
    }

    override suspend fun sendMessage(
        chatId: String,
        content: String,
        messageId: String?,
        imageUrls: List<String>?
    ): Result<ChatMessage, DataError.Remote> {
        return httpClient.post<SendMessageRequestDto, ChatMessageDto>(
            route = "/messages/$chatId",
            body = SendMessageRequestDto(
                content = content,
                messageId = messageId,
                imageUrls = imageUrls
            )
        ).map { it.toDomain() }
    }

    override suspend fun getImageUploadUrl(
        chatId: String,
        mimeType: String
    ): Result<ProfilePictureUploadUrls, DataError.Remote> {
        return httpClient.post<Unit, ProfilePictureUploadUrlsResponse>(
            route = "/messages/$chatId/image-upload-url",
            queryParams = mapOf(
                "mimeType" to mimeType
            ),
            body = Unit
        ).map { it.toDomain() }
    }

    override suspend fun uploadImage(
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

    override suspend fun downloadImage(url: String): Result<ByteArray, DataError.Remote> {
        return safeCall {
            httpClient.ktorGet {
                url(url)
            }
        }
    }

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError.Remote> {
        return httpClient.get<List<ChatMessageDto>>(
            route = "/chat/$chatId/messages",
            queryParams = buildMap {
                this["pageSize"] = ChatMessageConstants.PAGE_SIZE
                if(before != null) {
                    this["before"] = before
                }
            }
        ).map { it.map { it.toDomain() } }
    }
}