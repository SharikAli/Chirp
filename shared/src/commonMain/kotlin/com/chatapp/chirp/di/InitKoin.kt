package com.chatapp.chirp.di

import com.chatapp.auth.presentation.di.authPresentationModule
import com.chatapp.chat.presentation.di.chatPresentationModule
import com.chatapp.core.data.di.coreDataModule
import com.chatapp.core.presentation.di.corePresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            authPresentationModule,
            appModule,
            chatPresentationModule,
            corePresentationModule
        )
    }
}