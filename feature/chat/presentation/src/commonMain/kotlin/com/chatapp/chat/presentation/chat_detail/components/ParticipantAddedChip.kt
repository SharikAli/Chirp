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
fun ParticipantAddedChip(
    messageUi: MessageUi.ParticipantAdded,
    modifier: Modifier = Modifier,
) {
    val addedMessage = remember(messageUi) {
        val addedUsers = messageUi.addedUsers

        val addedUserMessage = when (addedUsers.size) {
            0 -> ""
            1 -> addedUsers.joinToString { it.username }
            2 -> addedUsers.joinToString(" and ") { it.username }
            3 -> {
                val remaining = addedUsers.size - 2
                "${addedUsers.take(2).joinToString { it.username }} and $remaining other"
            }

            else -> {
                val remaining = addedUsers.size - 2
                "${addedUsers.take(2).joinToString { it.username }} and $remaining others"
            }
        }

        "${messageUi.addedBy.username} added $addedUserMessage"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = addedMessage,
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
