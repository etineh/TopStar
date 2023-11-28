package com.pixel.chatapp.roomDatabase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.entities.EachUserChats;

import java.util.List;

@Dao
public interface UserChatDao {

    //  ------- users
    @Insert
    void insertUser(UserOnChatUI_Model userOnChatUIModel);
    @Update
    void updateUser(UserOnChatUI_Model userOnChatUIModel);

    @Query("UPDATE usersOnChatUI SET otherUserName = :otherName, imageUrl = :imageUrl WHERE id = :id")
    void updateOtherNameAndPhoto(String id, String otherName, String imageUrl);

    @Query("UPDATE usersOnChatUI SET message = :chat, emojiOnly = :emojiOnly, msgStatus = :statusNum, timeSent = :timeSent WHERE id = :id")
    void updateOutsideChat(String id, String chat, String emojiOnly, int statusNum, long timeSent);

    @Query("UPDATE usersOnChatUI SET msgStatus = :statusNum WHERE id = :otherUid")
    void updateOutsideDelivery(String otherUid, int statusNum);

    @Delete
    void deleteUser(UserOnChatUI_Model userOnChatUIModel);

    // Delete a user based on the id
    @Query("DELETE FROM usersOnChatUI WHERE id = :id")
    void deleteUserById(String id);

    @Query("SELECT * FROM usersOnChatUI ORDER BY timeSent DESC")
    List<UserOnChatUI_Model> getEachUser();



    //  ----------- user chats
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertChat(MessageModel chatModel);

    @Update
    void updateChat(MessageModel chatModel);

    // update emoji reaction field
    @Query("UPDATE chats SET emoji = :emoji WHERE id = :otherId AND idKey = :idKey")
    void updateChatEmoji(String otherId, String idKey, String emoji);

    // update delivery status
    @Query("UPDATE chats SET msgStatus = :msgStatus WHERE id = :otherId AND idKey = :idKey")
    void updateDeliveryStatus(String otherId, String idKey, int msgStatus);

    // update pin icon
    @Query("UPDATE chats SET isChatPin = :status WHERE id = :otherId AND idKey = :idKey")
    void updateChatPin(String otherId, String idKey, boolean status);

    @Delete
    void deleteChat(MessageModel chatModel);

    // Delete a user chats based on the id
    @Query("DELETE FROM chats WHERE id = :id")
    void deleteUserChatsById(String id);

    @Transaction
    @Query("SELECT * FROM chats WHERE id = :id")
    EachUserChats getEachUserChat(String id);


}
