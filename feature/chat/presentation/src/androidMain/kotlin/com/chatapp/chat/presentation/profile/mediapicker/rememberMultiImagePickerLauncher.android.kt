package com.chatapp.chat.presentation.profile.mediapicker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
actual fun rememberMultiImagePickerLauncher(
    maxItems: Int,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems)
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val parser = ContentUriParser(context)
                val images = uris.mapNotNull { uri ->
                    val bytes = parser.readUri(uri) ?: return@mapNotNull null
                    PickedImageData(bytes = bytes, mimeType = parser.getMimeType(uri))
                }
                onResult(images)
            }
        }
    }

    return remember {
        ImagePickerLauncher(
            onLaunch = {
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }
}
