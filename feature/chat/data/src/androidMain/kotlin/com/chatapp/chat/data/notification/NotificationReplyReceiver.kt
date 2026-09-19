package com.chatapp.chat.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.chatapp.chat.domain.message.MessageRepository
import com.chatapp.core.domain.util.onFailure
import com.chatapp.core.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Handles the "Reply" action's typed text (via RemoteInput) from the notification built in
 * ChatNotificationBuilder/ChirpFirebaseMessagingService. Runs with no app UI open - the send
 * goes over plain REST (MessageRepository.sendMessageViaRest), not the WebSocket connection,
 * since nothing guarantees a live socket while the app isn't running.
 */
@OptIn(ExperimentalUuidApi::class)
class NotificationReplyReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ChatNotificationConstants.ACTION_REPLY) return

        val chatId = intent.getStringExtra(ChatNotificationConstants.EXTRA_CHAT_ID) ?: return
        val replyText = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(ChatNotificationConstants.KEY_REPLY_TEXT)
            ?.toString()
            ?.trim()

        if (replyText.isNullOrBlank()) return

        val messageRepository = get<MessageRepository>()
        val applicationScope = get<CoroutineScope>()

        val appContext = context.applicationContext
        val title = intent.getStringExtra(ChatNotificationConstants.EXTRA_TITLE) ?: chatId
        val pendingResult = goAsync()

        applicationScope.launch {
            try {
                messageRepository
                    .sendMessageViaRest(
                        chatId = chatId,
                        content = replyText,
                        messageId = Uuid.random().toString()
                    )
                    .onSuccess {
                        showReplyInThread(appContext, chatId, title, replyText, failed = false)
                    }
                    .onFailure {
                        showReplyInThread(appContext, chatId, title, replyText, failed = true)
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }

    // Keeps the reply visible in the same thread (rather than resetting or dismissing the
    // notification), so a reply, then a new incoming message, keeps building up to the 5-message
    // cap in ChatNotificationBuilder.mergeWithActiveNotification.
    private fun showReplyInThread(
        context: Context,
        chatId: String,
        conversationTitle: String,
        replyText: String,
        failed: Boolean
    ) {
        val messages = ChatNotificationBuilder.mergeWithActiveNotification(
            context = context,
            chatId = chatId,
            newEntry = ChatNotificationBuilder.MessageEntry(
                senderName = "You",
                body = replyText,
                timestampMillis = System.currentTimeMillis(),
                isMe = true
            )
        )

        ChatNotificationBuilder.show(
            context = context,
            notificationId = ChatNotificationConstants.notificationIdForChat(chatId),
            notification = ChatNotificationBuilder.buildNewMessageNotification(
                context = context,
                chatId = chatId,
                conversationTitle = conversationTitle,
                messages = messages,
                replyFailed = failed
            )
        )
    }
}
