package com.pixel.chatapp.services.roomDatabase.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.pixel.chatapp.constants.K;
import com.pixel.chatapp.dataModel.MessageModel;
import com.pixel.chatapp.dataModel.UserOnChatUI_Model;
import com.pixel.chatapp.services.roomDatabase.repositories.UserChatRepository;

import java.util.List;

public class UserChatViewModel extends AndroidViewModel {

    private UserChatRepository repository;

    public UserChatViewModel(@NonNull Application application) {
        super(application);

        repository = new UserChatRepository(application);

    }

    //   =======   users on outside chats

    public void insertUser(UserOnChatUI_Model userOnChat){
        repository.insertUser(userOnChat);
    }

    public void updateUser(UserOnChatUI_Model userOnChat){
        repository.updateUser(userOnChat);
    }

    public void updateUserCallOrGame(String otherUid, String myUid, String text){
        repository.updateUserCallOrGame(otherUid, myUid, text);
    }

    public void updateOutsideDelivery(String otherUid, int statusNum){
        repository.updateOutsideDelivery(otherUid, statusNum);
    }

    public void updateOtherNameAndPhoto(String id, String otherUsername, String otherDisplayName,
                                        String otherContactName, String imageUrl){
        repository.updateOtherNameAndPhoto(id, otherUsername, otherDisplayName, otherContactName, imageUrl);
    }

    public void updateOutsideChat(String id, String chat, String emojiOnly, int statusNum, long timeSent, String idKey, int type){
        repository.updateOutsideChat(id, chat, emojiOnly, statusNum, timeSent, idKey, type);
    }

    public void editOutsideChat(String id, String chat, String emojiOnly, String idKey){
        repository.editOutsideChat(id, chat, emojiOnly, idKey);
    }

    public void deleteUserById(String otherId){
        repository.deleteUserById(otherId);
    }

    public List<UserOnChatUI_Model> getAllUsers(String myUid){
        return repository.getUsers(myUid);
    }


    //   =======   chats

    public void insertChat(String otherId, MessageModel chatModel){
        K.executors.execute(() -> {
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

    public void deleteChatByUserId(String otherId, String myId){
        repository.deleteChatsByUserId(otherId, myId);
    }

    // get the chats of each user
    public List<MessageModel> getEachUserChat_(String userUid, String myUid){
        return repository.getEachUserChats_(userUid, myUid);
    }

}












