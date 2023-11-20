package com.pixel.chatapp.roomDatabase.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.dao.UserChatDao;
import com.pixel.chatapp.roomDatabase.database.WinnerDatabase;
import com.pixel.chatapp.roomDatabase.entities.EachUserChats;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserChatRepository {

    private UserChatDao userChatDao;

    private List<UserOnChatUI_Model> users;

    ExecutorService executors = Executors.newSingleThreadExecutor();

    public UserChatRepository(Application application){
        WinnerDatabase database = WinnerDatabase.getInstance(application);
        userChatDao = database.userChatDao();
//        users = userChatDao.getEachUser();
    }

    public void insertUser(UserOnChatUI_Model userOnChatUIModel){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.insertUser(userOnChatUIModel);
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

    public void updateOtherNameAndPhoto(String id, String otherName, String imageUrl){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateOtherNameAndPhoto(id, otherName, imageUrl);
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

    public void updateOutsideChat(String id, String chat, String emojiOnly, int statusNum, long timeSent){
        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.updateOutsideChat(id, chat, emojiOnly, statusNum, timeSent);
            }
        });
    }



    //  ---------   inside chat ----------------
    public void insertChats(String userId, MessageModel chatModel){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                chatModel.setId(userId);
                userChatDao.insertChat(chatModel);
            }
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

    public void deleteChats(MessageModel chatModel){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.deleteChat(chatModel);
            }
        });

    }

    public void deleteChatsByUserId(String otherId){

        executors.execute(new Runnable() {
            @Override
            public void run() {
                userChatDao.deleteUserChatsById(otherId);
            }
        });

    }

    public EachUserChats getEachUserChats(String userId){
        return userChatDao.getEachUserChat(userId);
    }

    public List<UserOnChatUI_Model> getUsers(){
        return userChatDao.getEachUser();
    }
}












