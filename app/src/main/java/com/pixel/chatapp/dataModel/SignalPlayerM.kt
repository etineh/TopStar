package com.pixel.chatapp.dataModel

data class SignalPlayerM(
    var fromUid: String = "",
    var gameID: String = "null",
    var gameMode: String = "null",
    var hostNote: String = "",
    var idKey: String = "",
    var message: String = "",
    var numberOfPlayers: String = "",
    var senderName: String = "",
    var stakeAmount: String = "",
    var timeSent: Long = 0L,
    var totalStake: String = "",
//    var join: String = "",
//    var players: MutableList<AwaitPlayerM> = mutableListOf() // Corrected initialization
)

