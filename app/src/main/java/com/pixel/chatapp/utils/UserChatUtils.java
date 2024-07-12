package com.pixel.chatapp.utils;

import static com.pixel.chatapp.home.MainActivity.adapterMap;
import static com.pixel.chatapp.home.MainActivity.chatViewModel;
import static com.pixel.chatapp.home.MainActivity.loopOnceMap;
import static com.pixel.chatapp.home.MainActivity.recyclerMap;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ChatListAdapter;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.home.fragments.ChatsFragment;
import com.pixel.chatapp.home.fragments.PlayersFragment;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;

import java.util.List;

public class UserChatUtils {

    private final static DatabaseReference refUsersLast = FirebaseDatabase.getInstance().getReference("UsersList");
    private final static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    // find user and update the outside chat list with the new chat
    public static void findUserPositionByUID(List<UserOnChatUI_Model> userModelList, String otherId,
                                             MessageModel modelChats, int numberOfNewChat, Context context)
    {
        if (userModelList != null) {
            for (int i = 0; i < userModelList.size(); i++) {
                if (userModelList.get(i).getOtherUid().equals(otherId)) {
                    // Store the item in a temporary variable.
                    final int position = i;
                    UserOnChatUI_Model getUser = setUserModel(userModelList, modelChats, position, numberOfNewChat, context);

                    userModelList.remove(position);     // Remove the item from its old position.

                    userModelList.add(0, getUser);   // Insert the item at the first position

                    if(ChatsFragment.adapter != null)
                    {
                        AllConstants.handler.post(()->{
                            ChatsFragment.newInstance().notifyItemChanged(position);     // notify the adapter of the item changes
                            ChatsFragment.newInstance().notifyUserMoved(position);   // Notify the adapter that the user has moved.
                        });

                        updateUserChatOnPlayerFragment(otherId, getUser);

                    } else {
                        AllConstants.handler.post(()->{
                            PlayersFragment.newInstance().notifyItemChanged(position);
                            PlayersFragment.newInstance().notifyUserMoved(position);
                        });
                    }

                    MainActivity.chatViewModel.updateUser(getUser);     // update room db

                    if(numberOfNewChat > 0){    // update firebase with the new chat number
                        refUsersLast.child(user.getUid()).child(otherId).child("numberOfNewChat").setValue(numberOfNewChat);
                    } else {
                        // reset new chat count number -- inside and outside
                        AllConstants.handler.post(()-> new Handler().postDelayed(()-> new Thread(()-> {
                            try{
                                adapterMap.get(otherId).getChatByPinTypeAndDeleteViaRecycler(recyclerMap.get(otherId), otherId);   // UserChatUtil
                            } catch (Exception e) {
                                System.out.println("what is error UserChatUtil L70: " + e.getMessage());
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

                        AllConstants.handler.post(()->{
                            PlayersFragment.newInstance().notifyItemChanged(position__);
                            PlayersFragment.newInstance().notifyUserMoved(position__);
                        });
                        break;

                    } else if(position__ == userOnPlayerList.size()-1)  // user is not found
                    {
                        userOnPlayerList.remove(2);     // user not found, remove the last user from the list and add the new one.
                        userOnPlayerList.add(0, getUser);   // Insert the latest user at the first position

                        AllConstants.handler.post(()->{
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

                        AllConstants.handler.post(()->{
                            PlayersFragment.newInstance().notifyItemChanged(position__);
                            PlayersFragment.newInstance().notifyUserMoved(position__);
                        });
                        break;

                    } else if(position__ == userOnPlayerList.size()-1)  {   // user not found
                        userOnPlayerList.add(0, getUser);   // Insert the item at the first position
                        AllConstants.handler.post(()-> PlayersFragment.newInstance().notifyItemInserted(0));
                        break;
                    }
                }

            }

        } else {
            userOnPlayerList.add(0, getUser);   // Insert the item at the first position
            AllConstants.handler.post(()-> PlayersFragment.newInstance().notifyItemInserted(0));
        }

    }

    private static UserOnChatUI_Model setUserModel(List<UserOnChatUI_Model> userModelList, MessageModel modelChats, int position, int numberOfNewChat, Context context)
    {
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

        if (type == AllConstants.type_text){
            if(chat == null || chat.isEmpty()) chat = emojiOnly;

        } else if (type == AllConstants.type_voice_note){
            chat = AllConstants.MIC_ICON + vnDuration;

        }  if (type == AllConstants.type_audio)
        {
            chat = AllConstants.MUSIC_ICON + vnDuration;

        } else if (type == AllConstants.type_photo)
        {
            if(chat == null || chat.isEmpty()) chat = AllConstants.PHOTO_ICON + mContext.getString(R.string.photoCap);
            else chat = AllConstants.PHOTO_ICON + chat;
        }
        else if (type == AllConstants.type_video)
        {
            if(chat == null || chat.isEmpty()) chat = AllConstants.VIDEO_ICON + mContext.getString(R.string.videoCap);
            else chat = AllConstants.VIDEO_ICON + chat;

        } else if (type == AllConstants.type_document)
        {
            if(chat == null || chat.isEmpty()) chat = AllConstants.DOCUMENT_ICON + emojiOnly;
            else chat = AllConstants.DOCUMENT_ICON + chat;

        }else if (type == AllConstants.type_call)
        {
            if(emojiOnly.equals(mContext.getString(R.string.ongoingCall))){

                if(chat.contains("Audio")) chat = mContext.getString(R.string.audio_callCap);
                if(chat.contains("Video")) chat = mContext.getString(R.string.video_callCap);
                chat = mContext.getString(R.string.ongoingCall) + " " + chat;

            } else if (emojiOnly.equals(mContext.getString(R.string.incomingCall))) {
                if(chat.contains("Audio")) chat = AllConstants.CALL_ICON +  mContext.getString(R.string.incomingAudioCall);
                if(chat.contains("Video")) chat = AllConstants.CALL_ICON + mContext.getString(R.string.incomingVideoCall);

            } else{
                if(chat.contains("Audio")) chat = AllConstants.CALL_ICON + mContext.getString(R.string.audio_call) + " ••• " + emojiOnly;
                if(chat.contains("Video")) chat = AllConstants.CALL_ICON + mContext.getString(R.string.video_call) + " ••• " + emojiOnly;
            }

        } else if (type == AllConstants.type_pin)
        {
            chat = AllConstants.PIN_ICON + chat;
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
                        userModel.get(i).setMessage(AllConstants.DELETE_ICON +"  ...");
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

                        AllConstants.handler.post(()->{
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
                            userModel.get(i).setMessage(AllConstants.EDIT_ICON + chat);
                        } else {
                            userModel.get(i).setMessage(AllConstants.EDIT_ICON + emoji);
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

        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

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

                    if (type == AllConstants.type_pin && edit != null && edit.equals("yes"))
                    {
                        // Reset new chat count -- outside outside >> in case I am inside the chat when user send new chat
                        if (ChatsFragment.adapter != null) {
                            ChatsFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, AllConstants.fromChatFragment, true);
                        }
                        if (PlayersFragment.adapter != null) {
                            PlayersFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, AllConstants.fromPlayerFragment, true);
                        }
                        return i;
                    }
                }
            }
        }

        return -1;
    }


    public static void checkIfNewCountExist(List<MessageModel> modelList, String otherId)
    {
        if (modelList == null || otherId == null || PlayersFragment.adapter == null) {
            return; // Early exit if any of the required elements are null
        }
        new Thread(() -> {
            int startCount = modelList.size() > 5000 ? 5000 : 0;
            for (int i = modelList.size() - 1; i >= startCount; i--)
            {
                // check if new count exist
                int type = modelList.get(i).getType();
                String edit = modelList.get(i).getEdit();

                if (type == AllConstants.type_pin && edit != null && edit.equals("yes"))
                {
                    break;

                } else if(i == startCount){
                    // Reset new chat count if at the startCount boundary and it hasn't been reset yet
                    if (ChatsFragment.adapter != null) {
                        ChatsFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, AllConstants.fromChatFragment, true);
                    }
                    if (PlayersFragment.adapter != null) {
                        PlayersFragment.adapter.findUserModelByUidAndResetNewChatNum(otherId, AllConstants.fromPlayerFragment, true);
                    }
                }
            }
            loopOnceMap.put(otherId, true);

        }).start();
    }


}
