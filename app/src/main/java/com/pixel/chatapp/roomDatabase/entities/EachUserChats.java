package com.pixel.chatapp.roomDatabase.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;

import java.util.List;

public class EachUserChats {

    @Embedded
    public UserOnChatUI_Model userOnChatUI;

    @Relation(parentColumn = "id", entityColumn = "id")
    public List<MessageModel> userChatList;

}
