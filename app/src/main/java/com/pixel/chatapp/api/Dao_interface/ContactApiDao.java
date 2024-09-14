package com.pixel.chatapp.api.Dao_interface;

import com.pixel.chatapp.api.model.incoming.UserSearchM;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ContactApiDao {

    @POST("/contact/numbers")
    Call<List<UserSearchM>> contacts(@Body List<String> contactList);


}
