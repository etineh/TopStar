package com.pixel.chatapp.notification;

import static com.pixel.chatapp.home.MainActivity.adapterMap;
import static com.pixel.chatapp.notification.NotificationHelper.userMessages;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
//import android.app.RemoteInput;
import androidx.annotation.NonNull;

import android.app.Person;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.model.ContactModel;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.notification.ReplyReceiver;
import com.pixel.chatapp.roomDatabase.repositories.UserChatRepository;
import com.pixel.chatapp.utils.ChatUtils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static List<UserOnChatUI_Model> mUsersID = new ArrayList<>();
    private static final String myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private static final DatabaseReference refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
    private static final DatabaseReference refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");
//    private final static DatabaseReference refUsersLast = FirebaseDatabase.getInstance().getReference("UsersList");

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences contactNameShareRef = getSharedPreferences(AllConstants.CONTACTNAME, Context.MODE_PRIVATE);

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
                AllConstants.executors.execute(()->
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
                MessageModel modelChat = userRepository.findNewChatNumber(otherUid, myId, AllConstants.type_pin, "yes");
                if(modelChat != null)
                {
                    // update chat count
                    modelChat.setNewChatNumberID(currentNewChatNumber+"");
                    userRepository.updateChats(modelChat);

                } else {
                    currentNewChatNumber = 0;
                }

            } else // it is the first new chat // generate new id and add count
            {
                MessageModel newNewCountModel = new MessageModel(null, null, myId, null,
                        System.currentTimeMillis(), messageModel.getNewChatNumberID(), "yes", currentNewChatNumber+"",
                        null, 0, AllConstants.type_pin, null, null, false, false,
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
                if(task.isSuccessful()) System.out.println("it is success: " + task.getResult()+"");
                else System.out.println("it is failed: " + task.getException().getMessage());
            });

        } else  // first time user - add to database
        {
            refLastDetails.child(myId).child(otherUid).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    UserOnChatUI_Model userModel = snapshot.getValue(UserOnChatUI_Model.class);
                    userModel.setOtherUid(otherUid);
                    userModel.setMyUid(myId);
                    userModel.setNumberOfNewChat(1);

                    userRepository.insertUser(userModel);
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
        String myUid = data.get("myUid");
        String fromUid = data.get("fromUid");
        String message = data.get("message");
        String emojiOnly = data.get("emojiOnly");
        String senderName = data.get("senderName");
        String replyFrom = data.get("replyFrom");
        String edit = data.get("edit");
        String replyMsg = data.get("replyMsg");
        long timeSent = Long.parseLong(Objects.requireNonNull(data.get("timeSent")));
        String newChatNumberID = data.get("newChatNumberID");
        int msgStatus = Integer.parseInt(data.get("msgStatus"));
        int type = Integer.parseInt(data.get("type"));
        String imageSize = data.get("imageSize");
        String replyID = data.get("replyID");
        boolean chatIsPin = Boolean.parseBoolean(data.get("chatIsPin"));
        boolean chatIsForward = Boolean.parseBoolean(data.get("chatIsForward"));
        String emoji = data.get("emoji");
        String otherUid = data.get("otherUid");
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

    @SuppressLint("RestrictedApi")
    private void updateNotification(int notificationId, Context context, String userId, String replyText, String from)
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
            }

            // Ensure the notification remains visible
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
//        MainActivity.generateFCMToken();
//        refUsers.child(myId).child("general").child("fcmToken").setValue(token);

    }

    public static void readContactFromFile(Context context)
    {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("contacts.json");
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String json = stringBuilder.toString();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ContactModel>>(){}.getType();

            mUsersID = gson.fromJson(json, listType);
            System.out.println("what is user total list: " + mUsersID.size());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}







