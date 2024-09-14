package com.pixel.chatapp.api.model.outgoing

data class AddNewPlayerM(
    val threeValueM: ThreeValueM,
    val newPlayerDetailsMap: MutableMap<String, Any>
)
