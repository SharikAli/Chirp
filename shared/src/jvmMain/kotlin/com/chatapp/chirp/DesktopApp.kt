package com.chatapp.chirp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.application
import com.chatapp.chirp.deeplink.DesktopDeepLinkHandler
import com.chatapp.chirp.di.desktopModule
import com.chatapp.chirp.di.initKoin
import com.chatapp.chirp.navigation.ExternalUriHandler
import com.chatapp.chirp.theme.rememberAppTheme
import com.chatapp.chirp.windows.ChirpWindow
import org.koin.compose.koinInject

fun desktopApp(args: Array<String>) {
    initKoin {
        modules(desktopModule)
    }

    DesktopDeepLinkHandler.setup()

    val initialDeepLink = args.firstOrNull {
        val cleanedDeepLink = it.trim('"')

        DesktopDeepLinkHandler.supportedUriPatterns.any { it.matches(cleanedDeepLink) }
    }?.trim('"')

    application {
        val applicationStateHolder = koinInject<ApplicationStateHolder>()
        val applicationState by applicationStateHolder.state.collectAsState()
        val windows = applicationState.windows

        var canReceiveDeepLink by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(canReceiveDeepLink) {
            if (canReceiveDeepLink && initialDeepLink != null) {
                ExternalUriHandler.onNewUri(initialDeepLink)
            }
        }

        LaunchedEffect(windows) {
            if (windows.isEmpty()) {
                exitApplication()
            }
        }

        val appTheme = rememberAppTheme(applicationState.themePreference)

        for (window in windows) {
            key(window.id) {
                ChirpWindow(
                    appTheme = appTheme,
                    onCloseRequest = {
                        applicationStateHolder.onWindowCloseRequest(window.id)
                    },
                    onAddWindowClick = applicationStateHolder::onAddWindowClick,
                    onFocusChanged = { focused ->
                        applicationStateHolder.onWindowFocusChanged(window.id, focused)
                    },
                    onDeepLinkListenerSetup = {
                        canReceiveDeepLink = true
                    }
                )
            }
        }

        ChirpTrayMenu(
            state = applicationState.trayState,
            themePreferenceFromAppSettings = applicationState.themePreference,
            onThemePreferenceClick = applicationStateHolder::onThemePreferenceClick
        )
    }
}