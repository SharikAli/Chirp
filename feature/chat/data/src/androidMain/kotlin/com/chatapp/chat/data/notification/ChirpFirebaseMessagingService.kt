package com.chatapp.chat.data.notification

import com.chatapp.chat.domain.notification.DeviceTokenService
import com.chatapp.core.domain.auth.SessionStorage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ChirpFirebaseMessagingService : FirebaseMessagingService() {

    private val deviceTokenService by inject<DeviceTokenService>()
    private val sessionStorage by inject<SessionStorage>()
    private val applicationScope by inject<CoroutineScope>()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        applicationScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().first()
            if (authInfo != null) {
                deviceTokenService.registerToken(
                    token = token,
                    platform = "ANDROID"
                )
            }
        }
    }

    // Sent as data-only from the backend (see FirebasePushNotificationService on the server)
    // specifically so this always runs - a "notification" payload would be auto-displayed by
    // the OS without ever reaching this method, which would make it impossible to attach the
    // inline reply action below.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val chatId = data["chatId"] ?: return
        val title = data["title"] ?: return
        val body = data["body"] ?: return

        if (data["type"] != "new_message") {
            // "removed from chat" / "chat deleted" etc. - informational only, no reply action.
            ChatNotificationBuilder.show(
                context = this,
                notificationId = ChatNotificationConstants.notificationIdForChat(chatId),
                notification = ChatNotificationBuilder.buildPlainNotification(
                    context = this,
                    chatId = chatId,
                    title = title,
                    body = body
                )
            )
            return
        }

        val senderName = data["senderUsername"] ?: title

        // The thread shown is whatever's already in the active notification (if any) plus this
        // message, capped at 5 - see ChatNotificationBuilder.mergeWithActiveNotification. A
        // dismissed notification has nothing to read back, so the next message starts fresh.
        val messages = ChatNotificationBuilder.mergeWithActiveNotification(
            context = this,
            chatId = chatId,
            newEntry = ChatNotificationBuilder.MessageEntry(
                senderName = senderName,
                body = body,
                timestampMillis = System.currentTimeMillis(),
                isMe = false
            )
        )

        ChatNotificationBuilder.show(
            context = this,
            notificationId = ChatNotificationConstants.notificationIdForChat(chatId),
            notification = ChatNotificationBuilder.buildNewMessageNotification(
                context = this,
                chatId = chatId,
                conversationTitle = title,
                messages = messages
            )
        )
    }
}
