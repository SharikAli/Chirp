package com.chatapp.chat.data.mappers

import com.chatapp.chat.data.dto.response.ProfilePictureUploadUrlsResponse
import com.chatapp.chat.domain.models.ProfilePictureUploadUrls

fun ProfilePictureUploadUrlsResponse.toDomain(): ProfilePictureUploadUrls {
    return ProfilePictureUploadUrls(
        uploadUrl = uploadUrl,
        publicUrl = publicUrl,
        headers = headers
    )
}