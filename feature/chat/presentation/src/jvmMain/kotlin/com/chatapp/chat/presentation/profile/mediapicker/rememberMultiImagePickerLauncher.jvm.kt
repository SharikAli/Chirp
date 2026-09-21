package com.chatapp.chat.presentation.profile.mediapicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.select_a_profile_picture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import java.nio.file.Files
import javax.swing.SwingUtilities
import kotlin.coroutines.resume

@Composable
actual fun rememberMultiImagePickerLauncher(
    maxItems: Int,
    onResult: (List<PickedImageData>) -> Unit
): ImagePickerLauncher {
    val scope = rememberCoroutineScope()
    val dialogTitle = stringResource(Res.string.select_a_profile_picture)
    return remember {
        ImagePickerLauncher(
            onLaunch = {
                scope.launch {
                    val images = pickImages(dialogTitle, maxItems)
                    if (images.isNotEmpty()) {
                        onResult(images)
                    }
                }
            }
        )
    }
}

private suspend fun pickImages(fileDialogTitle: String, maxItems: Int): List<PickedImageData> {
    val files = suspendCancellableCoroutine { continuation ->
        var fileDialog: FileDialog? = null

        continuation.invokeOnCancellation {
            SwingUtilities.invokeLater {
                fileDialog?.dispose()
            }
        }

        SwingUtilities.invokeLater {
            try {
                fileDialog = FileDialog(Frame(), fileDialogTitle, FileDialog.LOAD)
                fileDialog.filenameFilter = FilenameFilter { _, name ->
                    allowedImageExtensions.any { name.endsWith(it) }
                }
                fileDialog.isMultipleMode = true
                fileDialog.isVisible = true

                continuation.resume(fileDialog.files.take(maxItems).toList())
            } catch (_: Exception) {
                continuation.resume(emptyList())
            }
        }
    }

    return withContext(Dispatchers.IO) {
        files.mapNotNull { file ->
            try {
                PickedImageData(
                    bytes = Files.readAllBytes(file.toPath()),
                    mimeType = getMimeTypeFromFileName(file.name)
                )
            } catch (_: Exception) {
                coroutineContext.ensureActive()
                null
            }
        }
    }
}
