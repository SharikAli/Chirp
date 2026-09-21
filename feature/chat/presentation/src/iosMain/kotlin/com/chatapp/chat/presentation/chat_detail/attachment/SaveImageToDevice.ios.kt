@file:OptIn(ExperimentalForeignApi::class)

package com.chatapp.chat.presentation.chat_detail.attachment

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.Photos.PHAccessLevelAddOnly
import kotlin.coroutines.resume

actual suspend fun saveImageToDevice(bytes: ByteArray, fileName: String): Boolean {
    val authorized = requestAddOnlyAuthorization()
    if (!authorized) return false

    val data = bytes.toNSData()

    return suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            changeBlock = {
                val request = PHAssetCreationRequest.creationRequestForAsset()
                request.addResourceWithType(
                    type = platform.Photos.PHAssetResourceTypePhoto,
                    data = data,
                    options = null
                )
            },
            completionHandler = { success, _ ->
                continuation.resume(success)
            }
        )
    }
}

private suspend fun requestAddOnlyAuthorization(): Boolean {
    val current = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelAddOnly)
    if (current == PHAuthorizationStatusAuthorized || current == PHAuthorizationStatusLimited) {
        return true
    }

    return suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status: PHAuthorizationStatus ->
            continuation.resume(
                status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
            )
        }
    }
}

@OptIn(kotlinx.cinterop.BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
}
