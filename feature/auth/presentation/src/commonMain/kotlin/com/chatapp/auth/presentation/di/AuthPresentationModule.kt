package com.chatapp.auth.presentation.di

import com.chatapp.auth.presentation.email_verification.EmailVerificationViewModel
import com.chatapp.auth.presentation.forgot_password.ForgotPasswordViewModel
import com.chatapp.auth.presentation.login.LoginViewModel
import com.chatapp.auth.presentation.register.RegisterViewModel
import com.chatapp.auth.presentation.register_success.RegisterSuccessViewModel
import com.chatapp.auth.presentation.reset_password.ResetPasswordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}