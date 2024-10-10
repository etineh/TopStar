package com.pixel.chatapp.services.api.model.outgoing

data class GameSignalM (
    val idToken: String,
    val otherUid: String,
    val dataMap: Map<String, Any>
//    val playersList: MutableList<AwaitPlayerM>
)
