package com.pixel.chatapp;

import android.content.Context;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ServerValue;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;

import java.util.List;
import java.util.Map;

public interface FragmentListener {
//    void logoutOption();

    void firstCallLoadPage(String otherName);
    void chatBodyVisibility(String otherName, String imageUrl, String userName, String uID, Context mContext_, RecyclerView recyclerChat);

    void sendRecyclerView(RecyclerView recyclerChat, String otherName, String otherUid);

    void getMessage(String userName, String otherName, String uID, Context mContext);

    void getLastSeenAndOnline(String otherUid);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods(String otherName, String userName, String uID);

    void onNetworkStatusChanged(boolean isConnected);

    void onEditOrReplyMessage(String itemList, String editOrReply, String id, long randomID, String status, int icon, String fromWho, int visible);
    void onDeleteMessage(String id, String fromWho, long randomID);
    void onForwardChat(int forwardType_, long forwardRandomID_, String chat);
    void sendPinData(String msgId_, String message_, Object timeStamp_, String pinByWho,
                     MessageAdapter.MessageViewHolder holder);

    void onUserDelete(String otherName, String myUserName, String otherUid);
}
