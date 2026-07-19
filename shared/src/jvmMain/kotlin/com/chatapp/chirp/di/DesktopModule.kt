package com.chatapp.chirp.di

import com.chatapp.chirp.ApplicationStateHolder
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val desktopModule = module {
    singleOf(::ApplicationStateHolder)
}