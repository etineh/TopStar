package com.pixel.chatapp.interface_listeners;

import android.net.Uri;

import com.pixel.chatapp.dataModel.MessageModel;

public interface ImageListener {

    void sendImageData(Uri imageUriPath);

    void getCurrentModelChat(MessageModel messageModel, int position);

    void onColorChange(int color);

    void onImageClick();

}
