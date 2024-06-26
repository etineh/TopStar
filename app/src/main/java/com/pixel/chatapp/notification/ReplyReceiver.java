package com.pixel.chatapp.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.RemoteInput;

public class ReplyReceiver extends BroadcastReceiver {

    public static final String REPLY_ACTION = "com.pixel.chatapp.REPLY_ACTION";
    private static final String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (REPLY_ACTION.equals(intent.getAction())) {
            CharSequence replyText = getReplyMessage(intent);
            Log.d("ReplyReceiver", "Received reply: " + replyText);

            // Here you can handle the reply, for example, send it to the server or update the UI
            //  0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video
//            if (containsOnlyEmojis(message)) {
//                // send as emoji text to increase the size
//                sendMessage(null, message, 0, null, null, otherUserUid);
//
//            } else {
//                // Send as normal text
//                sendMessage(message, null, 0, null, null, otherUserUid);
//            }

            // cancel notification
            int notificationId = intent.getIntExtra("notificationId", -1);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }

    private CharSequence getReplyMessage(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_TEXT_REPLY);
        }
        return null;
    }
}
