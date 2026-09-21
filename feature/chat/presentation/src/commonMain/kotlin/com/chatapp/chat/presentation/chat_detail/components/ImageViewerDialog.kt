package com.chatapp.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.close
import chirp.feature.chat.presentation.generated.resources.save_to_device
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageViewerDialog(
    imageUrls: List<String>,
    startIndex: Int,
    onDismiss: () -> Unit,
    onSaveClick: (url: String) -> Unit,
) {
    if (imageUrls.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val pagerState = rememberPagerState(
            initialPage = startIndex.coerceIn(0, imageUrls.lastIndex)
        ) { imageUrls.size }

        var showSaveMenu by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(imageUrls[page]) {
                            detectTapGestures(
                                onLongPress = { showSaveMenu = true }
                            )
                        }
                ) {
                    AsyncImage(
                        model = imageUrls[page],
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    DropdownMenu(
                        expanded = showSaveMenu && page == pagerState.currentPage,
                        onDismissRequest = { showSaveMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.save_to_device)) },
                            leadingIcon = {
                                Icon(Icons.Default.Download, contentDescription = null)
                            },
                            onClick = {
                                showSaveMenu = false
                                onSaveClick(imageUrls[page])
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.close),
                    tint = Color.White
                )
            }
        }
    }
}
