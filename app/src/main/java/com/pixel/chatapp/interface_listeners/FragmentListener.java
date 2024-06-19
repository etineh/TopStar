package com.pixel.chatapp.interface_listeners;

import android.app.Activity;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.model.MessageModel;

import java.lang.reflect.AccessibleObject;

public interface FragmentListener {

    //    void logoutOption();

//    void onRequestPermission(MessageAdapter.MessageViewHolder holder, MessageModel modelChats);

    void firstCallLoadPage(String otherUid);
    void chatBodyVisibility(String otherName, String imageUrl, String userName, String uID, Context mContext_, RecyclerView recyclerChat);

    void sendRecyclerView(RecyclerView recyclerChat, String otherUid);

    void getMessage(String userName, String uID, Context mContext, boolean activateRecycler);

    void getLastSeenAndOnline(String otherUid, Context context);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods(String otherUid, Context context, Activity activity);

    void onNetworkStatusChanged(boolean isConnected);

    void onEditOrReplyMessage(MessageModel messageModel, String editOrReply, String status, int icon,
                              int visible, MessageAdapter.MessageViewHolder holder);
    void onDeleteMessage();
    void onForwardChat();
    void onPinData(String msgId_, String message_, Object timeStamp_, MessageAdapter.MessageViewHolder holder);

    void onUserDelete(String otherName, String otherUid);
    void onEmojiReact(MessageAdapter.MessageViewHolder holder, String chatID);
}
