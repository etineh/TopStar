package com.pixel.chatapp.services.api.dao_interface;

import com.pixel.chatapp.services.api.model.outgoing.ChatNotificationM;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotificationDao {

    @POST("/api/notification/chat")
    Call<Void> chatNotify(@Body ChatNotificationM chatNotificationM);


}
