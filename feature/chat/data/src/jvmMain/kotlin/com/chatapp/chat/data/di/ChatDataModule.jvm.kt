package com.chatapp.chat.data.di

import com.chatapp.chat.data.lifecycle.AppLifecycleObserver
import com.chatapp.chat.data.network.ConnectionErrorHandler
import com.chatapp.chat.data.network.ConnectivityObserver
import com.chatapp.chat.data.notification.FirebasePushNotificationService
import com.chatapp.chat.database.DatabaseFactory
import com.chatapp.chat.domain.notification.PushNotificationService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
    singleOf(::ConnectionErrorHandler)

    singleOf(::FirebasePushNotificationService) bind PushNotificationService::class
}