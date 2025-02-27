package com.pixel.chatapp.utilities

import android.content.Context
import com.pixel.chatapp.services.api.dao_interface.GameAPI
import com.pixel.chatapp.services.api.model.outgoing.AddNewPlayerM
import com.pixel.chatapp.services.api.model.outgoing.ThreeValueM
import com.pixel.chatapp.constants.Ki
import com.pixel.chatapp.dataModel.AwaitPlayerM
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameUtils {

    companion object {
        @JvmStatic
        fun rejectGameOrAddNewPlayer(
            context: Context,
            hostUid: String, updateState: String?,
            newPlayerDetailsMap: MutableMap<String, Any>?,
            rejectGameInterface: RejectGameInterface
        ) {
            val gameAPI = Ki.retrofit.create(GameAPI::class.java)

            IdTokenUtil.generateToken ({ token: String? ->

                val threeValueM = ThreeValueM(token, hostUid, updateState?: "null")
                val addNewPlayerM = AddNewPlayerM(threeValueM, newPlayerDetailsMap?: mutableMapOf())

                gameAPI.updateSignal(addNewPlayerM).enqueue(object : Callback<Void?> {
                    override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                        if (response.isSuccessful) {
                            rejectGameInterface.onSuccess()
                        }
                    }

                    override fun onFailure(call: Call<Void?>, throwable: Throwable) {
                        rejectGameInterface.onFailure()
                    }
                })
            }, context)
        }

        @JvmStatic
        fun removePlayer(context: Context?, hostUid: String, rejectPlayerUid: String, rejectGameInterface: RejectGameInterface)
        {
            val gameAPI = Ki.retrofit.create(GameAPI::class.java)

            IdTokenUtil.generateToken ({token->
                val threeValueM = ThreeValueM(token, hostUid, rejectPlayerUid)

                gameAPI.removeSignal(threeValueM).enqueue(object : Callback<Void>{
                    override fun onResponse(p0: Call<Void>, p1: Response<Void>) {
                        if(p1.isSuccessful)  rejectGameInterface.onSuccess()
                    }

                    override fun onFailure(p0: Call<Void>, p1: Throwable) {
                        rejectGameInterface.onFailure()
                    }
                })
            }, context)

        }

        @JvmStatic
        fun getEachPlayerMap(selectedPlayerMList: List<AwaitPlayerM>): Map<String, Map<String, Any>> {
            val playersMap = mutableMapOf<String, MutableMap<String, Any>>()

            for (player in selectedPlayerMList) {
                val playerMap = mutableMapOf<String, Any>()
                playerMap["photoUri"] = player.photoUri
                playerMap["playerName"] = player.playerName
                playerMap["playerUid"] = player.playerUid
                playerMap["signalUpdate"] = player.signalUpdate
                playerMap["micAudio"] = player.micAudio
                playersMap[player.playerUid] = playerMap  // Use player UID as the key
            }

            return playersMap
        }

        @JvmStatic
        fun newPlayerMap(selectedPlayerMList: List<AwaitPlayerM>): Map<String, Any> {
            val playerMap = mutableMapOf<String, Any>()

            for (player in selectedPlayerMList) {
                playerMap["photoUri"] = player.photoUri
                playerMap["playerName"] = player.playerName
                playerMap["playerUid"] = player.playerUid
                playerMap["signalUpdate"] = player.signalUpdate
                playerMap["micAudio"] = player.micAudio
            }

            return playerMap
        }

        @JvmStatic
        fun gameId(): String {
            return "ts_" + System.currentTimeMillis().toString(36) +
                    (Math.random() * 1_000_000).toInt().toString(36)
        }


//        @JvmStatic
//        fun removePlayer_(hostUid: String, rejectPlayerUid: String, rejectGameInterface: RejectGameInterface) {
//
//            val gameAPI = K.retrofit.create(GameAPI::class.java)
//
//            IdTokenUtil.generateToken { idToken ->
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        val threeValueM = ThreeValueM(idToken, hostUid, rejectPlayerUid)
//                        val response = gameAPI.removeSignal_(threeValueM)
//                        if (response.isSuccessful) {
//                            // Handle success
//                            rejectGameInterface.onRejectSuccess()
//                        } else {
//                            // Handle failure response
//                            rejectGameInterface.onRejectFailure()
//                        }
//                    } catch (e: Exception) {
//                        // Handle network or conversion error
//                        rejectGameInterface.onRejectFailure()
//                    }
//                }
//            }
//        }


    }


    interface RejectGameInterface{
        fun onSuccess()

        fun onFailure()
    }

}