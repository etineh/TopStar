package com.pixel.chatapp.model

data class AwaitPlayerM(
    val photoUri: String = "null",
    val playerName: String = "",
    val playerUid: String = "",
    val signalUpdate: String = "",
    val micAudio: Boolean = false
)
