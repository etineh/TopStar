package com.pixel.chatapp;

import static com.pixel.chatapp.home.MainActivity.contactNameShareRef;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.app.RemoteInput;
import androidx.core.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.model.ContactModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.notification.ReplyReceiver;
import com.pixel.chatapp.roomDatabase.repositories.UserChatRepository;
import com.pixel.chatapp.roomDatabase.viewModels.UserChatViewModel;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String REPLY_ACTION = "com.pixel.chatapp.REPLY_ACTION";
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    private static final String CHANNEL_ID = "my_channel_id";
    public static List<UserOnChatUI_Model> mUsersID = new ArrayList<>();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;

        // Handle the notification payload (if any)
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }
        SharedPreferences contactNameShareRef = getSharedPreferences(AllConstants.CONTACTNAME, Context.MODE_PRIVATE);
        System.out.println("what is user total listsss: " + mUsersID.size());


        // Handle the data payload (if any)
        if (remoteMessage.getData().size() > 0) {
            // get the contact name or displayed name of other user
            String otherUid = remoteMessage.getData().get("fromUid");
            String senderName = remoteMessage.getData().get("senderName");
            String getUserName = contactNameShareRef.getString(otherUid, senderName);

            title = getUserName;
            body = remoteMessage.getData().get("message");
        }

        if (title != null && body != null) {
            showNotification(this, title, body, remoteMessage.getData());
        }

        // Access user data using UserRepository
        ExecutorService executors = Executors.newSingleThreadExecutor();
        executors.execute(() -> {
            UserChatRepository userRepository = new UserChatRepository(getApplication());
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                mUsersID = userRepository.getUsers(user.getUid());
//                Log.d(TAG, "User total list size: " + mUsersID.size());
                for (UserOnChatUI_Model userID : mUsersID) {
//                    Log.d(TAG, "User ID: " + userID.getOtherUserName());
                    System.out.println("what is user total list: " + mUsersID.size());

                }
            }
        });

//readContactFromFile(this);
        // Use a Handler to post to the main thread
//        new Handler(Looper.getMainLooper()).post(() -> {
//            UserChatViewModel userViewModel = new ViewModelProvider((ViewModelStoreOwner) MyFirebaseMessagingService.this).get(UserChatViewModel.class);
//
//            ExecutorService executors = Executors.newSingleThreadExecutor();
//
//            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//            executors.execute(() -> {
//                if (userViewModel.getAllUsers(user.getUid()) != null) {
//                    mUsersID = userViewModel.getAllUsers(user.getUid());
//                    System.out.println("what is user total list: " + mUsersID.size());
//                }
//            });
//        });

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
    public void showNotification(Context context, String title, String body, Map<String, String> data) {
        // Intent for opening the main activity
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
//                System.out.println("what is key: " + entry.getKey() + " what is value " + entry.getValue());
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Create reply action
        String replyLabel = context.getString(R.string.reply);
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .build();

        Intent replyIntent = new Intent(context, ReplyReceiver.class);
        replyIntent.setAction(REPLY_ACTION);
        replyIntent.putExtra("notificationId", 0); // Use a unique ID if you have multiple notifications
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.reply, replyLabel, replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.logo)
                .setColor(ContextCompat.getColor(context, R.color.orange)) // Use ContextCompat.getColor
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body)) // Use BigTextStyle to expand the notification
                .setContentIntent(pendingIntent)
                .addAction(replyAction); // Add reply action

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}







