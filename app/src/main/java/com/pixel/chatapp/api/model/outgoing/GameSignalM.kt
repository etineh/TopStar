package com.pixel.chatapp.api.model.outgoing

import com.pixel.chatapp.model.AwaitPlayerM
import java.util.Objects

data class GameSignalM (
    val idToken: String,
    val otherUid: String,
    val dataMap: Map<String, Any>
//    val playersList: MutableList<AwaitPlayerM>
)
