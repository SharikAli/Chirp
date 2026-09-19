package com.chatapp.chirp

import com.chatapp.chat.data.notification.IosNotificationReplyHandler

object IosNotificationReplyBridge {
    fun sendReply(
        chatId: String,
        content: String,
        onResult: (Boolean) -> Unit
    ) {
        IosNotificationReplyHandler.sendReply(chatId, content, onResult)
    }
}
