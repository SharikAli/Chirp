package com.chatapp.chat.data.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.chatapp.feature.chat.data.R

/**
 * Builds the WhatsApp-style "new message" notification (MessagingStyle + inline reply action)
 * shared between the initial FCM-triggered notification (ChirpFirebaseMessagingService) and the
 * notification shown again after a reply is sent/fails (NotificationReplyReceiver), so both stay
 * visually consistent.
 */
object ChatNotificationBuilder {

    private const val MAX_THREAD_MESSAGES = 5

    /** One line of the notification's conversation thread. */
    data class MessageEntry(
        val senderName: String,
        val body: String,
        val timestampMillis: Long,
        val isMe: Boolean
    )

    fun ensureChannel(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        if (manager.getNotificationChannelCompat(ChatNotificationConstants.CHANNEL_ID_MESSAGES) != null) {
            return
        }

        val channel = NotificationChannelCompat.Builder(
            ChatNotificationConstants.CHANNEL_ID_MESSAGES,
            NotificationManagerCompat.IMPORTANCE_HIGH
        )
            .setName(context.getString(R.string.notification_channel_messages_name))
            .setDescription(context.getString(R.string.notification_channel_messages_description))
            .build()

        manager.createNotificationChannel(channel)
    }

    /**
     * Folds [newEntry] into whatever thread is already showing for [chatId] (read back from the
     * still-active notification, if any), capped at [MAX_THREAD_MESSAGES] - the oldest entry is
     * dropped once a 6th arrives. Once the notification is dismissed there's nothing to read
     * back, so the next message starts a fresh single-entry thread.
     */
    fun mergeWithActiveNotification(
        context: Context,
        chatId: String,
        newEntry: MessageEntry
    ): List<MessageEntry> {
        val notificationId = ChatNotificationConstants.notificationIdForChat(chatId)

        val existingStyle = NotificationManagerCompat.from(context)
            .activeNotifications
            .firstOrNull { it.id == notificationId }
            ?.notification
            ?.let { NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(it) }

        val priorMessages = existingStyle?.messages.orEmpty().map { message ->
            MessageEntry(
                // A null person is MessagingStyle's own convention for "sent by the local user"
                // (see the addMessage(..., person = null) call below).
                senderName = message.person?.name?.toString() ?: "You",
                body = message.text?.toString().orEmpty(),
                timestampMillis = message.timestamp,
                isMe = message.person == null
            )
        }

        return (priorMessages + newEntry).takeLast(MAX_THREAD_MESSAGES)
    }

    /**
     * @param messages The conversation thread to show, oldest first. Must contain at least one
     * entry - build this via [mergeWithActiveNotification].
     * @param replyFailed Marks the notification as showing a reply that failed to send (keeps
     * the reply action so the user can retry) instead of the normal "new message" framing.
     */
    fun buildNewMessageNotification(
        context: Context,
        chatId: String,
        conversationTitle: String,
        messages: List<MessageEntry>,
        replyFailed: Boolean = false
    ): Notification {
        ensureChannel(context)

        val notificationId = ChatNotificationConstants.notificationIdForChat(chatId)

        val me = Person.Builder().setName("You").build()
        val people = mutableMapOf<String, Person>()

        fun personFor(entry: MessageEntry): Person? {
            if (entry.isMe) return null
            return people.getOrPut(entry.senderName) { Person.Builder().setName(entry.senderName).build() }
        }

        val style = NotificationCompat.MessagingStyle(me)
            .setConversationTitle(conversationTitle)

        messages.forEach { entry ->
            style.addMessage(entry.body, entry.timestampMillis, personFor(entry))
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            buildOpenChatIntent(context, chatId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, ChatNotificationConstants.CHANNEL_ID_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(style)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setGroup(chatId)
            .addAction(
                buildReplyAction(
                    context = context,
                    chatId = chatId,
                    notificationId = notificationId,
                    conversationTitle = conversationTitle
                )
            )

        if (replyFailed) {
            builder.setContentText(context.getString(R.string.notification_reply_failed))
        }

        return builder.build()
    }

    /** For informational, non-repliable notifications (e.g. removed from chat, chat deleted). */
    fun buildPlainNotification(
        context: Context,
        chatId: String,
        title: String,
        body: String
    ): Notification {
        ensureChannel(context)

        val notificationId = ChatNotificationConstants.notificationIdForChat(chatId)
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            buildOpenChatIntent(context, chatId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, ChatNotificationConstants.CHANNEL_ID_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun buildReplyAction(
        context: Context,
        chatId: String,
        notificationId: Int,
        conversationTitle: String
    ): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(ChatNotificationConstants.KEY_REPLY_TEXT)
            .setLabel(context.getString(R.string.notification_reply_hint))
            .build()

        val replyIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
            action = ChatNotificationConstants.ACTION_REPLY
            putExtra(ChatNotificationConstants.EXTRA_CHAT_ID, chatId)
            putExtra(ChatNotificationConstants.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ChatNotificationConstants.EXTRA_TITLE, conversationTitle)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            replyIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.notification_reply_action),
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .build()
    }

    private fun buildOpenChatIntent(context: Context, chatId: String): Intent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()

        return launchIntent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("chatId", chatId)
        }
    }

    fun show(context: Context, notificationId: Int, notification: Notification) {
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted - drop silently, matching platform behavior when
            // the user has declined notification permission.
        }
    }

    fun cancel(context: Context, chatId: String) {
        NotificationManagerCompat.from(context)
            .cancel(ChatNotificationConstants.notificationIdForChat(chatId))
    }
}
