package com.pixel.chatapp.roomDatabase.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;

import java.util.List;

@Dao
public interface UserChatDao {

    //  ------- users
    @Insert
    void insertUser(UserOnChatUI_Model userOnChatUIModel);
    @Update
    void updateUser(UserOnChatUI_Model userOnChatUIModel);

    @Query("UPDATE usersOnChatUI SET message = :text WHERE otherUid = :otherUid AND myUid = :myUid")
    void updateCallOrGame(String otherUid, String myUid, String text);

    @Query("UPDATE usersOnChatUI SET otherUserName = :otherUsername, otherDisplayName = :otherDisplayName, " +
            "otherContactName = :contactName, imageUrl = :imageUrl WHERE otherUid = :otherUid")
    void updateOtherNameAndPhoto(String otherUid, String otherUsername, String otherDisplayName, String contactName, String imageUrl);

    @Query("UPDATE usersOnChatUI SET message = :chat, emojiOnly = :emojiOnly, msgStatus = :statusNum, " +
            "timeSent = :timeSent, idKey = :idKey, type = :type WHERE otherUid = :otherUid")
    void updateOutsideChat(String otherUid, String chat, String emojiOnly, int statusNum, long timeSent, String idKey, int type);

    @Query("UPDATE usersOnChatUI SET message = :chat, emojiOnly = :emojiOnly WHERE otherUid = :otherUid AND idKey = :idKey")
    void editOutsideChat(String otherUid, String chat, String emojiOnly, String idKey);

    @Query("UPDATE usersOnChatUI SET msgStatus = :statusNum WHERE otherUid = :otherUid")
    void updateOutsideDelivery(String otherUid, int statusNum);

    @Delete
    void deleteUser(UserOnChatUI_Model userOnChatUIModel);

    // Delete a user based on the id
    @Query("DELETE FROM usersOnChatUI WHERE otherUid = :otherUid")
    void deleteUserById(String otherUid);

    @Query("SELECT * FROM usersOnChatUI WHERE myUid = :myUid ORDER BY timeSent DESC")
    List<UserOnChatUI_Model> getEachUser(String myUid);



    //  ==========================   user chats     ==========================

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertChat(MessageModel chatModel);

    @Update
    void updateChat(MessageModel chatModel);

    // update emoji reaction field
    @Query("UPDATE chats SET emoji = :emoji WHERE otherUid = :otherId AND idKey = :idKey")
    void updateChatEmoji(String otherId, String idKey, String emoji);

    // update delivery status
    @Query("UPDATE chats SET msgStatus = :msgStatus WHERE otherUid = :otherId AND idKey = :idKey")
    void updateDeliveryStatus(String otherId, String idKey, int msgStatus);

    // update pin icon
    @Query("UPDATE chats SET chatIsPin = :status WHERE otherUid = :otherId AND idKey = :idKey")
    void updateChatPin(String otherId, String idKey, boolean status);

    // update image path field
    @Query("UPDATE chats SET photoUriPath = :photoLowPath, photoUriOriginal = :photoHighPath, " +
            "imageSize = :imageSize, msgStatus = :delivery WHERE otherUid = :otherId AND idKey = :idKey")
    void updatePhotoUriPath( String idKey, String otherId, String photoLowPath,
                             String photoHighPath, String imageSize, int delivery );

    // update voice note download
    @Query("UPDATE chats SET voiceNote = :voiceNotePath, vnDuration = :voiceNoteDur WHERE otherUid = :otherId AND idKey = :idKey")
    void updateVoiceNotePath(String otherId, String idKey, String voiceNotePath, String voiceNoteDur);


    @Delete
    void deleteChat(MessageModel chatModel);

    // Delete a user chats based on the id (otherID)
    @Query("DELETE FROM chats WHERE otherUid = :otherId AND myUid = :myId")
    void deleteUserChatsById(String otherId, String myId);

    // get the chats of each user based on the id
    @Query("SELECT * FROM chats WHERE otherUid = :otherUid AND myUid = :myId")
    List<MessageModel> getEachUserChat_(String otherUid, String myId);

}
