package com.chatapp.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chatapp.chat.presentation.model.MessageUi
import com.chatapp.core.designsystem.theme.extended

@Composable
fun ParticipantRemovedChip(
    messageUi: MessageUi.ParticipantRemoved,
    modifier: Modifier = Modifier,
) {
    val removedMessage = remember(messageUi) {
        val removedUsers = messageUi.removedUsers

        val removedUserMessage = when (removedUsers.size) {
            0 -> ""
            1 -> removedUsers.joinToString { it.username }
            2 -> removedUsers.joinToString(" and ") { it.username }
            3 -> {
                val remaining = removedUsers.size - 2
                "${removedUsers.take(2).joinToString { it.username }} and $remaining other"
            }

            else -> {
                val remaining = removedUsers.size - 2
                "${removedUsers.take(2).joinToString { it.username }} and $remaining others"
            }
        }

        "${messageUi.removedBy.username} removed $removedUserMessage"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = removedMessage,
            modifier = Modifier
                .padding(
                    vertical = 4.dp,
                    horizontal = 12.dp
                )
                .align(Alignment.Center),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.extended.textPlaceholder,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}