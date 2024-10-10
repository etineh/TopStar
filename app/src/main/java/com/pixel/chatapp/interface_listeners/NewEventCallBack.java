package com.pixel.chatapp.interface_listeners;

import com.pixel.chatapp.dataModel.CallModel;

public interface NewEventCallBack {
    void onNewEventReceived(CallModel model);
}
