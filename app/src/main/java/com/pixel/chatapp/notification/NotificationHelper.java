package com.pixel.chatapp.notification;

import static com.pixel.chatapp.home.MainActivity.adapterMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.app.RemoteInput;
import androidx.core.app.RemoteInput;

import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.pixel.chatapp.R;
import com.pixel.chatapp.activities.RedirectHome;
import com.pixel.chatapp.activities.RedirectToHomeActivity;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.side_bar_menu.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationHelper {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String REPLY_ACTION = "com.pixel.chatapp.REPLY_ACTION";
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    private static final String CHANNEL_ID = "my_channel_id";

//    private static final String CHANNEL_ID = "your_channel_id";
    private static final String PREFERENCES_NAME = "notifications";
    private static final String KEY_MESSAGES = "messages";
    private static NotificationCompat.Builder notificationBuilder = null;
    public static FragmentListener listener;
    public static Context homeContext;
    public static Activity homeActivity;

    public static final Map<String, List<NotificationCompat.MessagingStyle.Message>> userMessages = new HashMap<>();
    private static NotificationCompat.MessagingStyle messagingStyle;

    public static NotificationCompat.Builder getNotificationBuilder() {
        return notificationBuilder;
    }

    public static NotificationCompat.MessagingStyle getStyle() {
        return messagingStyle;
    }


    @SuppressLint("RestrictedApi")
    public static void showNotification(Context context, String otherUid, String title, String body, Map<String, String> data)
    {
        addChatToList(otherUid, title, body);

        Intent intent;
        if(adapterMap != null && adapterMap.get(otherUid) != null) {
            intent = new Intent(context, MainActivity.class);
        } else {
            intent = new Intent(context, RedirectToHomeActivity.class);
        }
        intent.putExtra("otherUid", otherUid);
        intent.putExtra("title", title);
        intent.putExtra("body", body);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        int notificationId = otherUid.hashCode();

        if (data != null) {     // cancel and reset
            notificationBuilder = null;
            NotificationManagerCompat notificationManager_ = NotificationManagerCompat.from(context);
            notificationManager_.cancel(notificationId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setColor(ContextCompat.getColor(context, R.color.orange))
                .setAutoCancel(true)
                .setStyle(messagingStyle)
                .setContentIntent(pendingIntent)
                .addAction(setReplyOption(context, otherUid, title, body, notificationId, data))
                .setGroup(otherUid);


        if (data != null)   // Set the sound for the notification only if data is not null
        {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        } else {
            notificationBuilder.setDefaults(0); // Update silently
            notificationBuilder.setOnlyAlertOnce(true); // Update silently
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notificationId, notificationBuilder.build());

    }

    @SuppressLint("RestrictedApi")
    private static void addChatToList(String userId, String title, String body)
    {
        List<NotificationCompat.MessagingStyle.Message> messages = userMessages.getOrDefault(userId, new ArrayList<>());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
        {
            Person user = new Person.Builder().setName(title).build();
            // Create the messaging style with the accumulated messages
            messagingStyle = new NotificationCompat.MessagingStyle(androidx.core.app.Person.fromAndroidPerson(user));

            messages.add(new NotificationCompat.MessagingStyle.Message(body, System.currentTimeMillis(), androidx.core.app.Person.fromAndroidPerson(new Person.Builder().setName(title).build())));
            userMessages.put(userId, messages);

            for (NotificationCompat.MessagingStyle.Message message : messages) {
                messagingStyle.addMessage(message);
            }
        }
    }

    private static NotificationCompat.Action setReplyOption(Context context, String userId, String title, String body, int notificationId, Map<String, String> data)
    {
        // Create reply action
        String replyLabel = context.getString(R.string.reply);
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .setChoices(data == null ? null : getSuggestedReplies(body, context)) // Add suggested replies here
                .build();

        Intent replyIntent = new Intent(context, ReplyReceiver.class);
        replyIntent.setAction(REPLY_ACTION);
        replyIntent.putExtra("notificationId", notificationId);
        replyIntent.putExtra("userId", userId);
        replyIntent.putExtra("otherUser", title);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, notificationId, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Action.Builder(R.drawable.reply, replyLabel, replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

    }

    private static String[] getSuggestedReplies(String chat, Context context)
    {
        if(chat.toLowerCase().contains("i wil") || chat.toLowerCase().contains("I'll"))  {
            return new String[] {"Good", "That's nice."};
        } else if(chat.toLowerCase().contains("how are you") || chat.toLowerCase().contains("sup") ){
            return new String[] {"I'm good", "Great and you?"};
        } else if(chat.toLowerCase().contains("it going") || chat.toLowerCase().contains("sup") ){
            return new String[] {"Not bad", "Going fine"};
        } else if(chat.toLowerCase().contains("and you") || chat.toLowerCase().contains("n u") ){
            return new String[] {"Not sure", "Fine"};
        } else if(chat.toLowerCase().contains("hi") || chat.toLowerCase().contains("hello")
                || chat.toLowerCase().contains("helo") || chat.toLowerCase().contains("hey") ){
            return new String[] {"Hi", "Sup", "Who are you pls?"};
        } else if(chat.toLowerCase().contains("thank") || chat.toLowerCase().contains("grateful") || chat.toLowerCase().contains("appreciate") ){
            return new String[] {"You're welcome!", "Thank you too",};
        } else if(chat.toLowerCase().contains("welcome") || chat.toLowerCase().contains("grateful")){
            return new String[] {"Okay", "Great!"};
        } else if(chat.toLowerCase().contains("eaten")){
            return new String[] {"Not yet", "Not hungry", "No food"};
        } else if(chat.toLowerCase().contains("food")){
            return new String[] {"Rice", "Eba and soup", "Guess"};
        } else if(chat.toLowerCase().contains("afa")){
            return new String[] {"I dey o", "Fine and you?", "Great!"};
        }

        return new String[] {"Okay", "Thanks"};

    }

    public static void clearAllMessagesAndNotifications(Context context) {
        userMessages.clear();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    public static void clearMessagesForUser(String otherUid, Context context) {
        userMessages.remove(otherUid);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(otherUid.hashCode());
    }

    public static void clearMessages(String userId) {
        userMessages.remove(userId);
    }

}

