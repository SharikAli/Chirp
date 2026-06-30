package com.chatapp.chat.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chatapp.chat.database.dao.ChatDao
import com.chatapp.chat.database.dao.ChatMessageDao
import com.chatapp.chat.database.dao.ChatParticipantDao
import com.chatapp.chat.database.dao.ChatParticipantsCrossRefDao
import com.chatapp.chat.database.entities.ChatEntity
import com.chatapp.chat.database.entities.ChatMessageEntity
import com.chatapp.chat.database.entities.ChatParticipantCrossRef
import com.chatapp.chat.database.entities.ChatParticipantEntity
import com.chatapp.chat.database.view.LastMessageView

@Database(
    entities = [
        ChatEntity::class,
        ChatParticipantEntity::class,
        ChatMessageEntity::class,
        ChatParticipantCrossRef::class,
    ],
    views = [
        LastMessageView::class
    ],
    version = 1,
)
abstract class ChirpChatDatabase : RoomDatabase() {
    abstract val chatDao: ChatDao
    abstract val chatParticipantDao: ChatParticipantDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val chatParticipantsCrossRefDao: ChatParticipantsCrossRefDao

    companion object {
        const val DB_NAME = "chirp.db"
    }
}