package com.chatapp.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImageData) -> Unit): ImagePickerLauncher {
    return ImagePickerLauncher { }
}