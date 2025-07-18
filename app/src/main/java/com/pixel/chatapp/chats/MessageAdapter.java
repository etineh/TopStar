package com.pixel.chatapp.chats;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixel.chatapp.FragmentListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.PinMessageModel;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    // Declare a Set to keep track of highlighted positions
    public static Set<Integer> highlightedPositions = new HashSet<>();;
    public  List<MessageModel> modelList;
    public String uId;
    public String userName;
    public  Context mContext;
    Boolean status;
    private int send;
    private int receive;
    FirebaseUser user;
    DatabaseReference refCheck, refUsers, refPinMessages;
    private MessageViewHolder lastOpenViewHolder = null;
    Handler handler;
    private static final String VOICE_NOTE = "MyPreferences";
    private static final String KEY_LIST = "myList";
    private List<Map<String, Object>> mapList;

    private FragmentListener fragmentListener;
    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

    public MessageAdapter(List<MessageModel> modelList, String userName, String uId, Context mContext) {
        this.modelList = modelList;
        this.userName = userName;
        this.uId = uId;
        this.mContext = mContext;
        handler = new Handler(Looper.getMainLooper());

        status = false;
        send = 1;
        receive = 2;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refCheck = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
//        refPinMessages = FirebaseDatabase.getInstance().getReference("PinMessages");

    }

    // add new message to list method
    public void addNewMessageDB(MessageModel newMessages) {
        modelList.add(newMessages);
    }

    public void deleteMessage(String id){
        for (int i = modelList.size()-1; i >= 0; i--) {
            String all_IDs = modelList.get(i).getIdKey();
            if (id.equals(all_IDs)) { // Use equals() for string comparison
                modelList.remove(i);
                notifyDataSetChanged();
                break;
            }
        }
    }

    //  delete all chat with user
    public void clearChats() {
        modelList.clear();
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == send){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_msg, parent, false);
        } else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_received, parent, false);
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position_) {

        holder.setIsRecyclable(false);      // stop position from repeating itself

        int chatPosition = position_;     //   to get the position of each msg
//        holder.cardViewChatBox.setTag(chatPosition);        //     to get cardView position
        MessageModel modelUser = modelList.get(chatPosition);    // get the model position of each chat

        long convert = (long) modelUser.getTimeSent();
        Date d = new Date(convert); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

//        if(MainActivity.readDatabase == 0){ //---------
        holder.timeMsg.setText(time.toLowerCase());       // show the time each msg was sent

        holder.textViewShowMsg.setText(modelUser.getMessage());    //  Show messages

        // set edit icon on chat
        if(modelUser.getEdit() != null){
            if(modelUser.getEdit().equals("edited"))
                holder.editNotify.setVisibility(View.VISIBLE);
        }

        // set forward icon on chat
        if(modelUser.getIsChatForward() != null && modelUser.getIsChatForward()){
            holder.forwardIcon_IV.setVisibility(View.VISIBLE);
        }

        // set pin icon on chat
        if(modelUser.getIsChatPin() != null && modelUser.getIsChatPin()){
            holder.pinIcon_IV.setVisibility(View.VISIBLE);
        }

        // ----------------- Voice Note setting
//        int visible = (int) modelList.get(pos).getType();   //  1 is visible, 4 is invisible, 8 is Gone
//        holder.voicePlayerView.setVisibility(visible);

        // ----------------- reply msg setting
        int intValue = (int) modelUser.getVisibility();
        holder.constraintReplyCon.setVisibility(intValue);    // set reply container to visibility
        holder.senderNameTV.setText(modelUser.getReplyFrom());  //  set the username for reply msg
        holder.textViewReplyMsg.setText(modelUser.getReplyMsg());     //   set the reply text on top msg

        // set unsent and sent msg... delivery and seen settings-- msg status tick
        int intMsg = modelUser.getMsgStatus();
        int numMsg = R.drawable.baseline_grade_24;

        // 700024 --- tick one msg  // 700016 -- send msg   // 700033 -- load
        if(intMsg == 700033){   // load
            numMsg = R.drawable.message_load;
        } else if (intMsg == 700024) {  // read
            numMsg = R.drawable.message_tick_one;
        }

        holder.seenMsg.setImageResource(numMsg);     // set msg status tick

//        }   //--------------
        // --------------------------   Settings end   ----------------------------------------------------------------------


        //   get the number of new message I have
//        newMsgNumber(holder, pos);

        // forward option
        holder.imageViewForward.setOnClickListener(view -> {

            long randomID = (long)(Math.random() * 1_010_001);

            MainActivity.forwardMessageMap.put("from", userName);
            MainActivity.forwardMessageMap.put("type", modelUser.getType());
            MainActivity.forwardMessageMap.put("randomID", randomID);
            MainActivity.forwardMessageMap.put("message", modelUser.getMessage());
            MainActivity.forwardMessageMap.put("msgStatus", 700024);
            MainActivity.forwardMessageMap.put( "timeSent", ServerValue.TIMESTAMP);
            MainActivity.forwardMessageMap.put("visibility", 8);
            MainActivity.forwardMessageMap.put("isChatPin", false);
            MainActivity.forwardMessageMap.put("isChatForward", true);


            fragmentListener.onForwardChat(modelUser.getType(), randomID, modelUser.getMessage());

            holder.constraintChatTop.setVisibility(View.GONE);  // close option menu

            // reverse arrow
            if(modelUser.getFrom().equals(userName)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
        });


        // reply option
        holder.imageViewReply.setOnClickListener(view -> {

            if(modelUser.getMsgStatus() == 700033){
                Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
            }
            else {

                holder.constraintChatTop.setVisibility(View.GONE);  // close option menu
                // call method in MainActivity and set up the details
                fragmentListener.onEditOrReplyMessage(modelUser.getMessage(),"reply", modelUser.getIdKey(),
                        modelUser.getRandomID(), "replying...", R.drawable.reply, modelUser.getFrom(), 1);
                //  1 is visible, 4 is invisible, 8 is Gone
            }
            // reverse arrow
            if(modelUser.getFrom().equals(userName)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
        });


        // edit option
        holder.imageViewEdit.setOnClickListener(view -> {

            int deliveryStatus = modelUser.getMsgStatus();
            int positionCheck = modelList.size() - chatPosition;    // 1000 - 960 => 40

            if(deliveryStatus == 700033){
                Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
            } else if (positionCheck > 100) {
                Toast.makeText(mContext, "Edit recent message", Toast.LENGTH_SHORT).show();
            } else {

                holder.constraintChatTop.setVisibility(View.GONE);  // close option menu

                // send data to MainActivity via interface listener
                fragmentListener.onEditOrReplyMessage(modelUser.getMessage(),"edit", modelUser.getIdKey(),
                      modelUser.getRandomID(), "editing...", android.R.drawable.ic_menu_edit, null, View.GONE);
            }
            // reverse arrow
            if(modelUser.getFrom().equals(userName)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
        });


        // delete option
        holder.imageViewDel.setOnClickListener(view -> {

            if(modelUser.getMsgStatus() == 700033){
                Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
            } else {

                holder.constraintChatTop.setVisibility(View.GONE);

                String id = modelUser.getIdKey();
                String fromWho = modelUser.getFrom();
                long randomID = modelUser.getRandomID();

                fragmentListener.onDeleteMessage(id, fromWho, randomID);  // call method on MainActivity(L700)

                // reverse arrow
                if(modelUser.getFrom().equals(userName)){
                    holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
                } else{
                    holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
                }
            }
        });

        // emoji onClick option
        holder.imageViewReact.setOnClickListener(view -> {

            // Highlight the original message
            highlightItem(chatPosition);    // use this method as notifyItemChanged();

            // Add the original position to the set of highlighted positions
            highlightedPositions.clear();
            highlightedPositions.add(chatPosition);

            try{
                fragmentListener.onEmojiReact(holder, modelUser.getIdKey());
            }catch (Exception e){
                System.out.println("Urgent error at MA320" + e.getMessage());
//                Toast.makeText(mContext, "Restart app", Toast.LENGTH_SHORT).show();
            };

            // reverse arrow
            if(modelUser.getFrom().equals(userName)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
            holder.constraintChatTop.setVisibility(View.GONE);

        });

        // copy option
        holder.imageViewCopy.setOnClickListener(view -> {

            String selectedText = modelUser.getMessage();
            ClipboardManager clipboard =  (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", selectedText);

            if (clipboard == null || clip == null) return;
            clipboard.setPrimaryClip(clip);

            Toast.makeText(mContext, "Copied!", Toast.LENGTH_SHORT).show();
            // for paste code
//                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                try {
//                    CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
//                } catch (Exception e) {
//                    return;
//                }
            // reverse arrow
            if(modelUser.getFrom().equals(userName)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
            holder.constraintChatTop.setVisibility(View.GONE);
        });

        // pin options -- (for me or everyone)
        holder.imageViewPin.setOnClickListener(view -> {

            // send pin chat data to MainActivity
            fragmentListener.onPinData(modelUser.getIdKey(), modelUser.getMessage(),
                    ServerValue.TIMESTAMP, userName, holder);

            holder.constraintChatTop.setVisibility(View.GONE);  // close the option menu

            // reverse arrow
            if(modelUser.getFrom().equals(userName)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
        });


        //   show chat selection options
        View.OnClickListener optionClickListener = view -> {

            // Close the previously open chat options
            if (lastOpenViewHolder != null && lastOpenViewHolder != holder) {
                lastOpenViewHolder.constraintChatTop.setVisibility(View.GONE);

                // reverse the image resource to it's original imageView
                if(modelUser.getFrom().equals(userName)){
                    lastOpenViewHolder.imageViewOptions.setImageResource(R.drawable.arrow_left);
                } else{
                    lastOpenViewHolder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
                }
            }

            // make option menu visible if it's gone
            if(holder.constraintChatTop.getVisibility() == View.GONE){

                holder.constraintChatTop.setVisibility(View.VISIBLE);

                // indicate sign that msg can't be edited
                if(modelList.size() - chatPosition > 100){
                    int fadedOrangeColor = ContextCompat.getColor(mContext, R.color.transparent_orange);
                    holder.imageViewEdit.setColorFilter(fadedOrangeColor);
                }

                holder.imageViewOptions.setImageResource(R.drawable.baseline_cancel_24);

                // change the pin icon to unpin/view
                boolean check = false;
                boolean checkPublic = false;
                for (PinMessageModel pinMes :
                        MainActivity.pinPrivateChatMap.get(MainActivity.otherUserName)) {

                    if (pinMes.getMsgId().equals(modelUser.getIdKey())) {
                        check = true;
                        break;
                    }
                }
                for (PinMessageModel pinChatEveryone :
                        MainActivity.pinPublicChatMap.get(MainActivity.otherUserName)) {

                    if (pinChatEveryone.getMsgId().equals(modelUser.getIdKey())) {
                        checkPublic = true;
                        break;
                    }
                }

                if(check && checkPublic){
                    holder.pinALL_IV.setVisibility(View.VISIBLE);
                    holder.pinALL_IV.setImageResource(R.drawable.baseline_disabled_visible_view_24);
                    MainActivity.pinMineTV.setText("Unpin for me");
                    MainActivity.pinEveryoneTV.setText("Unpin for everyone");

                }else {
                    if(check){
                        holder.pinALL_IV.setVisibility(View.VISIBLE);
                        holder.pinALL_IV.setImageResource(R.drawable.lock);
                        MainActivity.pinMineTV.setText("Unpin for me");
                        MainActivity.pinEveryoneTV.setText("Pin for everyone");
                    } else if (checkPublic) {
                        holder.pinALL_IV.setVisibility(View.VISIBLE);
                        holder.pinALL_IV.setImageResource(R.drawable.baseline_public_24);
                        MainActivity.pinEveryoneTV.setText("Unpin for everyone");
                        MainActivity.pinMineTV.setText("Pin for me only");
                    } else {
                        holder.pinALL_IV.setVisibility(View.GONE);
                        MainActivity.pinMineTV.setText("Pin for me only");
                        MainActivity.pinEveryoneTV.setText("Pin for everyone");
                    }
                }

            } else{ // hide if it's visible and return arrow image
                holder.constraintChatTop.setVisibility(View.GONE);
                // reverse the image resource to it's original imageView
                if(modelUser.getFrom().equals(userName)){
                    holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
                } else{
                    holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
                }
            }

            // Update the last open ViewHolder
            lastOpenViewHolder = holder;

        };

        holder.cardViewChatBox.setOnClickListener(optionClickListener);
        holder.textViewShowMsg.setOnClickListener(optionClickListener);
        holder.imageViewOptions.setOnClickListener(optionClickListener);

        // close chat option
        holder.constraintMsgContainer.setOnClickListener(view -> {
            if(holder.constraintChatTop.getVisibility() == View.VISIBLE){
                holder.constraintChatTop.setVisibility(View.GONE);
            }
            if(modelUser.getFrom().equals(userName)){
                holder.imageViewOptions.setImageResource(R.drawable.arrow_left);
            } else{
                holder.imageViewOptions.setImageResource(R.drawable.arrow_right_);
            }
        });

        //  scroll and highlight reply message
        holder.constraintReplyCon.setOnClickListener(view -> {

            String originalMessageId = modelUser.getReplyID();
            int originalPosition = findMessagePositionById(originalMessageId);

            // Scroll to the original message's position
            if (originalPosition != RecyclerView.NO_POSITION) {
                // position is the item number clicked, originalPosition is the item number found.
                // so if item click has number of 3010, and the item found has a number of 3002, i.e 3010 - 3002 = 8
                int positionCount = chatPosition - originalPosition;

                if( positionCount < 15 ){   // increase the number (9++ to shift the highlight msg up)
                    MainActivity.recyclerMap.get(MainActivity.otherUserName).smoothScrollToPosition(originalPosition-7); // change later to 7 or 9
                } else {    // decrease the number (11-- to shift the highlight msg down)
                    MainActivity.recyclerMap.get(MainActivity.otherUserName).scrollToPosition(originalPosition-11);
                }

                // Highlight the original message
                highlightItem(originalPosition);    // use this method as notifyItemChanged();

                // Add the original position to the set of highlighted positions
                highlightedPositions.clear();
                highlightedPositions.add(originalPosition);

                // when the down-arrow button on MainActivity(444) is clicked, it should check first if
                // goToLastMessage = true; then scroll to the previous message, else scroll down as usual
                MainActivity.goToLastMessage = true;
                MainActivity.goToNum = chatPosition;

            }
        });

        // Apply highlighting if the current position is in the set of highlighted positions
        if (highlightedPositions.contains(chatPosition)) {
            // Apply highlighting to the view
            holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent_orangeLow));
        } else {
            // Reset the view's background to default
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }


        // download voice
        holder.circleDownload.setOnClickListener(view -> {
            holder.progressBar.setVisibility(View.VISIBLE);     // change later
            holder.progressBar.incrementProgressBy(40);     // change later

            downloadVoiceNote(chatPosition, holder);

        });


//        ContextWrapper contextWrapper = new ContextWrapper(mContext);
//        File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
//        File[] files = audioDir.listFiles();
//
//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    String fileName = file.getName();
//                    long fileSize = file.length();
//                    // Do something with the file information
//                    System.out.println("File: " + fileName + ", Size: " + fileSize);
//                }
//            }
//        } else {
//            System.out.println("No files found in the directory");
//        }


        // set the voice note
        // fetch the downloaded audio to voicePlayer
//        if (getVoiceNote(mContext) != null){
//            for (Map<String, Object> mapAccess : getVoiceNote(mContext)) {
//
//                String input = mapAccess.toString();    // convert each map to string value
//
//                 // Remove the curly braces at the start and end of the string
//                input = input.substring(1, input.length() - 1);
//
//                // Split the string into key and value using the "=" delimiter
//                String[] keyValue = input.split("=");
//
//                if (keyValue.length == 2) {
//                    String key = keyValue[0];
//                    String value = keyValue[1];
//
//                    // check if vn map key == position key, then set vn and download icon visibility.
//                    if(key.equals(modelList.get(pos).getIdKey()) ){
//                        modelList.get(pos).setVoicenote(value);
//                        String vnPath = modelList.get(pos).getVoicenote();
//                        modelList.get(pos).setType(8);        // 8 is GONE
//                        holder.voicePlayerView.setAudio(vnPath);
//                        holder.circleDownload.setVisibility((int) modelList.get(pos).getType());
////                    System.out.println("Go through data " + da);
//                    }
//                    else holder.circleDownload.setVisibility(visible);
//
//                } else {
//                    // Handle the case when the string cannot be split into key-value pair
//                    Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
//                }
//            }
//        } else System.out.println("Nothing on the list");
//getVoiceNote(mContext);
    }


    // ---------------------- methods ---------------------------

    public void addEmojiReact(MessageViewHolder holder, String emoji){
        holder.react_Constr.setVisibility(View.VISIBLE);
        holder.react_TV.setText(emoji);
    }

    public void pinIconDisplay(MessageViewHolder holder_){
        holder_.pinIcon_IV.setVisibility(View.VISIBLE);
    }
    public void pinIconHide(MessageViewHolder holder_){
        holder_.pinIcon_IV.setVisibility(View.GONE);
    }
    public int findMessagePositionById(String messageId) {
        for (int i = modelList.size()-1; i >= 0; i--) {
            if (modelList.get(i).getIdKey().equals(messageId)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }
    public void highlightItem(int position) {
        // Clear previous highlight, if any.
        for (int i = 0; i < MainActivity.recyclerMap.get(MainActivity.otherUserName).getChildCount(); i++) {
            View itemView = MainActivity.recyclerMap.get(MainActivity.otherUserName).getChildAt(i);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Highlight the clicked item
        View itemView = MainActivity.recyclerMap.get(MainActivity.otherUserName).getLayoutManager().findViewByPosition(position);
        if (itemView != null) {
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent_orangeLow));
        }
    }

    // save voice note to local storage sharePreference & json
    public void save_VN_PathFileToGson(Context context, List<Map<String, Object>> mapList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(VOICE_NOTE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(mapList);     // save the map into gson and save the gson to the sharePre.
        editor.putString(KEY_LIST, json);
        editor.apply();
    }

    //  get voice note from sharePreference via gson
    public List<Map<String, Object>> getVoiceNote(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(VOICE_NOTE, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(KEY_LIST, "");
////        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
////        editor.remove(KEY_LIST);
////        editor.apply();
        return gson.fromJson(json, type);
    }


    private String getRecordFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(mContext);
        File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(audioDir, "voice" + System.currentTimeMillis() + "note.3gp");
        return file.getPath();
    }

    private void downloadVoiceNote(int pos, @NonNull MessageViewHolder holder){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // bug fix--- by using thread
                    String address = modelList.get(pos).getMessage();
                    String filePath = getRecordFilePath();
                    URL url = new URL(address);

                    // get the file total size
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("HEAD");
                    long length = connection.getContentLengthLong();

                    // Download to local storage file path
                    InputStream is = url.openStream();
                    OutputStream os = new FileOutputStream(new File(filePath));

                    byte[] bytes = new byte[1024];
                    int len, downloaded = 0;
                    while ((len = is.read(bytes)) != -1){
                        os.write(bytes, 0, len);
                        downloaded += len;

                        holder.progressBar.setProgress(downloaded);
                        System.out.println("Downloading " + downloaded/1000.0f);

                        final int finalProgress = downloaded;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                if(finalProgress == length) {

                                    //  bug fix --- let gson arraylist be the new arraylist so it wont generate new arraylist
                                    if(getVoiceNote(mContext) == null){
                                        mapList = new ArrayList<>();
                                    } else mapList = getVoiceNote(mContext);

                                    // Create HashMap and add it to the ArrayList
                                    Map<String, Object> mapVN = new HashMap<>();
                                    String id = modelList.get(pos).getIdKey();
                                    //  set the id as key and file path as the value
                                    mapVN.put(id, filePath);
                                    mapList.add(mapVN);

//                                    modelList.get(pos).setVoicenote(filePath);   // change later
                                    holder.voicePlayerView.setAudio(filePath);  // put the filePath to the voicePlayer

                                    save_VN_PathFileToGson(mContext, mapList);    // save to gson sharePre.

                                    holder.progressBar.setVisibility(View.GONE);
                                    holder.circleDownload.setVisibility(View.GONE);

                                }
                            }
                        });
                    }

                    os.flush();
                    is.close();
                    os.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    holder.progressBar.setProgress(0);     // change later
                    System.out.println("fail to download " );

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "error... check your network and refresh!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

    }
    private void newMsgNumber(MessageViewHolder holder, int pos){
        refCheck.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!snapshot.child(uId).child("newMsgCount").exists()){
                    refCheck.child(user.getUid()).child(uId).child("newMsgCount").setValue(0);
                }
                else
                {
                    long newMsgNumber = (long) snapshot.child(uId).child("newMsgCount").getValue();
//                            Log.i("Check", "the num "+snapshot.child(uId));

                    if(newMsgNumber == 0) {
                        holder.constraintNewMsg.setVisibility(View.GONE);
                    }
                    else {
                        if(pos > (modelList.size() - (newMsgNumber+1)) && pos < (modelList.size() - (newMsgNumber-1))){
                            holder.constraintNewMsg.setVisibility(View.VISIBLE);
                            holder.textViewNewMsg.setText(newMsgNumber +" new messages");
                        }
                        else{
                            holder.constraintNewMsg.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        List<MessageModel> list = modelList;
        if(list != null){
            return list.size();
        } else {
            return 0;
        }
//        return modelList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewShowMsg, textViewNewMsg;
        private ImageView seenMsg, editNotify, pinALL_IV, pinIcon_IV, forwardIcon_IV;
        private ImageView imageViewReply, imageViewEdit, imageViewPin, imageViewForward;
        private ImageView imageViewReact, imageViewCopy, imageViewDel, imageViewOptions;
        private ConstraintLayout constraintChatTop, constraintMsgContainer, constraintNewMsg;
        private ConstraintLayout constraintReplyCon, constrSlide, react_Constr;
        private TextView react_TV;
        private TextView textViewReplyMsg, senderNameTV;
        private CircleImageView circleSendMsg, circleDownload;
        private ProgressBar progressBar;
        private EditText editTextMessage;
        private VoicePlayerView voicePlayerView;
        private TextView timeMsg;
        private CardView cardViewChatBox;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if(status){
                timeMsg = itemView.findViewById(R.id.textViewChatTime);
                seenMsg = itemView.findViewById(R.id.imageViewSeen);
                textViewShowMsg = itemView.findViewById(R.id.textViewSend);
                cardViewChatBox = itemView.findViewById(R.id.cardViewSend);

                pinALL_IV = itemView.findViewById(R.id.pinALL_S_IV);
                pinIcon_IV = itemView.findViewById(R.id.pinSender_IV);
                forwardIcon_IV = itemView.findViewById(R.id.forwardS_IV);

                imageViewReply = itemView.findViewById(R.id.imageViewReplyMsg);
                imageViewEdit = itemView.findViewById(R.id.imageViewEdit);
                imageViewPin = itemView.findViewById(R.id.imageViewPinMsg);
                imageViewForward = itemView.findViewById(R.id.imageViewForward);
                imageViewReact = itemView.findViewById(R.id.iVReact);
                imageViewCopy = itemView.findViewById(R.id.imageViewCopyText);
                imageViewDel = itemView.findViewById(R.id.imageViewDel2);
                editNotify = itemView.findViewById(R.id.editedSender_IV);
                textViewNewMsg = itemView.findViewById(R.id.textViewNewMsg);
                constraintNewMsg = itemView.findViewById(R.id.constraintNewMsg);
                constraintChatTop = itemView.findViewById(R.id.constraintChatTop);
                constraintMsgContainer = itemView.findViewById(R.id.constraint);
                circleSendMsg = itemView.findViewById(R.id.fab);
                editTextMessage = itemView.findViewById(R.id.editTextMessage);
                constraintReplyCon = itemView.findViewById(R.id.constriantReplyBox);
                textViewReplyMsg = itemView.findViewById(R.id.textViewReply);
                senderNameTV = itemView.findViewById(R.id.senderNameTV);
                constrSlide = itemView.findViewById(R.id.constrSlide);
                imageViewOptions = itemView.findViewById(R.id.imageViewOptions);
                voicePlayerView = itemView.findViewById(R.id.voicePlayerView);
                circleDownload = itemView.findViewById(R.id.cirleDownload);
                progressBar = itemView.findViewById(R.id.progressBarP6);

                react_Constr = itemView.findViewById(R.id.reactS_Constraint);
                react_TV = itemView.findViewById(R.id.reactS_TV);

            } else {
                timeMsg = itemView.findViewById(R.id.textViewChatTime2);
                cardViewChatBox = itemView.findViewById(R.id.cardViewReceived);
                seenMsg = itemView.findViewById(R.id.imageViewSeen2);
                textViewShowMsg = itemView.findViewById(R.id.textViewReceived);

                pinALL_IV = itemView.findViewById(R.id.pinALL_R_IV);
                pinIcon_IV = itemView.findViewById(R.id.pinReceiver_IV);
                forwardIcon_IV = itemView.findViewById(R.id.forwardR_IV);

                imageViewReply = itemView.findViewById(R.id.imageViewReplyMsg2);
                imageViewEdit = itemView.findViewById(R.id.imageEdit);
                imageViewPin = itemView.findViewById(R.id.imageViewReceivePinMsg);
                imageViewForward = itemView.findViewById(R.id.imageViewForward2);
                imageViewReact = itemView.findViewById(R.id.imageViewReact2);
                imageViewCopy = itemView.findViewById(R.id.imageViewReceiveCopyText);
                imageViewDel = itemView.findViewById(R.id.imageViewReceiveDel);
                editNotify = itemView.findViewById(R.id.editedReceiver_IV);
                textViewNewMsg = itemView.findViewById(R.id.textViewNewMsg2);
                constraintNewMsg = itemView.findViewById(R.id.constraintNewMsg2);
                constraintChatTop = itemView.findViewById(R.id.constraintReceiveTop);
                constraintMsgContainer = itemView.findViewById(R.id.constraintBody);
                editTextMessage = itemView.findViewById(R.id.editTextMessage);
                senderNameTV = itemView.findViewById(R.id.senderName2);
                constrSlide = itemView.findViewById(R.id.constrSlide2);
                imageViewOptions = itemView.findViewById(R.id.imageViewOptions2);

                constraintReplyCon = itemView.findViewById(R.id.constriantReplyBox2);
                textViewReplyMsg = itemView.findViewById(R.id.textViewReply2);
                voicePlayerView = itemView.findViewById(R.id.voicePlayerView2);
                circleDownload = itemView.findViewById(R.id.circleDownload2);
                progressBar = itemView.findViewById(R.id.progressBar2);

                react_Constr = itemView.findViewById(R.id.reactR_Constraint);
                react_TV = itemView.findViewById(R.id.reactR_TV);

            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(modelList.get(position).getFrom().equals(userName)){
            status = true;
            return send;
        } else {
            status = false;
            return receive;
        }
    }

}


