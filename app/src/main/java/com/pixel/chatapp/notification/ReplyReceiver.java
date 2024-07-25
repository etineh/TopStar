package com.pixel.chatapp.notification;

import static com.pixel.chatapp.notification.NotificationHelper.userMessages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Person;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.ChatListener;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.repositories.UserChatRepository;
import com.pixel.chatapp.utils.ChatUtils;
import com.pixel.chatapp.utils.UserChatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReplyReceiver extends BroadcastReceiver{

    public static final String REPLY_ACTION = "com.pixel.chatapp.REPLY_ACTION";
    private static final String KEY_TEXT_REPLY = "key_text_reply";

    private static final String myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    public static ChatListener chatListener;


    @SuppressLint("RestrictedApi")
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (REPLY_ACTION.equals(intent.getAction())) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                String replyText = remoteInput.getString(KEY_TEXT_REPLY);
                String otherUid = intent.getStringExtra("userId");
                String otherUser = intent.getStringExtra("otherUser");
//                int notificationId = intent.getIntExtra("notificationId", 0);
                String you = otherUser.contains(">>") ? otherUser : context.getString(R.string.you) + " >> " + otherUser;


                if (replyText != null && replyText.length() > 0 && otherUid != null)
                {
//                    updateNotification(notificationId, context, userId, replyText, you);        // this doesn't remove it but give auto-suggestion
                    NotificationHelper.showNotification(context, otherUid, you, replyText, null);   // this removes the notification icon

                    // send chat to database
                    if(chatListener != null){
                        chatListener.sendMessage(replyText.trim(), null, 0, null, null, otherUid, false);

                    } else {
                        new Thread(()-> sendToDatabase(context, otherUid, replyText)).start();
                    }

                }
            }
        }
    }

    private void sendToDatabase(Context context, String otherUid, String replyText)
    {
        SharedPreferences myProfileShareRef = context.getSharedPreferences(myId, Context.MODE_PRIVATE);
        SharedPreferences myUserNamePreferences = context.getSharedPreferences(AllConstants.MYUSERNAME, Context.MODE_PRIVATE);
        SharedPreferences otherUserFcmTokenRef = context.getSharedPreferences(AllConstants.FCMTOKEN, Context.MODE_PRIVATE);

        DatabaseReference refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
        DatabaseReference refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");
        DatabaseReference refOnReadRequest = FirebaseDatabase.getInstance().getReference("OnReadRequest");

        String newChatNumberKey = refMsgFast.child(myId).child(otherUid).push().getKey();  // create an id for each message

        String chatKey = refMsgFast.child(myId).child(otherUid).push().getKey();  // create an id for each message

        String getMyUserName = myUserNamePreferences.getString(AllConstants.USERNAME, null);
        String myDisplayName = myProfileShareRef.getString(AllConstants.PROFILE_DISNAME, "@"+getMyUserName);

        // create messageModel
        MessageModel messageModel = new MessageModel(replyText, myDisplayName, myId, null,
                System.currentTimeMillis(), chatKey, null, newChatNumberKey, null,
                700024, 0, null, null, false, false, null,
                null, null, null, null, null);

        messageModel.setMyUid(myId);

        // activate ROOM
        Application application = (Application) context.getApplicationContext();
        UserChatRepository userRepository = new UserChatRepository(application);

        // save to local ROOM database -- inside chat
        userRepository.insertChats(otherUid, messageModel);

        // save to user -- outside chat update
        List<UserOnChatUI_Model> userModelList = userRepository.getUsers(myId);
        int userPosition = findPositionUserByUid(userModelList, otherUid);  // find the position of the user
        UserOnChatUI_Model getUser = UserChatUtils.setUserModel(userModelList, messageModel, userPosition, 0, context);
        userRepository.updateUser(getUser);

        //  loop through chats first 500 and delete new count chat if found
        UserChatUtils.checkIfNewCountExist(userRepository.getEachUserChats_(otherUid, myId), otherUid, true, userRepository);


        // send the chat to other user  -- inside chat
        Map<String, Object> getInsideChatMap = ChatUtils.setMessageMap(messageModel, replyText,
                null, 700024, null);
        refMsgFast.child(otherUid).child(myId).child(messageModel.getIdKey()).setValue(getInsideChatMap);


        // send to outside user chat
        Map<String, Object> getOutsideChatMap = ChatUtils.setOutsideChatMap(replyText,
                null, messageModel, 700024, null, context);

        refLastDetails.child(otherUid).child(myId).updateChildren(getOutsideChatMap);
        refLastDetails.child(myId).child(otherUid).updateChildren(getOutsideChatMap);


        //  send chatKey to other User to read  -- customise later to check user OnRead settings
        refOnReadRequest.child(otherUid).child(myId).push().setValue(messageModel.getIdKey());

        // notify other user
        String fcmToken = otherUserFcmTokenRef.getString(otherUid, null);
        ChatUtils.sentChatNotification( otherUid, getInsideChatMap, fcmToken);

        refLastDetails.child(myId).child(otherUid).child("numberOfNewChat").setValue(0);
    }

    private int findPositionUserByUid(List<UserOnChatUI_Model> userModelList, String otherUid)
    {
        if(otherUid == null && userModelList == null) return -1;

        for (int i = 0; i < userModelList.size(); i++)
        {
            if(userModelList.get(i) != null && userModelList.get(i).getOtherUid().equals(otherUid)) {
                return i;
            }
        }

        return -1;
    }

    @SuppressLint("RestrictedApi")
    public static void updateNotification(int notificationId, Context context, String userId, String replyText, String from)
    {
        List<NotificationCompat.MessagingStyle.Message> messages = userMessages.get(userId);
        if (messages != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                messages.add(new NotificationCompat.MessagingStyle.Message(replyText, System.currentTimeMillis(), androidx.core.app.Person.fromAndroidPerson(new Person.Builder().setName("You").build())));
            }
        }

        // Update the notification with the reply
        NotificationCompat.Builder notificationBuilder = NotificationHelper.getNotificationBuilder();
        if (notificationBuilder != null) {
            NotificationCompat.MessagingStyle messagingStyle = NotificationHelper.getStyle();
            if (messagingStyle != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    messagingStyle.addMessage(replyText, System.currentTimeMillis(), androidx.core.app.Person.fromAndroidPerson(new Person.Builder().setName(from).build()));
                }
                notificationBuilder.setStyle(messagingStyle);
                notificationBuilder.setDefaults(0); // Update silently
                notificationBuilder.setOnlyAlertOnce(true); // Update silently

            }

            // Ensure the notification remains visible
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }


}
