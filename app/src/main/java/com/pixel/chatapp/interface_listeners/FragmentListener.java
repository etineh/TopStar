package com.pixel.chatapp.interface_listeners;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;

import java.lang.reflect.AccessibleObject;

public interface FragmentListener {

//    void openAndCloseTopCon(boolean open);

    void openChatClickOption(boolean open, MessageModel messageModel, int totalSize, int chatPosition);

    void setDelAndPinForWho(boolean makeVisible, boolean makeTopViewVisible);

    void onLongPressUser(UserOnChatUI_Model userModel);

    void chatBodyVisibility(String otherName, String imageUrl, String userName, String uID, Context mContext_, RecyclerView recyclerChat);

    void sendRecyclerView(RecyclerView recyclerChat, String otherUid);

    void getMessage(String userName, String uID, Context mContext, boolean activateRecycler);

    void getLastSeenAndOnline(String otherUid, Context context);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods(String otherUid, Context context, Activity activity);

    void onNetworkStatusChanged(boolean isConnected);

}
