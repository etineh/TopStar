package com.pixel.chatapp.api.Dao_interface;

import com.pixel.chatapp.api.model.LoginDetailM;
import com.pixel.chatapp.api.model.ResultApiM;
import com.pixel.chatapp.api.model.UserSearchM;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserDao {

    @GET("user/{phone}")
    Call<UserSearchM> fineUser(@Path("phone") String phone);

    @POST("user/login")
    Call<ResultApiM> login(@Body LoginDetailM loginDetailM);

}
