package com.pixel.chatapp;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.chats.MessageAdapter;

public interface FragmentListener {
//    void onFragmentAction();
//    void logoutOption();

    void msgBodyVisibility(int data, String otherName, String imageUrl, String userName, String uID, Context mContext);

    void sendMsgAdapter(MessageAdapter adapter1, int scroll);

    //
    void onEditMessage(String itemList, int icon);

    void getLastSeenAndOnline(String otherUid);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods();

//    void onClickOpenMessage(ConstraintLayout itemLayout);

}
