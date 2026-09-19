package com.chatapp.chat.data.notification

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
 * Handles a notification "reply" action's typed text on iOS - called from
 * AppDelegate.userNotificationCenter(_:didReceive:) via IosNotificationReplyBridge while the app
 * may be fully backgrounded. Sends over plain REST (MessageRepository.sendMessageViaRest), not
 * the WebSocket connection, since nothing guarantees a live socket in that state. Callback-based
 * rather than suspend so it's trivially callable from Swift, matching IosDeviceTokenHolderBridge.
 */
@OptIn(ExperimentalUuidApi::class)
object IosNotificationReplyHandler : KoinComponent {

    fun sendReply(
        chatId: String,
        content: String,
        onResult: (Boolean) -> Unit
    ) {
        val messageRepository = get<MessageRepository>()
        val applicationScope = get<CoroutineScope>()

        applicationScope.launch {
            messageRepository
                .sendMessageViaRest(
                    chatId = chatId,
                    content = content,
                    messageId = Uuid.random().toString()
                )
                .onSuccess { onResult(true) }
                .onFailure { onResult(false) }
        }
    }
}
