package com.pixel.chatapp.interface_listeners

interface TriggerOnForward {

    fun openOnForwardView()

    fun openSelectGameOption()

    fun showMinimiseGameAlert(openSignal: Boolean)

    fun proceedToAwaitActivity(stakeAmount: String, gameMode: String, hostNote: String)

}