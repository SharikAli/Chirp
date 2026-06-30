package com.chatapp.chat.presentation.di

import com.chatapp.chat.presentation.chat_detail.ChatDetailViewModel
import com.chatapp.chat.presentation.chat_list.ChatListViewModel
import com.chatapp.chat.presentation.chat_list_detail.ChatListDetailViewModel
import com.chatapp.chat.presentation.create_chat.CreateChatViewModel
import com.chatapp.chat.presentation.manage_chat.ManageChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatPresentationModule = module {
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ChatListDetailViewModel)
    viewModelOf(::CreateChatViewModel)
    viewModelOf(::ChatDetailViewModel)
    viewModelOf(::ManageChatViewModel)
}