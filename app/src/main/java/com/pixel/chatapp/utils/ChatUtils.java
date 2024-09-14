package com.pixel.chatapp.utils;

import static com.pixel.chatapp.home.MainActivity.chatViewModel;
import static com.pixel.chatapp.home.MainActivity.getLastTimeChat;
import static com.pixel.chatapp.home.MainActivity.insideChatMap;
import static com.pixel.chatapp.home.MainActivity.receiveIndicator;
import static com.pixel.chatapp.home.MainActivity.recyclerContainer;
import static com.pixel.chatapp.home.MainActivity.recyclerMap;
import static com.pixel.chatapp.home.MainActivity.scrollNumMap;
import static com.pixel.chatapp.home.MainActivity.scrollPositionIV;
import static com.pixel.chatapp.home.MainActivity.sendIndicator;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.pixel.chatapp.api.Dao_interface.NotificationDao;
import com.pixel.chatapp.api.model.outgoing.ChatNotificationM;
import com.pixel.chatapp.chats.MessageAdapter;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.home.fragments.ChatsFragment;
import com.pixel.chatapp.home.fragments.PlayersFragment;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.repositories.UserChatRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatUtils {

    private static final String myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    private static final DatabaseReference refMsgFast = FirebaseDatabase.getInstance().getReference("MsgFast");
    private static final DatabaseReference refLastDetails = FirebaseDatabase.getInstance().getReference("UsersList");

    public static void getChatFromOtherUser(MessageModel messageModel, String otherId, MessageAdapter adapter, Context context, String from)
    {
        if (messageModel.getFromUid().equals(otherId) && messageModel.getIdKey() != null)
        {
            messageModel.setMyUid(myId);
            messageModel.setMsgStatus(0);

            if( !adapter.messageIdSet.contains(messageModel.getIdKey()) )
            {
                System.out.println("what is from: " + from + " context: " + context + " key: " + messageModel.getIdKey());
                if(messageModel.getType() == AllConstants.type_call) MainActivity.missCallModel = messageModel;

                if(adapter.getItemCount() == 0) addEmptyChatCard(otherId, adapter);   // other user sending me chat the first time

                // get the number of previous unread chat, if any.
                UserOnChatUI_Model outsideUserModel = ChatsFragment.adapter != null ? ChatsFragment.adapter.findUserModelByUid(otherId)
                        : PlayersFragment.adapter.findUserModelByUid(otherId) ;

                String newChatDateKey = refMsgFast.child(myId).push().getKey();  // create an id for each message

                // check recycler position before scrolling // or receiving number of new chat alert
                int scrollNumCheck = scrollNumMap.get(otherId) == null ? adapter.getItemCount() - 1
                        : (int) scrollNumMap.get(otherId) ;
                int scrollCheck = adapter.getItemCount() - scrollNumCheck;

                int currentNewChatNumber = 0;

                //  ============    check if it's the first chat of today,
                if(outsideUserModel != null)
                {
                    notifyFirstChatOfANewDay(outsideUserModel.getTimeSent(), messageModel.getTimeSent(),    // onNewChatInteraction
                            newChatDateKey, adapter, otherId, context);

                    // add the new chat alert   ================= 5 new chats
                    if( (insideChatMap.get(otherId) != null && insideChatMap.get(otherId) && scrollCheck > 5)   // I am inside the chat
                            || ( insideChatMap.get(otherId) != null && !insideChatMap.get(otherId) )    // I am not inside the chat
                            || (insideChatMap.get(otherId) == null) )  // a total new user
                    {
                        currentNewChatNumber = outsideUserModel.getNumberOfNewChat() + 1;

                        if(currentNewChatNumber > 1) // add increment to previous new chat number -- inside chat
                        {
                            adapter.getChatAndIncrementNewChatNumber(currentNewChatNumber+"");

                        } else // it is the first new chat // generate new id and add count
                        {
                            MessageModel newNewCountModel = new MessageModel(null, null, myId, null,
                                    System.currentTimeMillis(), messageModel.getNewChatNumberID(), "yes", currentNewChatNumber+"",
                                    null, 0, AllConstants.type_pin, null, null, false, false,
                                    null, null, null, null, null, null);

                            newNewCountModel.setMyUid(myId);

                            // add new chat model number count to local list
                            adapter.addMyMessageDB(newNewCountModel);

                            // save to local ROOM database
                            chatViewModel.insertChat(otherId, newNewCountModel);

                        }
                    }

                } else {    // it is null but previously in recyclerMap b4 deleted -- new chat of a deleted user
                    notifyFirstChatOfNewUser(messageModel.getTimeSent(), newChatDateKey, adapter, otherId, messageModel, context);
                }

                // add the new msg to the model List at MessageAdapter
                adapter.addNewMessageDB(messageModel);  // receiving from other user
                // add to room database -- inside chat
                chatViewModel.insertChat(otherId, messageModel);

                // find position and move it to top as recent chat // add to outside ROOM // update new count
                if(ChatsFragment.adapter != null) {
                    UserChatUtils.findUserPositionByUID(ChatsFragment.adapter.userModelList,
                            otherId, messageModel, currentNewChatNumber, context);
                } else {
                    UserChatUtils.findUserPositionByUID(PlayersFragment.adapter.userModelList,
                            otherId, messageModel, currentNewChatNumber, context);
                }


                // update last msg for outside chat display chat, since it will show msg new count
                refLastDetails.child(myId).child(otherId).child("msgStatus").setValue(0);

                AllConstants.handler.post(()->{
                    // scroll to last position I am inside chat
                    if(insideChatMap.get(otherId) != null && insideChatMap.get(otherId) && scrollCheck < 10)
                    {
                        scrollToPreviousPosition(otherId, (adapter.getItemCount() - 1));     // new message
                    }

                    adapter.notifyItemInserted(adapter.getItemCount() - 1);

                    // show new msg alert text for user
                    if(scrollPositionIV.getVisibility() == View.VISIBLE){
                        receiveIndicator.setVisibility(View.VISIBLE);
                        sendIndicator.setVisibility(View.GONE);
                    }
                });

            } else {
                System.out.println("what is already exist from: " + from + " context: " + context + " key: " + messageModel.getIdKey());
            }

        }
    }

    // scroll to position on new message update
    public static void scrollToPreviousPosition(String otherId, int position){
        for (int i = 0; i < recyclerContainer.getChildCount(); i++) {
            View child = recyclerContainer.getChildAt(i);

            if (child == recyclerMap.get(otherId)){
                RecyclerView recyclerView = (RecyclerView) child;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollToPosition(position);

            }
        }
    }


    public static void addEmptyChatCard(String otherID, MessageAdapter adapter){
        String chatKey = refMsgFast.child(myId).push().getKey();  // create an id for each message

        // save to local list for fast update
        MessageModel messageModel = new MessageModel(null, null, myId, null,
                System.currentTimeMillis(), chatKey, null, null,
                null, 0, 10, null, null, false, false,
                null, null, null, null, null, null);

        messageModel.setMyUid(myId);

        // add chat to local list
        adapter.addMyMessageDB(messageModel);  // add empty card

        // save to local ROOM database
        chatViewModel.insertChat(otherID, messageModel);

    }

    public static void notifyFirstChatOfANewDay(long compareTime, long timeStamp, String chatID,
                                                MessageAdapter adapter, String otherId, Context context)
    {
        if (TimeUtils.isNotToday(compareTime))  // The timestamp is not today -- add
        {
            MessageModel newDateChatModel = new MessageModel(null, null, myId, null,
                    timeStamp, chatID, "newDate", null,
                    null, 0, AllConstants.type_pin, null, null, false, false,
                    null, null, null, null, null, null);

            newDateChatModel.setMyUid(myId);

            // add to local chat list
            if(adapter != null)
            {
                adapter.addMyMessageDB(newDateChatModel);
                getLastTimeChat.put(otherId, System.currentTimeMillis());   // disable sending new time
                // save to local ROOM database  -- inside chat
                chatViewModel.insertChat(otherId, newDateChatModel);

            } else  // it is coming from notification, app is offline
            {
                // activate ROOM
                Application application = (Application) context.getApplicationContext();
                UserChatRepository userRepository = new UserChatRepository(application);
                // add to ROOM
                userRepository.insertChats(otherId, newDateChatModel);
            }

        }

//1718750571411
    }

    private static void notifyFirstChatOfNewUser(long timeStamp, String chatID, MessageAdapter adapter,
                                                 String otherId, MessageModel messageModel, Context context)    // new date and chat count
    {
        MessageModel newDateChatModel = new MessageModel(null, null, myId, null,
                timeStamp, chatID, "newDate", null,
                null, 0, AllConstants.type_pin, null, null, false, false,
                null, null, null, null, null, null);

        newDateChatModel.setMyUid(myId);

        // add to local chat list
        adapter.addMyMessageDB(newDateChatModel);

        // save to local ROOM database  -- inside chat
        chatViewModel.insertChat(otherId, newDateChatModel);

        getLastTimeChat.put(otherId, System.currentTimeMillis());   // disable sending new time

//1718750571411

        String newChatDateKey = refMsgFast.child(myId).push().getKey();  // create an id for today chat

        MessageModel newNewCountModel = new MessageModel(null, null, myId, null,
                System.currentTimeMillis(), newChatDateKey, "yes", "1",
                null, 0, AllConstants.type_pin, null, null, false, false,
                null, null, null, null, null, null);

        newNewCountModel.setMyUid(myId);

        // add new chat model number count to local list
        adapter.addMyMessageDB(newNewCountModel);

        // save to local ROOM database
        chatViewModel.insertChat(otherId, newNewCountModel);

        AllConstants.handler.post(()-> {
            new Handler().postDelayed(()->  // delay 1sec to give time to userModelList to add the new user
            {
                if(ChatsFragment.adapter != null) {     // update to 1 new count
                    UserChatUtils.findUserPositionByUID(ChatsFragment.adapter.userModelList,
                            otherId, messageModel, 1, context);
                } else {
                    UserChatUtils.findUserPositionByUID(PlayersFragment.adapter.userModelList,
                            otherId, messageModel, 1, context);
                }

            }, 1000);
        });

        refLastDetails.child(myId).child(otherId).child("numberOfNewChat").setValue(1);

    }

    public static Map<String, Object> setMessageMap(MessageModel chatModel, String text, String emojiOnly,
                                                     int msgStatus, String durationOrSizeVN)
    {   // 700024 --- tick one msg  // 700016 -- seen msg   // 700033 -- load

        Map<String, Object> messageMap = new HashMap<>();
        // type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)

        messageMap.put("senderName", chatModel.getSenderName());
        messageMap.put("fromUid", myId);
        messageMap.put("type", chatModel.getType());            // 8 is for text while 1 is for voice note
        messageMap.put("idKey", chatModel.getIdKey());
        messageMap.put("message", text);
        messageMap.put("emojiOnly", emojiOnly);
        messageMap.put("voiceNote", chatModel.getVoiceNote());
        messageMap.put("vnDuration", durationOrSizeVN);
        messageMap.put("msgStatus", msgStatus);
        messageMap.put( "timeSent", ServerValue.TIMESTAMP);
        messageMap.put("newChatNumberID", chatModel.getNewChatNumberID());
        messageMap.put("chatIsPin", false);
        messageMap.put("chatIsForward", false);

        return messageMap;

    }


    public static Map<String, Object> setOutsideChatMap(String text, String emojiOnly, MessageModel chatModel, int msgStatus, String vnDuration, Context context)
    {
        // type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
        String chat = UserChatUtils.setChatText(chatModel.getType(), text, emojiOnly, vnDuration, context);

        Map<String, Object> latestChatMap = new HashMap<>();

        latestChatMap.put("fromUid", myId);
        latestChatMap.put("senderName", chatModel.getSenderName());
        latestChatMap.put("message", chat);
        latestChatMap.put("type", chatModel.getType());
        latestChatMap.put("msgStatus", msgStatus);
        latestChatMap.put( "timeSent", ServerValue.TIMESTAMP);
        latestChatMap.put("idKey", chatModel.getIdKey());

        return latestChatMap;
    }

    public static void sentChatNotification(String otherUid, Map<String, Object> getInsideChatMap, String fcmToken)
    {
        NotificationDao notificationDao = AllConstants.retrofit.create(NotificationDao.class);

        ChatNotificationM notificationM = new ChatNotificationM(fcmToken, otherUid, getInsideChatMap);

        notificationDao.chatNotify(notificationM).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                System.out.println("what is notify fail:: " + throwable.getMessage());

            }
        });
    }

}











