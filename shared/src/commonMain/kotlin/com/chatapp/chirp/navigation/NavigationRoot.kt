package com.chatapp.chirp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.chatapp.auth.presentation.navigation.AuthGraphRoutes
import com.chatapp.auth.presentation.navigation.authGraph

@Composable
fun NavigationRoot(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AuthGraphRoutes.Graph
    ) {
        authGraph(
            navController = navController,
            onLoginSuccess = {

            }
        )
    }
}