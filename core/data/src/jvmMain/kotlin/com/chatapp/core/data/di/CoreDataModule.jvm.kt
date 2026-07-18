package com.chatapp.core.data.di

import com.chatapp.core.data.auth.createDataStore
import com.chatapp.core.data.preferences.DataStoreThemePreferences
import com.chatapp.core.domain.preferences.ThemePreferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformCoreDataModule = module {
    single { createDataStore() }
    single<HttpClientEngine> { OkHttp.create() }
    singleOf(::DataStoreThemePreferences) bind ThemePreferences::class
}