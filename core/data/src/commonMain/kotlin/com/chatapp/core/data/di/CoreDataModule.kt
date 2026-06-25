package com.chatapp.core.data.di

import com.chatapp.core.data.auth.KtorAuthService
import com.chatapp.core.data.logging.KermitLogger
import com.chatapp.core.data.network.HttpClientFactory
import com.chatapp.core.domain.auth.AuthService
import com.chatapp.core.domain.logging.ChirpLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<ChirpLogger> { KermitLogger }
    single {
        HttpClientFactory(get()).create(get())
    }
    singleOf(::KtorAuthService) bind AuthService::class
}