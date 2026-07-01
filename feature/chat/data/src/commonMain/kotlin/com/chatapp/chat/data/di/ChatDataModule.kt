package com.chatapp.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.chatapp.chat.data.chat.KtorChatParticipantService
import com.chatapp.chat.data.chat.KtorChatService
import com.chatapp.chat.data.chat.OfflineFirstChatRepository
import com.chatapp.chat.data.chat.WebSocketChatConnectionClient
import com.chatapp.chat.data.message.KtorChatMessageService
import com.chatapp.chat.data.message.OfflineFirstMessageRepository
import com.chatapp.chat.data.network.ConnectionRetryHandler
import com.chatapp.chat.data.network.KtorWebSocketConnector
import com.chatapp.chat.database.DatabaseFactory
import com.chatapp.chat.domain.chat.ChatConnectionClient
import com.chatapp.chat.domain.chat.ChatParticipantService
import com.chatapp.chat.domain.chat.ChatRepository
import com.chatapp.chat.domain.chat.ChatService
import com.chatapp.chat.domain.message.ChatMessageService
import com.chatapp.chat.domain.message.MessageRepository
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module

val chatDataModule = module {
    includes(platformChatDataModule)

    singleOf(::KtorChatParticipantService) bind ChatParticipantService::class
    singleOf(::KtorChatService) bind ChatService::class
    singleOf(::OfflineFirstChatRepository) bind ChatRepository::class
    singleOf(::OfflineFirstMessageRepository) bind MessageRepository::class
    singleOf(::WebSocketChatConnectionClient) bind ChatConnectionClient::class
    singleOf(::ConnectionRetryHandler)
    singleOf(::KtorWebSocketConnector)
    singleOf(::KtorChatMessageService) bind ChatMessageService::class
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}