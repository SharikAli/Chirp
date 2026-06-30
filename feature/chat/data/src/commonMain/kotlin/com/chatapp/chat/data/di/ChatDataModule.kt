package com.chatapp.chat.data.di

import com.chatapp.chat.data.chat.KtorChatParticipantService
import com.chatapp.chat.data.chat.KtorChatService
import com.chatapp.chat.domain.chat.ChatParticipantService
import com.chatapp.chat.domain.chat.ChatService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chatDataModule = module {
    singleOf(::KtorChatParticipantService) bind ChatParticipantService::class
    singleOf(::KtorChatService) bind ChatService::class
}