package com.chatapp.chat.data.message

import com.chatapp.chat.database.ChirpChatDatabase
import com.chatapp.chat.domain.message.MessageRepository
import com.chatapp.chat.domain.models.ChatMessageDeliveryStatus
import com.chatapp.core.data.database.safeDatabaseUpdate
import com.chatapp.core.domain.util.DataError
import com.chatapp.core.domain.util.EmptyResult
import kotlin.time.Clock

class OfflineFirstMessageRepository(
    private val database: ChirpChatDatabase
): MessageRepository {

    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate {
            database.chatMessageDao.updateDeliveryStatus(
                messageId = messageId,
                status = status.name,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }
    }
}