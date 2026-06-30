package com.chatapp.chat.database

import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<ChirpChatDatabase> {
        return Room.databaseBuilder(ChirpChatDatabase.DB_NAME)
    }
}