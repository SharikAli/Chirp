package com.chatapp.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.remove_attachment
import coil3.compose.AsyncImage
import com.chatapp.chat.presentation.chat_detail.model.PendingAttachment
import com.chatapp.chat.presentation.chat_detail.model.PendingAttachmentStatus
import com.chatapp.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource

@Composable
fun PendingAttachmentRow(
    attachments: List<PendingAttachment>,
    onRemoveClick: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(attachments, key = { it.id }) { attachment ->
            PendingAttachmentThumbnail(
                attachment = attachment,
                onRemoveClick = { onRemoveClick(attachment.id) }
            )
        }
    }
}

@Composable
private fun PendingAttachmentThumbnail(
    attachment: PendingAttachment,
    onRemoveClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(45.dp)
    ) {
        val bytes = attachment.compressedBytes

        // While compressing (or if the thumbnail can't be rendered), fall back to a plain
        // file-icon + filename card instead of leaving a blank/broken thumbnail.
        if (attachment.status == PendingAttachmentStatus.READY && bytes != null) {
            AsyncImage(
                model = bytes,
                contentDescription = attachment.fileName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Column(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.extended.surfaceHigher)
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (attachment.status == PendingAttachmentStatus.PROCESSING) {
                    CircularProgressIndicator(modifier = Modifier.size(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.extended.textSecondary
                    )
                }
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(Res.string.remove_attachment),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 4.dp)
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.extended.surfaceHigher)
                .clickable(
                    onClick = onRemoveClick
                )
        )
    }
}
