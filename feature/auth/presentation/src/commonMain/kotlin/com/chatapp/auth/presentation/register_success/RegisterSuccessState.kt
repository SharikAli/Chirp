package com.chatapp.auth.presentation.register_success

data class RegisterSuccessState(
    val registeredEmail: String = "",
    val isResendingVerificationEmail: Boolean = false
)