package com.pixel.chatapp;

import android.content.Context;
import android.widget.EditText;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.chats.MessageAdapter;

import java.util.List;

public interface FragmentListener {
//    void onFragmentAction();
//    void logoutOption();

    void msgBodyVisibility(int data, String otherName, String imageUrl, String userName);

    void sendMsgAdapter(MessageAdapter adapter1, int scroll);
//
    void onEditMessage(String itemList, int icon);

    void getLastSeenAndOnline(String otherUid);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods();

//    void onClickOpenMessage(ConstraintLayout itemLayout);

}
