package com.pixel.chatapp.roomDatabase.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.entities.EachUserChats;
import com.pixel.chatapp.roomDatabase.repositories.UserChatRepository;

import java.util.List;

public class UserChatViewModel extends AndroidViewModel {

    private UserChatRepository repository;

    public UserChatViewModel(@NonNull Application application) {
        super(application);

        repository = new UserChatRepository(application);

    }

    public void insertUser(UserOnChatUI_Model userOnChat){
        repository.insertUser(userOnChat);
    }

    public void updateUser(UserOnChatUI_Model userOnChat){
        repository.updateUser(userOnChat);
    }

    public void updateOutsideDelivery(String otherUid, int statusNum){
        repository.updateOutsideDelivery(otherUid, statusNum);
    }

    public void updateOtherNameAndPhoto(String id, String otherName, String imageUrl){
        repository.updateOtherNameAndPhoto(id, otherName, imageUrl);
    }

    public void updateOutsideChat(String id, String chat, String emojiOnly, int statusNum, long timeSent, String idKey){
        repository.updateOutsideChat(id, chat, emojiOnly, statusNum, timeSent, idKey);
    }

    public void editOutsideChat(String id, String chat, String emojiOnly, String idKey){
        repository.editOutsideChat(id, chat, emojiOnly, idKey);
    }

    public void deleteUserById(String otherId){
        repository.deleteUserById(otherId);
    }

    public List<UserOnChatUI_Model> getAllUsers(){
        return repository.getUsers();
    }


    public void insertChat(String otherId, MessageModel chatModel){
        AllConstants.executors.execute(() -> {
            repository.insertChats(otherId, chatModel);
        });
    }

    public void updatePhotoUriPath(String idKey, String otherId, String photoLowPath,
                                   String photoHighPath, String imageSize, int delivery){
        repository.updatePhotoUriPath(idKey, otherId, photoLowPath, photoHighPath, imageSize, delivery);
    }

    public void updateVoiceNotePath(String otherId, String idKey, String voiceNotePath, String voiceNoteDur)
    {
        repository.updateVoiceNotePath(otherId, idKey, voiceNotePath, voiceNoteDur);
    }

    public void updateChat(MessageModel chatModel){
        repository.updateChats(chatModel);
    }

    public void updateChatEmoji(String otherId, String idKey, String emoji){
        repository.updateChatEmoji(otherId, idKey, emoji);
    }

    public void updateDeliveryStatus(String otherId, String idKey, int deliveryStatus){
        repository.updateDeliveryStatus(otherId, idKey, deliveryStatus);
    }

    public void updateChatPin(String otherId, String idKey, boolean status){
        repository.updateChatPin(otherId, idKey, status);
    }

    public void deleteChat(MessageModel chatModel){
        repository.deleteChats(chatModel);
    }

    public void deleteChatByUserId(String otherId){
        repository.deleteChatsByUserId(otherId);
    }
    public EachUserChats getEachUserChat(String id){
        return repository.getEachUserChats(id);
    }

    // get the chats of each user
//    public List<MessageModel> getEachUserChat_(String userUid){
//        return repository.getEachUserChats_(userUid);
//    }
}












