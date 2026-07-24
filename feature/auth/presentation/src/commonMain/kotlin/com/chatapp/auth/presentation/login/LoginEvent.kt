package com.chatapp.auth.presentation.login

sealed interface LoginEvent {
    data object Success: LoginEvent
    data class NavigateToEmailVerificationScreen(val email: String): LoginEvent
}