package com.chatapp.chirp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chatapp.auth.presentation.register.RegisterRoot
import com.chatapp.chirp.navigation.NavigationRoot
import com.chatapp.core.designsystem.theme.ChirpTheme

@Composable
@Preview
fun App() {
    ChirpTheme {
        NavigationRoot()
    }
}