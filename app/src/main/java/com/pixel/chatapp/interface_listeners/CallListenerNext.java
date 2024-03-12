package com.pixel.chatapp.interface_listeners;

public interface CallListenerNext {

    void endCall();
    void callConnected(String return_duration);
    void returnToCallLayoutVisibilty();
    void isConnecting(String return_duration);

    void busyCall();
}
