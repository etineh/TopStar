package com.pixel.chatapp;

import android.content.Context;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;

import java.util.List;

public interface FragmentListener {
//    void logoutOption();

    void firstCallLoadPage(String otherName);
    void msgBodyVisibility(String otherName, String imageUrl, String userName, String uID);

    void sendRecyclerView(RecyclerView recyclerChat, String otherName, String otherUid);

    void getMessage(String userName, String otherName, String uID, Context mContext);

    void getLastSeenAndOnline(String otherUid);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods(String otherName, String userName, String uID);

    void onNetworkStatusChanged(boolean isConnected);

    void onEditOrReplyMessage(String itemList, String editOrReply, String id, long randomID, String status, int icon, String fromWho, int visible);
    void onDeleteMessage(String id, String fromWho, long randomID);

}
