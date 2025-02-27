package com.pixel.chatapp.services.notification;

import static com.pixel.chatapp.view_controller.MainActivity.adapterMap;

import android.app.Application;
import androidx.annotation.NonNull;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pixel.chatapp.constants.Kc;
import com.pixel.chatapp.constants.Ki;
import com.pixel.chatapp.dataModel.MessageModel;
import com.pixel.chatapp.dataModel.UserOnChatUI_Model;
import com.pixel.chatapp.services.roomDatabase.repositories.UserChatRepository;
import com.pixel.chatapp.utilities.ChatUtils;

import java.util.Map;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

//    private static final String TAG = Ki.FIREBASE_SERVICE;
    private static final String myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private static final DatabaseReference refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
    private static final DatabaseReference refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");
//    private final static DatabaseReference refUsersLast = FirebaseDatabase.getInstance().getReference("UsersList");

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences contactNameShareRef = getSharedPreferences(Ki.CONTACTNAME, Context.MODE_PRIVATE);

        Map<String, String> data = remoteMessage.getData();

        // Handle the data payload (if any)
        if (data.size() > 0)
        {
            MessageModel messageModel = messageModelMethod(data);   // get the message model

            String otherUid = messageModel.getFromUid();
            String senderName = messageModel.getSenderName();

//            ReplyReceiver.contextFMS = this;

            String title = contactNameShareRef.getString(otherUid, senderName);
            String body = messageModel.getMessage();

            // If there's new message from otherUser, add here to adapter UI
            if(messageModel.getFromUid() != null)
            {
                Kc.executor.execute(()->
                {
                    if(adapterMap != null && adapterMap.get(otherUid) != null){
                        if(messageModel.getIdKey() != null) {
                            ChatUtils.getChatFromOtherUser(messageModel, otherUid, adapterMap.get(otherUid), this, "notify");
                        }
                    } else {
                        getChatWhenAppIsNotActive(this, messageModel, otherUid);
                    }
                });
            }

            NotificationHelper.showNotification(this, otherUid, title, body, remoteMessage.getData());

        }

    }

    private void getChatWhenAppIsNotActive(Context context, MessageModel messageModel, String otherUid)
    {
        messageModel.setMyUid(myId);

        // activate ROOM
        Application application = (Application) context.getApplicationContext();
        UserChatRepository userRepository = new UserChatRepository(application);

        UserOnChatUI_Model getUserModel = userRepository.findUserByUid(otherUid, myId);

        if(getUserModel != null)
        {
            String newChatDateKey = refMsgFast.child(myId).child(otherUid).push().getKey();  // create an id for each message

            // check if it's first chat for today
            ChatUtils.notifyFirstChatOfANewDay(getUserModel.getTimeSent(), messageModel.getTimeSent(),    // onNotification
                    newChatDateKey, null, otherUid, context);


            int currentNewChatNumber = getUserModel.getNumberOfNewChat() + 1;

            if(currentNewChatNumber > 1) // add increment to previous new chat number -- inside chat
            {
                MessageModel modelChat = userRepository.findNewChatNumber(otherUid, myId, Ki.type_pin, "yes");
                if(modelChat != null)
                {
                    // update chat count
                    modelChat.setNewChatNumberID(String.valueOf(currentNewChatNumber));
                    userRepository.updateChats(modelChat);

                } else {
                    currentNewChatNumber = 0;
                }

            } else // it is the first new chat // generate new id and add count
            {
                MessageModel newNewCountModel = new MessageModel(null, null, myId, null,
                        System.currentTimeMillis(), messageModel.getNewChatNumberID(), "yes", String.valueOf(currentNewChatNumber),
                        null, 0, Ki.type_pin, null, null, false, false,
                        null, null, null, null, null, null);

                newNewCountModel.setMyUid(myId);

                // save to local ROOM database
                userRepository.insertChats(otherUid, newNewCountModel);
            }

            // update the outside ROOM
            getUserModel.setNumberOfNewChat(currentNewChatNumber);
            userRepository.updateUser(getUserModel);
//System.out.println("what is number: " + currentNewChatNumber);
            refLastDetails.child(myId).child(otherUid).child("numberOfNewChat").setValue(currentNewChatNumber).addOnCompleteListener(task ->
            {
                if(task.isSuccessful()) System.out.println("it is success: " + task.getResult());
                else System.out.println("it is failed: " + Objects.requireNonNull(task.getException()).getMessage());
            });

        } else  // first time user - add to database
        {
            refLastDetails.child(myId).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    UserOnChatUI_Model userModel = snapshot.getValue(UserOnChatUI_Model.class);

                    if (userModel != null) {
                        userModel.setOtherUid(otherUid);
                        userModel.setMyUid(myId);
                        userModel.setNumberOfNewChat(1);

                        userRepository.insertUser(userModel);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            refLastDetails.child(myId).child(otherUid).child("numberOfNewChat").setValue(1);
        }

        // add to room database -- inside chat
        userRepository.insertChats(otherUid, messageModel);

        // delete from firebase
        refMsgFast.child(myId).child(otherUid).child(messageModel.getIdKey()).removeValue();

        // update last msg for outside chat display chat, since it will show msg new count
        refLastDetails.child(myId).child(otherUid).child("msgStatus").setValue(0);

    }

    private MessageModel messageModelMethod(Map<String, String> data)
    {
        String idKey = data.get("idKey");
//        String myUid = data.get("myUid");
        String fromUid = data.get("fromUid");
        String message = data.get("message");
        String emojiOnly = data.get("emojiOnly");
        String senderName = data.get("senderName");
        String replyFrom = data.get("replyFrom");
        String edit = data.get("edit");
        String replyMsg = data.get("replyMsg");
        long timeSent = Long.parseLong(Objects.requireNonNull(data.get("timeSent")));
        String newChatNumberID = data.get("newChatNumberID");
        int msgStatus = Integer.parseInt(Objects.requireNonNull(data.get("msgStatus")));
        int type = Integer.parseInt(Objects.requireNonNull(data.get("type")));
        String imageSize = data.get("imageSize");
        String replyID = data.get("replyID");
        boolean chatIsPin = Boolean.parseBoolean(data.get("chatIsPin"));
        boolean chatIsForward = Boolean.parseBoolean(data.get("chatIsForward"));
        String emoji = data.get("emoji");
//        String otherUid = data.get("otherUid");
        String voiceNote = data.get("voiceNote");
        String vnDuration = data.get("vnDuration");
        String photoUriPath = data.get("photoUriPath");
        String photoUriOriginal = data.get("photoUriOriginal");

        return new MessageModel(
                message, senderName, fromUid, replyFrom, timeSent, idKey, edit, newChatNumberID, replyMsg, msgStatus,
                type, imageSize, replyID, chatIsPin, chatIsForward, emoji, emojiOnly, voiceNote, vnDuration,
                photoUriPath, photoUriOriginal
        );

    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

}







