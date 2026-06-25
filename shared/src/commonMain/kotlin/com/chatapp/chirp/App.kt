package com.chatapp.chirp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.chatapp.auth.presentation.register.RegisterRoot
import com.chatapp.chirp.navigation.DeepLinkListener
import com.chatapp.chirp.navigation.NavigationRoot
import com.chatapp.core.designsystem.theme.ChirpTheme

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    DeepLinkListener(navController)

    ChirpTheme {
        NavigationRoot(navController)
    }
}