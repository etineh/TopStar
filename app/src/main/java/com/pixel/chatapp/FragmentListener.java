package com.pixel.chatapp;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.chats.MessageAdapter;

public interface FragmentListener {
//    void onFragmentAction();
//    void logoutOption();

    void msgBodyVisibility(int data, String otherName, String imageUrl, String userName, String uID, Context mContext,
                           int position, RecyclerView cy);

    void sendRecyclerView(RecyclerView recyclerChat);

    //
    void onEditMessage(String itemList, int icon);

    void getLastSeenAndOnline(String otherUid);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods();

//    void onClickOpenMessage(ConstraintLayout itemLayout);

}
