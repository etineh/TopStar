package com.pixel.chatapp.interface_listeners;

import com.pixel.chatapp.model.CallModel;

public interface CallsListener {

    void getRequestVideoCall(CallModel callModel);
    void acceptVideoCall();
    void getRejectVideoCallResp(CallModel callModel);
    void myUserOffCamera();

}
