package com.chatapp.chirp

sealed interface MainEvent {
    data object OnSessionExpired: MainEvent
}