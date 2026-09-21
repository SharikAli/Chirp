package com.chatapp.chat.presentation.chat_detail.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.attach_image
import chirp.feature.chat.presentation.generated.resources.attachment
import chirp.feature.chat.presentation.generated.resources.cloud_off_icon
import chirp.feature.chat.presentation.generated.resources.send
import chirp.feature.chat.presentation.generated.resources.send_a_message
import com.chatapp.chat.domain.models.ConnectionState
import com.chatapp.chat.presentation.chat_detail.model.PendingAttachment
import com.chatapp.chat.presentation.chat_detail.model.PendingAttachmentStatus
import com.chatapp.chat.presentation.util.toUiText
import com.chatapp.core.designsystem.components.ChirpButton
import com.chatapp.core.designsystem.components.textfields.ChirpMultiLineTextField
import com.chatapp.core.designsystem.theme.ChirpTheme
import com.chatapp.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun MessageBox(
    messageTextFieldState: TextFieldState,
    isSendButtonEnabled: Boolean,
    connectionState: ConnectionState,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    pendingAttachments: List<PendingAttachment> = emptyList(),
    onAttachClick: () -> Unit = {},
    onRemoveAttachment: (String) -> Unit = {}
) {
    val isConnected = connectionState == ConnectionState.CONNECTED
    Column(modifier = modifier) {
        ChirpMultiLineTextField(
            state = messageTextFieldState,
            modifier = Modifier
                .onPreviewKeyEvent { keyEvent ->
                    val isModifierKeyPressed = keyEvent.isMetaPressed || keyEvent.isCtrlPressed
                    val isSendShortcutPressed = isModifierKeyPressed
                            && keyEvent.key == Key.Enter
                            && keyEvent.type == KeyEventType.KeyDown

                    if (isSendShortcutPressed) {
                        onSendClick()
                        true
                    } else false
                },
            placeholder = stringResource(Res.string.send_a_message),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            onKeyboardAction = onSendClick,
            bottomContent = {
                if (pendingAttachments.isNotEmpty()) {
                    PendingAttachmentRow(
                        attachments = pendingAttachments,
                        onRemoveClick = onRemoveAttachment,
                        modifier = Modifier
                            .weight(1f)
                    )
                }
                if (pendingAttachments.isEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (!isConnected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.cloud_off_icon),
                            contentDescription = connectionState.toUiText().asString(),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.extended.textDisabled
                        )
                        Text(
                            text = connectionState.toUiText().asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.extended.textDisabled
                        )
                    }
                }
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.attachment),
                        contentDescription = stringResource(Res.string.attach_image)
                    )
                }

                ChirpButton(
                    text = stringResource(Res.string.send),
                    onClick = onSendClick,
                    enabled = isConnected && (isSendButtonEnabled || pendingAttachments.isNotEmpty())
                )
            }
        )
    }
}

@Composable
@Preview
fun MessageBoxPreview() {
    ChirpTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MessageBox(
                messageTextFieldState = rememberTextFieldState(),
                isSendButtonEnabled = true,
                connectionState = ConnectionState.CONNECTED,
                onSendClick = {},
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
@Preview
fun MessageBoxWithAttachmentsPreview() {
    ChirpTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MessageBox(
                messageTextFieldState = rememberTextFieldState(
                    initialText = "Check these out!"
                ),
                isSendButtonEnabled = true,
                connectionState = ConnectionState.CONNECTED,
                onSendClick = {},
                pendingAttachments = listOf(
                    PendingAttachment(
                        id = "1",
                        fileName = "sunset.jpg",
                        mimeType = "image/jpeg",
                        status = PendingAttachmentStatus.PROCESSING
                    ),
                    PendingAttachment(
                        id = "2",
                        fileName = "beach.jpg",
                        mimeType = "image/jpeg",
                        status = PendingAttachmentStatus.READY,
                        compressedBytes = ByteArray(0)
                    ),
                    PendingAttachment(
                        id = "3",
                        fileName = "mountains.jpg",
                        mimeType = "image/jpeg",
                        status = PendingAttachmentStatus.FAILED
                    )
                ),
                onAttachClick = {},
                onRemoveAttachment = {},
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}