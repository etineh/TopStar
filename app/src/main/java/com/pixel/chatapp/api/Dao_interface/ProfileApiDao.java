package com.pixel.chatapp.api.Dao_interface;

import com.pixel.chatapp.api.model.incoming.ResultApiM;
import com.pixel.chatapp.api.model.ThreeValueM;
import com.pixel.chatapp.api.model.TwoValueM;
import com.pixel.chatapp.api.model.SetUpM;

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
