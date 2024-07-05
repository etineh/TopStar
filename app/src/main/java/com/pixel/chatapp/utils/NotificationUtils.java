package com.pixel.chatapp.utils;

//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;

public class NotificationUtils {

    public static void sendChatNotification(String recipientToken, String senderId, String messageContent, String timeSent, boolean onSeen) {
//        Message message = Message.builde
//                .setToken(recipientToken)
//                .setNotification(Notification.builder()
//                        .setTitle("New message from " + senderId)
//                        .setBody(messageContent)
//                        .build())
//                .putData("senderId", senderId)
//                .putData("message", messageContent)
//                .putData("timeSent", timeSent)
//                .putData("onSeen", String.valueOf(onSeen))
//                .build();
//
//        try {
//            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println("Successfully sent message: " + response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
