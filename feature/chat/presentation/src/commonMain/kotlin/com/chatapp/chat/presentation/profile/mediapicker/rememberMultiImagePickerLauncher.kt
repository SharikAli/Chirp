package com.chatapp.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable

@Composable
expect fun rememberMultiImagePickerLauncher(
    maxItems: Int,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher
