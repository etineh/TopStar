package com.pixel.chatapp.services.api.dao_interface;

import com.pixel.chatapp.services.api.model.incoming.ResultApiM;
import com.pixel.chatapp.services.api.model.outgoing.ThreeValueM;
import com.pixel.chatapp.services.api.model.outgoing.TwoValueM;
import com.pixel.chatapp.services.api.model.SetUpM;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ProfileApiDao {

    @POST("/setting/profile/setup")
    Call<Void> setUp(@Body SetUpM setUpM);

    @POST("/setting/profile/email")
    Call<Void> updateEmail(@Body ThreeValueM valueM);

    @POST("/setting/profile/number")
    Call<ResultApiM> number(@Body ThreeValueM valueM);


    @POST("/setting/profile/username")
    Call<ResultApiM> username(@Body TwoValueM valueM);



}
