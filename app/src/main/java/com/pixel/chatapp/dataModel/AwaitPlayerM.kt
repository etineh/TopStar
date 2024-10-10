package com.pixel.chatapp.dataModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AwaitPlayerM(
    val photoUri: String = "null",
    val playerName: String = "",
    val playerUid: String = "",
    val signalUpdate: String = "",
    val micAudio: Boolean = false
) : Parcelable
