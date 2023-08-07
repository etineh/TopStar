package com.pixel.chatapp;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.chats.MessageModel;

import java.util.List;

public interface FragmentListener {
//    void onFragmentAction();
//    void logoutOption();

    void msgBodyVisibility(int data, String otherName, String imageUrl, String userName, String uID);

    void sendRecyclerView(RecyclerView recyclerChat, String otherName, String otherUid);
    void getMessage(String userName, String otherName, String uID, Context mContext);
    void onEditMessage(String itemList, int icon);

    void getLastSeenAndOnline(String otherUid);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods(String otherName, String userName, String uID);

    void onNetworkStatusChanged(boolean isConnected);

//    void onClickOpenMessage(ConstraintLayout itemLayout);

}
