package com.chatapp.chat.data.notification

object ChatNotificationConstants {
    const val CHANNEL_ID_MESSAGES = "messages"

    const val ACTION_REPLY = "com.chatapp.chirp.action.REPLY_TO_CHAT"
    const val KEY_REPLY_TEXT = "key_reply_text"

    const val EXTRA_CHAT_ID = "extra_chat_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_TITLE = "extra_title"

    fun notificationIdForChat(chatId: String): Int = chatId.hashCode()
}
