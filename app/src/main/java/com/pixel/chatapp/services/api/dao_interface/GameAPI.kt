package com.pixel.chatapp.services.api.dao_interface

import com.pixel.chatapp.services.api.model.incoming.ResultApiM
import com.pixel.chatapp.services.api.model.outgoing.AddNewPlayerM
import com.pixel.chatapp.services.api.model.outgoing.GameSignalM
import com.pixel.chatapp.services.api.model.outgoing.ThreeValueM
import com.pixel.chatapp.services.api.model.outgoing.TwoValueM
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

interface GameAPI {

    @POST("/game/signal")
    fun signal(@Body gameSignalM: GameSignalM): Call<Void>

//    @PUT("/game/signal")
//    suspend fun updateSignal(@Body twoValueM: TwoValueM): Response<ResultApiM>

    @PUT("/game/signal")
    fun updateSignal(@Body addNewPlayerM: AddNewPlayerM): Call<Void>

//    @PATCH("/game/signal")
//    suspend fun removeSignal_(@Body threeValueM: ThreeValueM): Response<Void>

    @PATCH("/game/signal")
    fun removeSignal(@Body threeValueM: ThreeValueM): Call<Void>


    @POST("/game/join")
    fun join(@Body twoValueM: TwoValueM): Call<ResultApiM>

    @PUT("/game/join")
    fun await(@Body twoValueM: TwoValueM): Call<Void>

//    @GET("/game/join/{idToken}")
//    fun join(@Path("idToken") idToken: String): Call<ResultApiM>

    @POST("/game/startGame")
    fun startGame(@Body twoValueM: TwoValueM): Call<ResultApiM>


    @POST("/game/quit")
    fun quitGame(@Body threeValueM: ThreeValueM): Call<Void>



}