package com.pixel.chatapp;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.model.MessageModel;

public interface FragmentListener {
//    void logoutOption();

    void firstCallLoadPage(String otherUid);
    void chatBodyVisibility(String otherName, String imageUrl, String userName, String uID, Context mContext_, RecyclerView recyclerChat);

    void sendRecyclerView(RecyclerView recyclerChat, String otherUid);

    void getMessage(String userName, String uID, Context mContext);

    void getLastSeenAndOnline(String otherUid);

    void msgBackgroundActivities(String otherUid);

    void callAllMethods(String otherUid);

    void onNetworkStatusChanged(boolean isConnected);

    void onEditOrReplyMessage(MessageModel messageModel, String editOrReply, String status, int icon, String fromWho, int visible);
    void onDeleteMessage(MessageModel messageModel);
    void onForwardChat(int forwardType_, long forwardRandomID_, String chat, String emojiOnly);
    void onPinData(String msgId_, String message_, Object timeStamp_, String pinByWho,
                     MessageAdapter.MessageViewHolder holder);

    void onUserDelete(String otherName, String otherUid);
    void onEmojiReact(MessageAdapter.MessageViewHolder holder, String chatID);
}
