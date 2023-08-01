package com.pixel.chatapp.chats;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.os.EnvironmentCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixel.chatapp.FragmentListener;
import com.pixel.chatapp.R;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlinx.coroutines.GlobalScope;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<MessageModel> modelList;
    String uId;
    String userName;
    Boolean status;
    private int send;
    private int receive;
    FirebaseUser user;
    DatabaseReference refCheck, refUsers;
    Context mContext;
    EditText editTextMsg;
    ConstraintLayout deleteBody;
    private CardView cardViewReply;
    private TextView textViewReply, textViewDelOther, nameReply, replyVisible;
    private ImageView editOrReplyIV;
    Handler handler;
    private static final String VOICE_NOTE = "MyPreferences";
    private static final String KEY_LIST = "myList";
    private List<Map<String, Object>> mapList;

    private FragmentListener fragmentListener;
    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }

//    public MessageAdapter(List<MessageModel> modelList, String userName, String uId, Context mContext, EditText editMsg,
//                          ConstraintLayout deleteBody, TextView textViewReply, CardView cardViewReply, TextView textViewDelOther,
//                          ImageView editOrReplyIV, TextView nameReply, TextView replyVisible) {

    public MessageAdapter(List<MessageModel> modelList, String userName, String uId, Context mContext) {
        this.modelList = modelList;
        this.userName = userName;
        this.uId = uId;
        this.mContext = mContext;
        notifyDataSetChanged();
//        this.editTextMsg = editMsg;
//        this.deleteBody = deleteBody;
//        this.textViewReply = textViewReply;
//        this.cardViewReply = cardViewReply;
//        this.textViewDelOther = textViewDelOther;
//        this.editOrReplyIV = editOrReplyIV;
//        this.nameReply = nameReply;
//        this.replyVisible = replyVisible;
        handler = new Handler(Looper.getMainLooper());
//        mapArrayList = new ArrayList<>();

        status = false;
        send = 1;
        receive = 2;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refCheck = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");

    }

    public void addNewMessages(List<MessageModel> newMessages) {
//        modelList.clear();
        int startPosition = modelList.size();
        modelList.addAll(newMessages);
        notifyItemRangeInserted(startPosition, newMessages.size());
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
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        holder.setIsRecyclable(false);      // stop position from repeating itself

        int pos = position;     //   to get the position of each msg
        holder.cardViewChatBox.setTag(pos);        //     to get cardView position

        long convert = (long) modelList.get(position).getTimeSent();
        Date d = new Date(convert); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

        holder.timeMsg.setText(time.toLowerCase());       // show the time each msg was sent

        holder.textViewShowMsg.setText(modelList.get(pos).getMessage());    //  Show messages
        holder.editNotify.setText(modelList.get(pos).getEdit());    // notify user when msg is edited
//notifyDataSetChanged();

        // ----------------- Voice Note setting
//        int visible = (int) modelList.get(pos).getType();   //  1 is visible, 4 is invisible, 8 is Gone
//        holder.voicePlayerView.setVisibility(visible);


        // ----------------- reply msg setting
        int intValue = (int) modelList.get(pos).getVisibility();
        holder.constraintReplyCon.setVisibility(intValue);    // set reply container to visibility
        holder.senderNameTV.setText(modelList.get(pos).getReplyFrom());  //  set the username for reply msg
        holder.textViewReplyMsg.setText(modelList.get(pos).getReplyMsg());     //   set the reply text on top msg

        // set unsent and sent msg... delivery and seen settings-- msg status tick
        int intMsg = modelList.get(pos).getMsgStatus();
        int numMsg = (int) R.drawable.message_tick_one;

        if(intMsg == 700033){
            numMsg = (int) R.drawable.message_load;
        } else if (intMsg == 700016) {
            numMsg = (int) R.drawable.baseline_grade_24;
        }
        // 700024 --- tick one msg  // 700016 -- send msg   // 700033 -- load
        holder.seenMsg.setImageResource(numMsg);     // set msg status tick


        //   get the number of new message I have
//        newMsgNumber(holder, pos);


        // reply option
        holder.imageViewReply.setOnClickListener(view -> {

            if(modelList.get(pos).getMsgStatus() == 700033){
                Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
            }
            else {
                editOrReplyIV.setImageResource(R.drawable.reply);   // set reply icon

                editAndReply("reply", modelList.get(pos).getIdKey(), editTextMsg, holder,
                        pos, modelList.get(pos).getFrom(), "replying...", 1);       // 1 is visibility, 8 is Gone and 4 is Invisible
            }
        });

        // edit option
        holder.imageViewEdit.setOnClickListener(view -> {

            if(modelList.get(pos).getMsgStatus() == 700033){
                Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
            }
            else {

                // show edit msg, show edit icon
                fragmentListener.onEditMessage(modelList.get(pos).getMessage(), android.R.drawable.ic_menu_edit);
                holder.constraintChatTop.setVisibility(View.GONE);  // close option menu


//                editOrReplyIV.setImageResource(android.R.drawable.ic_menu_edit);    // set edit icon
//                editTextMsg.setText(""+ modelList.get(pos).getMessage());
//
//                editAndReply("yes", modelList.get(pos).getIdKey(), editTextMsg, holder,
//                        pos, modelList.get(pos).getFrom(), "editing...", 4);
            }
        });

        // delete option
        holder.imageViewDel.setOnClickListener(view -> {

            if(modelList.get(pos).getMsgStatus() == 700033){
                Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
            } else {
                // user1 should be unable to delete user2 msg
                if(!modelList.get(pos).getFrom().equals(userName)){
                    textViewDelOther.setVisibility(View.GONE);
                } else {
                    textViewDelOther.setVisibility(View.VISIBLE);
                }

                deleteBody.setVisibility(View.VISIBLE);
                // Send the idKey to messageActivity with LocalBroadcast
                Intent intent = new Intent("editMsg");
                intent.putExtra("id", modelList.get(pos).getIdKey());

                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                holder.constraintChatTop.setVisibility(View.GONE);
            }
        });

        // copy option
        holder.imageViewCopy.setOnClickListener(view -> {

            String selectedText = modelList.get(pos).getMessage();
            ClipboardManager clipboard =  (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", selectedText);

            if (clipboard == null || clip == null) return;
            clipboard.setPrimaryClip(clip);

            Toast.makeText(mContext, "Text copied!", Toast.LENGTH_SHORT).show();
            // for paste code
//                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
//                try {
//                    CharSequence text = clipboard.getPrimaryClip().getItemAt(0).getText();
//                } catch (Exception e) {
//                    return;
//                }
        });



        //   show chat options
        holder.cardViewChatBox.setOnClickListener(view -> {
            if(holder.constraintChatTop.getVisibility() == View.GONE){
                holder.constraintChatTop.setVisibility(View.VISIBLE);
            } else{
                holder.constraintChatTop.setVisibility(View.GONE);
            }
        });

        holder.textViewShowMsg.setOnClickListener(view -> {
            if(holder.constraintChatTop.getVisibility() == View.GONE){
                holder.constraintChatTop.setVisibility(View.VISIBLE);
            } else{
                holder.constraintChatTop.setVisibility(View.GONE);
            }
        });

        // close chat option
        holder.constraintMsgContainer.setOnClickListener(view -> {
            if(holder.constraintChatTop.getVisibility() == View.VISIBLE){
                holder.constraintChatTop.setVisibility(View.GONE);
            }
        });

        // download voice
        holder.circleDownload.setOnClickListener(view -> {
            holder.progressBar.setVisibility(View.VISIBLE);     // change later
            holder.progressBar.incrementProgressBy(40);     // change later

            downloadVoiceNote(pos, holder);

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

    private void editAndReply(String listener, String id, EditText editText, MessageViewHolder holder,
                              int pos, String replyFrom, String status, int visibility){

        editText.requestFocus();
        // pop up keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

        int intValue = (int) 1; // visibility
        cardViewReply.setVisibility(intValue);
        textViewReply.setText(modelList.get(pos).getMessage()); // set the reply text
        holder.constraintChatTop.setVisibility(View.GONE);  // close option menu

        // set reply name and replying hint
        replyVisible.setVisibility(View.VISIBLE);
        replyVisible.setText(status);
        nameReply.setVisibility(visibility);
        if (modelList.get(pos).getFrom().equals(userName)) {
            nameReply.setText("From You.");
        }
        else {
            nameReply.setText(modelList.get(pos).getFrom() +
                    " (@" +modelList.get(pos).getFrom()+")");
        }

        // Send the idKey to messageActivity with LocalBroadcast
        Intent intent = new Intent("editMsg");
        intent.putExtra("id", id);
        intent.putExtra("listener", listener);
        intent.putExtra("replyFrom", replyFrom);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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

        TextView textViewShowMsg, textViewNewMsg, editNotify;
        ImageView seenMsg;
        ImageView imageViewReply, imageViewEdit, imageViewPin, imageViewForward;
        ImageView imageViewReact, imageViewCopy, imageViewDel, imageViewOptions;
        ConstraintLayout constraintChatTop, constraintMsgContainer, constraintNewMsg;
        ConstraintLayout constraintReplyCon, constrSlide;
        TextView textViewReplyMsg, senderNameTV;
        CircleImageView circleSendMsg, circleDownload;
        ProgressBar progressBar;
        EditText editTextMessage;
        private VoicePlayerView voicePlayerView;
        TextView timeMsg;
        CardView cardViewChatBox;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if(status){
                timeMsg = itemView.findViewById(R.id.textViewChatTime);
                seenMsg = itemView.findViewById(R.id.imageViewSeen);
                textViewShowMsg = itemView.findViewById(R.id.textViewSend);
                cardViewChatBox = itemView.findViewById(R.id.cardViewSend);

                imageViewReply = itemView.findViewById(R.id.imageViewReplyMsg);
                imageViewEdit = itemView.findViewById(R.id.imageViewEdit);
                imageViewPin = itemView.findViewById(R.id.imageViewPinMsg);
                imageViewForward = itemView.findViewById(R.id.imageViewForward);
                imageViewReact = itemView.findViewById(R.id.iVReact);
                imageViewCopy = itemView.findViewById(R.id.imageViewCopyText);
                imageViewDel = itemView.findViewById(R.id.imageViewDel2);
                editNotify = itemView.findViewById(R.id.textViewEditSender);
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

            } else {
                timeMsg = itemView.findViewById(R.id.textViewChatTime2);
                cardViewChatBox = itemView.findViewById(R.id.cardViewReceived);
                seenMsg = itemView.findViewById(R.id.imageViewSeen2);
                textViewShowMsg = itemView.findViewById(R.id.textViewReceived);

                imageViewReply = itemView.findViewById(R.id.imageViewReplyMsg2);
                imageViewEdit = itemView.findViewById(R.id.imageEdit);
                imageViewPin = itemView.findViewById(R.id.imageViewReceivePinMsg);
                imageViewForward = itemView.findViewById(R.id.imageViewForward2);
                imageViewReact = itemView.findViewById(R.id.imageViewReact2);
                imageViewCopy = itemView.findViewById(R.id.imageViewReceiveCopyText);
                imageViewDel = itemView.findViewById(R.id.imageViewReceiveDel);
                editNotify = itemView.findViewById(R.id.textViewEditedReceiver);
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


