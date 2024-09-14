package com.pixel.chatapp.api.Dao_interface

import com.pixel.chatapp.api.model.SetUpM
import com.pixel.chatapp.api.model.incoming.ResultApiM
import com.pixel.chatapp.api.model.outgoing.AddNewPlayerM
import com.pixel.chatapp.api.model.outgoing.GameSignalM
import com.pixel.chatapp.api.model.outgoing.ThreeValueM
import com.pixel.chatapp.api.model.outgoing.TwoValueM
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

}