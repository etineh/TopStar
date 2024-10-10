package com.pixel.chatapp.services.roomDatabase.repositories;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import com.pixel.chatapp.dataModel.MessageModel;
import com.pixel.chatapp.dataModel.UserOnChatUI_Model;
import com.pixel.chatapp.services.roomDatabase.dao.UserChatDao;
import com.pixel.chatapp.services.roomDatabase.database.TopStarDatabase;

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

    public void updateUserCallOrGame(String otherUid, String myUid, String text){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateCallOrGame(otherUid, myUid, text);
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

    public void updateOutsideChat(String id, String chat, String emojiOnly, int statusNum, long timeSent, String idKey, int type){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateOutsideChat(id, chat, emojiOnly, statusNum, timeSent, idKey, type);
            }
        });
    }

    public void editOutsideChat(String id, String chat, String emojiOnly, String idKey){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.editOutsideChat(id, chat, emojiOnly, idKey);
            }
        });
    }

    public List<UserOnChatUI_Model> getUsers(String myUid){
        return userChatDao.getEachUser(myUid);
    }

    public UserOnChatUI_Model findUserByUid(String otherUid, String myUid){
        return userChatDao.findUserByUid(otherUid, myUid);
    }

    //  ---------   inside chat ----------------
    public void insertChats(String userId, MessageModel chatModel){

        executors.execute(() -> {
            if(chatModel.getIdKey() != null){
                chatModel.setOtherUid(userId);
                userChatDao.insertChat(chatModel);
            }
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

    public MessageModel findNewChatNumber(String otherId, String myId, int type, String yes)
    {
        return userChatDao.findNewChatNumber(otherId, myId, type, yes);
    }

    // get all the chats of each user
    public List<MessageModel> getEachUserChats_(String userUID, String myUid){
        return userChatDao.getEachUserChat_(userUID, myUid);
    }

}












