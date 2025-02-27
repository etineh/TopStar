package com.pixel.chatapp.utilities;

import static com.pixel.chatapp.view_controller.MainActivity.adapterMap;
import static com.pixel.chatapp.view_controller.MainActivity.loopOnceMap;
import static com.pixel.chatapp.view_controller.MainActivity.recyclerMap;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.constants.Kc;
import com.pixel.chatapp.constants.Ki;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.view_controller.fragments.ChatsFragment;
import com.pixel.chatapp.view_controller.fragments.PlayersFragment;
import com.pixel.chatapp.dataModel.MessageModel;
import com.pixel.chatapp.dataModel.UserOnChatUI_Model;
import com.pixel.chatapp.services.roomDatabase.repositories.UserChatRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UserChatUtils {

    private final static DatabaseReference refUsersLast = FirebaseDatabase.getInstance().getReference("UsersList");
    private static final String myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    public static List<UserOnChatUI_Model> getAllUsersFromRoom(Context context) {
        try {
            CompletableFuture<List<UserOnChatUI_Model>> future = new CompletableFuture<>();
            new Thread(()->{

                // activate ROOM
                Application application = (Application) context.getApplicationContext();
                UserChatRepository userRepository = new UserChatRepository(application);

                future.complete(userRepository.getUsers(myId));

            }).start();

            return future.get();

        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error occur UserChatUtils L60: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    // find user and update the outside chat list with the new chat
    public static void findUserPositionByUID(List<UserOnChatUI_Model> userModelList, String otherId,
                                             MessageModel modelChats, int numberOfNewChat, Context context)
    {
        if (userModelList != null) {
            for (int i = 0; i < userModelList.size(); i++)
            {
                if (userModelList.get(i).getOtherUid().equals(otherId))
                {
                    final int position = i;
                    UserOnChatUI_Model getUser = setUserModel(userModelList, modelChats, position, numberOfNewChat, context);

                    userModelList.remove(position);     // Remove the item from its old position.

                    userModelList.add(0, getUser);   // Insert the item at the first position

                    if(ChatsFragment.adapter != null)
                    {
                        Kc.handler.post(()->{
                            ChatsFragment.newInstance().notifyItemChanged(position);     // notify the adapter of the item changes
                            ChatsFragment.newInstance().notifyUserMoved(position);   // Notify the adapter that the user has moved.
                        });

                        updateUserChatOnPlayerFragment(otherId, getUser);

                    } else {
                        Kc.handler.post(()->{
                            PlayersFragment.newInstance().notifyItemChanged(position);
                            PlayersFragment.newInstance().notifyUserMoved(position);
                        });
                    }

                    MainActivity.chatViewModel.updateUser(getUser);     // update room db

                    if(numberOfNewChat > 0){    // update firebase with the new chat number
                        refUsersLast.child(myId).child(otherId).child("numberOfNewChat").setValue(numberOfNewChat);
                    } else {
                        // reset new chat count number -- inside and outside
                        Kc.handler.post(()-> new Handler().postDelayed(()-> new Thread(()-> {
                            try{
                                Objects.requireNonNull(adapterMap.get(otherId)).getChatByPinTypeAndDeleteViaRecycler(Objects.requireNonNull(recyclerMap.get(otherId)), otherId);   // UserChatUtil
                            } catch (Exception e) {
                                System.out.println("what is error UserChatUtil L80: " + e.getMessage());
                            }
                        }).start(), 500));
                    }

                    break;

                }
            }
        }
    }

    private static void updateUserChatOnPlayerFragment(String otherId, UserOnChatUI_Model getUser)
    {
        List<UserOnChatUI_Model> userOnPlayerList = PlayersFragment.adapter.userModelList;
        if(userOnPlayerList.size() > 0)
        {
            for (int j = 0; j < userOnPlayerList.size(); j++)
            {
                final int position__ = j;
                String otherUid = userOnPlayerList.get(position__).getOtherUid();
                if(userOnPlayerList.size() == 3)    // is 3 and user is found
                {
                    if(otherId.equals(otherUid))    // user is found
                    {
                        userOnPlayerList.remove(position__);     // Remove the user from the list.
                        userOnPlayerList.add(0, getUser);   // Insert the latest user at the first position

                        Kc.handler.post(()->{
                            PlayersFragment.newInstance().notifyItemChanged(position__);
                            PlayersFragment.newInstance().notifyUserMoved(position__);
                        });
                        break;

                    } else if(position__ == userOnPlayerList.size()-1)  // user is not found
                    {
                        userOnPlayerList.remove(2);     // user not found, remove the last user from the list and add the new one.
                        userOnPlayerList.add(0, getUser);   // Insert the latest user at the first position

                        Kc.handler.post(()->{
                            PlayersFragment.newInstance().notifyItemChanged(position__);
                            PlayersFragment.newInstance().notifyUserMoved(position__);
                        });
                    }


                } else if (userOnPlayerList.size() < 3)
                {
                    if(otherId.equals(otherUid))    // user is found
                    {
                        userOnPlayerList.remove(position__);     // Remove the user from the list.
                        userOnPlayerList.add(0, getUser);   // Insert the latest user at the first position

                        Kc.handler.post(()->{
                            PlayersFragment.newInstance().notifyItemChanged(position__);
                            PlayersFragment.newInstance().notifyUserMoved(position__);
                        });
                        break;

                    } else if(position__ == userOnPlayerList.size()-1)  {   // user not found
                        userOnPlayerList.add(0, getUser);   // Insert the item at the first position
                        Kc.handler.post(()-> PlayersFragment.newInstance().notifyItemInserted(0));
                        break;
                    }
                }

            }

        } else {
            userOnPlayerList.add(0, getUser);   // Insert the item at the first position
            Kc.handler.post(()-> PlayersFragment.newInstance().notifyItemInserted(0));
        }

    }

    public static UserOnChatUI_Model setUserModel(List<UserOnChatUI_Model> userModelList, MessageModel modelChats,
                                                   int position, int numberOfNewChat, Context context)
    {
        if (userModelList == null || position == -1) return null;

        UserOnChatUI_Model getUser = userModelList.get(position);
        String chat = modelChats.getMessage();
        String emojiOnly = modelChats.getEmojiOnly();
        String vnDuration = modelChats.getVnDuration();
        long timeSent = modelChats.getTimeSent();
        String chatID = modelChats.getIdKey();
        int type = modelChats.getType();

        chat = setChatText(type, chat, emojiOnly, vnDuration, context);

        // update user outside chat data
        getUser.setFromUid(modelChats.getFromUid());
        getUser.setIdKey(chatID);
        getUser.setMessage(chat);
        getUser.setType(type);
        getUser.setEmojiOnly(emojiOnly);
        getUser.setMsgStatus(modelChats.getMsgStatus());
        getUser.setTimeSent(timeSent);
        getUser.setNumberOfNewChat(numberOfNewChat);

        return getUser;
    }

    public static String setChatText(int type, String chat, String emojiOnly, String vnDuration, Context mContext){

        if (type == Ki.type_text){
            if(chat == null || chat.isEmpty()) chat = emojiOnly;

        } else if (type == Ki.type_voice_note){
            chat = Ki.MIC_ICON + vnDuration;

        }  if (type == Ki.type_audio)
        {
            chat = Ki.MUSIC_ICON + vnDuration;

        } else if (type == Ki.type_photo)
        {
            if(chat == null || chat.isEmpty()) chat = Ki.PHOTO_ICON + mContext.getString(R.string.photoCap);
            else chat = Ki.PHOTO_ICON + chat;
        }
        else if (type == Ki.type_video)
        {
            if(chat == null || chat.isEmpty()) chat = Ki.VIDEO_ICON + mContext.getString(R.string.videoCap);
            else chat = Ki.VIDEO_ICON + chat;

        } else if (type == Ki.type_document)
        {
            if(chat == null || chat.isEmpty()) chat = Ki.DOCUMENT_ICON + emojiOnly;
            else chat = Ki.DOCUMENT_ICON + chat;

        }else if (type == Ki.type_call)
        {
            if(emojiOnly.equals(mContext.getString(R.string.ongoingCall))){

                if(chat.contains("Audio")) chat = mContext.getString(R.string.audio_callCap);
                if(chat.contains("Video")) chat = mContext.getString(R.string.video_callCap);
                chat = mContext.getString(R.string.ongoingCall) + " " + chat;

            } else if (emojiOnly.equals(mContext.getString(R.string.incomingCall))) {
                if(chat.contains("Audio")) chat = Ki.CALL_ICON +  mContext.getString(R.string.incomingAudioCall);
                if(chat.contains("Video")) chat = Ki.CALL_ICON + mContext.getString(R.string.incomingVideoCall);

            } else{
                if(chat.contains("Audio")) chat = Ki.CALL_ICON + mContext.getString(R.string.audio_call) + " ••• " + emojiOnly;
                if(chat.contains("Video")) chat = Ki.CALL_ICON + mContext.getString(R.string.video_call) + " ••• " + emojiOnly;
            }

        } else if (type == Ki.type_pin)
        {
            chat = Ki.PIN_ICON + chat;
        }

        return chat;

    }


    // delete the last chat from the outside UI if same
    public static void findUserAndDeleteChat(ChatListAdapter adapter, String userUid, String chatId)    // delete last chat
    {
        List<UserOnChatUI_Model> userModel = adapter.userModelList;
        if (userModel != null) {
            for (int i = 0; i < userModel.size(); i++) {
                // check if it's same user ID
                if (userModel.get(i).getOtherUid().equals(userUid)) {
                    // check if it's same message ID
                    if(userModel.get(i).getIdKey().equals(chatId)){
                        // Remove the chat from its old position.
                        userModel.get(i).setMessage(Ki.DELETE_ICON +"  ...");
//                        mUsersID.get(i).setEmojiOnly("...");
                        adapter.notifyItemChanged(i, new Object());
                    }
                    break;
                }
            }
        }
    }

    public static void findUserAndDelete(ChatListAdapter adapter, String userUid)   // find user and delete from the ChatList
    {
        new Thread(()-> {
            List<UserOnChatUI_Model> userModel = adapter.userModelList;

            if (userModel != null) {
                for (int i = 0; i < userModel.size(); i++) {
                    int userPosition = i;
                    if (userModel.get(userPosition).getOtherUid().equals(userUid)) {

                        Kc.handler.post(()->{
                            userModel.remove(userPosition);
                            adapter.notifyItemRemoved(userPosition);
                            adapter.notifyItemRangeChanged(userPosition, userModel.size());
                        });

                        // Exit the loop, as we've found the item and moved it.
                        break;
                    }
                }
            }
        }).start();
    }

    public static void findUserAndEditChat(ChatListAdapter adapter, String userUid, String chatId, String chat, String emoji)
    {
        List<UserOnChatUI_Model> userModel = adapter.userModelList;

        if (userModel != null) {
            for (int i = 0; i < userModel.size(); i++) {
                // check if it's same user ID
                if (userModel.get(i).getOtherUid().equals(userUid)) {
                    // check if it's same message ID
                    if(userModel.get(i).getIdKey().equals(chatId)){
                        if(chat != null){
                            userModel.get(i).setMessage(Ki.EDIT_ICON + chat);
                        } else {
                            userModel.get(i).setMessage(Ki.EDIT_ICON + emoji);
                        }

                        adapter.notifyItemChanged(i, new Object());
                    }
                    break;
                }
            }
        }
    }


    public static int getNewChatNumberPosition(RecyclerView recyclerView, String otherId, List<MessageModel> modelList)
    {
        if (modelList == null || otherId == null || PlayersFragment.adapter == null || recyclerView == null) {
            return -1; // Early exit if any of the required elements are null
        }

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof LinearLayoutManager linearLayoutManager) {

            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

            // Ensure the indices are within bounds
            if (firstVisibleItemPosition < 0 || lastVisibleItemPosition < 0) {
                return -1; // Indicating no valid position found
            }

            for (int i = lastVisibleItemPosition + 2; i >= firstVisibleItemPosition; i--) {

                if (i < modelList.size()) { // Check if index is within bounds
                    int type = modelList.get(i).getType();
                    String edit = modelList.get(i).getEdit();

                    if (type == Ki.type_pin && edit != null && edit.equals("yes"))
                    {
                        // Reset new chat count -- outside outside >> in case I am inside the chat when user send new chat
                        if (ChatsFragment.adapter != null) {
                            ChatsFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, Ki.fromChatFragment, true);
                        }
                        if (PlayersFragment.adapter != null) {
                            PlayersFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, Ki.fromPlayerFragment, true);
                        }
                        return i;
                    }
                }
            }
        }

        return -1;
    }


    public static void checkIfNewCountExist(List<MessageModel> modelList, String otherId, boolean fromNotification, UserChatRepository userChatRepository)
    {
        if(!fromNotification){
            if (modelList == null || otherId == null || PlayersFragment.adapter == null) {
                return; // Early exit if any of the required elements are null
            }
        } else if (modelList == null || otherId == null) return;

        new Thread(() -> {
            int startCount = modelList.size() > 5000 ? 5000 : 0;
            for (int i = modelList.size() - 1; i >= startCount; i--)
            {
                // check if new count exist
                int type = modelList.get(i).getType();
                String edit = modelList.get(i).getEdit();

                if (type == Ki.type_pin && edit != null && edit.equals("yes"))
                {
//                    System.out.println("what is calling2222: " + i);

                    if (!fromNotification) break;
                    else
                    {   // delete from ROOM
                        userChatRepository.deleteChats(modelList.get(i)); // delete from room
//                        System.out.println("what is i have found and delete UserChatUtil L380: " + i + " chat: " + modelList.get(i));
                    }

                } else if(i == startCount && !fromNotification){
                    // Reset new chat count if at the startCount boundary and it hasn't been reset yet
                    if (ChatsFragment.adapter != null) {
                        ChatsFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, Ki.fromChatFragment, true);
                    }
                    if (PlayersFragment.adapter != null) {
                        PlayersFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, Ki.fromPlayerFragment, true);
                    }
                    loopOnceMap.put(otherId, true);
                }
            }

        }).start();
    }


}
