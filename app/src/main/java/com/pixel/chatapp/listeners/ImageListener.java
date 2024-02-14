package com.pixel.chatapp.listeners;

import android.net.Uri;

import com.pixel.chatapp.model.MessageModel;

public interface ImageListener {

    void sendImageData(Uri imageUriPath);

    void getCurrentModelChat(MessageModel messageModel, int position);

    void onColorChange(int color);

}
