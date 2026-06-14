package com.chatapp.chirp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform