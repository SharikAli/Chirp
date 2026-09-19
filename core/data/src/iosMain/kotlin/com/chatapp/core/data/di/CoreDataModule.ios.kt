package com.chatapp.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.chatapp.core.data.auth.createDataStore
import com.chatapp.core.data.security.IosSecureStorage
import com.chatapp.core.data.security.SecureStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformCoreDataModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStore<Preferences>> {
        createDataStore()
    }
//    singleOf(::IosSecureStorage) bind SecureStorage::class
}