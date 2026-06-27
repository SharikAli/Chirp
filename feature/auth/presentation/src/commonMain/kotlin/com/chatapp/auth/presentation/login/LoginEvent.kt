package com.chatapp.auth.presentation.login

sealed interface LoginEvent {
    data object Success: LoginEvent
}