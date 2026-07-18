package com.chatapp.chirp

import androidx.compose.ui.window.TrayState
import com.chatapp.chirp.windows.WindowState
import com.chatapp.core.domain.preferences.ThemePreference

data class ApplicationState(
    val windows: List<WindowState> = listOf(WindowState()),
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val trayState: TrayState = TrayState()
)