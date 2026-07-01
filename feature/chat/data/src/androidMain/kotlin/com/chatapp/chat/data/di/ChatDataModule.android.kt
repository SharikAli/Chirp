package com.chatapp.chat.data.di

import com.chatapp.chat.data.lifecycle.AppLifecycleObserver
import com.chatapp.chat.data.network.ConnectionErrorHandler
import com.chatapp.chat.data.network.ConnectivityObserver
import com.chatapp.chat.database.DatabaseFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory(androidContext()) }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)
}