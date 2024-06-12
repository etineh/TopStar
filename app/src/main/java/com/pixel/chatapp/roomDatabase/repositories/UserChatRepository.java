package com.pixel.chatapp.roomDatabase.repositories;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.dao.UserChatDao;
import com.pixel.chatapp.roomDatabase.database.TopStarDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserChatRepository {

    private UserChatDao userChatDao;

    private List<UserOnChatUI_Model> users;

    ExecutorService executors = Executors.newSingleThreadExecutor();

    public UserChatRepository(Application application){
        TopStarDatabase database = TopStarDatabase.getInstance(application);
        userChatDao = database.userChatDao();
//        users = userChatDao.getEachUser();
    }

    public void insertUser(UserOnChatUI_Model userOnChatUIModel){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    userChatDao.insertUser(userOnChatUIModel);
                } catch (SQLiteConstraintException e) {
                    // Handle the unique constraint violation
                    userChatDao.updateUser(userOnChatUIModel);
                }
            }
        });
    }

    public void updateUser(UserOnChatUI_Model userOnChatUIModel){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateUser(userOnChatUIModel);
            }
        });
    }

    public void updateOutsideDelivery(String otherUid, int statusNum){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateOutsideDelivery(otherUid, statusNum);
            }
        });
    }

    public void updateOtherNameAndPhoto(String id, String otherUsername, String otherDisplayName, String otherContactName, String imageUrl){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateOtherNameAndPhoto(id, otherUsername, otherDisplayName, otherContactName, imageUrl);
            }
        });
    }

    public void deleteUserById(String otherId){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.deleteUserById(otherId);
            }
        });
    }

    public void updateOutsideChat(String id, String chat, String emojiOnly, int statusNum, long timeSent, String idKey){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateOutsideChat(id, chat, emojiOnly, statusNum, timeSent, idKey);
            }
        });
    }

    public void editOutsideChat(String id, String chat, String emojiOnly, String idKey ){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.editOutsideChat(id, chat, emojiOnly, idKey);
            }
        });
    }



    //  ---------   inside chat ----------------
    public void insertChats(String userId, MessageModel chatModel){

        executors.execute(() -> {
            chatModel.setId(userId);
            userChatDao.insertChat(chatModel);
        });

    }

    public void updatePhotoUriPath( String idKey, String otherId, String photoLowPath,
                                    String photoHighPath, String imageSize, int delivery ){
        executors.execute(() -> {
            userChatDao.updatePhotoUriPath(idKey, otherId, photoLowPath, photoHighPath, imageSize, delivery);
        });
    }

    public void updateVoiceNotePath(String otherId, String idKey, String voiceNotePath, String voiceNoteDur)
    {
        executors.execute(() -> {
            userChatDao.updateVoiceNotePath(otherId, idKey, voiceNotePath, voiceNoteDur);
        });
    }

    public void updateChats(MessageModel chatModel){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateChat(chatModel);
            }
        });

    }

    public void updateChatEmoji(String otherId, String idKey, String emoji){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateChatEmoji(otherId, idKey, emoji);
            }
        });

    }

    public void updateChatPin(String otherId, String idKey, boolean status){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateChatPin(otherId, idKey, status);
            }
        });

    }

    public void updateDeliveryStatus(String otherId, String idKey, int deliveryStatus){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateDeliveryStatus(otherId, idKey, deliveryStatus);
            }
        });

    }

    public void deleteChats(MessageModel chatModel){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.deleteChat(chatModel);
            }
        });

    }

    public void deleteChatsByUserId(String otherId, String myId){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.deleteUserChatsById(otherId, myId);
            }
        });

    }

    public List<UserOnChatUI_Model> getUsers(String myUid){
        return userChatDao.getEachUser(myUid);
    }

    // get all the chats of each user
    public List<MessageModel> getEachUserChats_(String userUID, String myUid){
        return userChatDao.getEachUserChat_(userUID, myUid);
    }

}












