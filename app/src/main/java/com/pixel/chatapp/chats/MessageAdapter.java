package com.pixel.chatapp.chats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
    private TextView textViewReply;


    public MessageAdapter(List<MessageModel> modelList, String userName, String uId, Context mContext, EditText editMsg,
                          ConstraintLayout deleteBody, TextView textViewReply, CardView cardViewReply) {
        this.modelList = modelList;
        this.userName = userName;
        this.uId = uId;
        this.mContext = mContext;
        this.editTextMsg = editMsg;
        this.deleteBody = deleteBody;
        this.textViewReply = textViewReply;
        this.cardViewReply = cardViewReply;

        status = false;
        send = 1;
        receive = 2;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refCheck = FirebaseDatabase.getInstance().getReference("Checks");
        refUsers = FirebaseDatabase.getInstance().getReference("Users");

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

        int pos = holder.getAdapterPosition();     //   to get the position of each msg
        holder.cardViewChatBox.setTag(pos);        //     to get cardView position

        long convert = (long) modelList.get(position).getTimeSent();
        Date d = new Date(convert); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

        holder.timeMsg.setText(time.toLowerCase());       // show the time each msg was sent

        // show all messages in positions
        holder.textViewShowMsg.setText(modelList.get(position).getMessage());

        holder.editNotify.setText(modelList.get(pos).getEdit());    // notify user when msg is edited

        holder.constraintReplyCon.setVisibility(modelList.get(pos).getVisibility());

        holder.textViewReplyMsg.setText(modelList.get(pos).getReplyMsg());

        // unsent and sent msg... delivery and seen settings
//        refCheck.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                long msgCount = (long) snapshot.child(uId).child(user.getUid()).child("unreadMsg").getValue() + 1;
//                long offCount = (long) snapshot.child(uId).child(user.getUid()).child("offCount").getValue();
//
//                // tick load when no network and approve when network and unread msg tick
//                if(pos > (modelList.size() - (msgCount)) && pos < (modelList.size() - offCount) && msgCount >1) {
//                    holder.seenMsg.setImageResource(R.drawable.message_tick_one);
//                }
//                else if (pos >= (modelList.size() - offCount))
//                    holder.seenMsg.setImageResource(R.drawable.message_load);
//                else if (pos >= modelList.size()-msgCount - 10 && msgCount == 1) {
//                    holder.seenMsg.setImageResource(R.drawable.baseline_grade_24);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        //   get the number of new message I have
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


        //  show chat options
        holder.cardViewChatBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(holder.constraintChatTop.getVisibility() == View.GONE){
                    holder.constraintChatTop.setVisibility(View.VISIBLE);
                } else{
                    holder.constraintChatTop.setVisibility(View.GONE);
                }
            }
        });

        // close chat option
        holder.constraintMsgContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.constraintChatTop.getVisibility() == View.VISIBLE){
                    holder.constraintChatTop.setVisibility(View.GONE);
                }
            }
        });

        // reply option
        holder.imageViewReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardViewReply.setVisibility(1);
                textViewReply.setText(modelList.get(pos).getMessage());

                // pop up keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                holder.constraintChatTop.setVisibility(View.GONE);

                // Send the idKey to messageActivity with LocalBroadcast
                Intent intent = new Intent("editMsg");
                intent.putExtra("id", modelList.get(pos).getIdKey());
                intent.putExtra("listener", "reply");
//                intent.putExtra("replyMsg", modelList.get(pos).getReplyMsg());

                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

        // edit option
        holder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editTextMsg.setText(""+ modelList.get(pos).getMessage());
                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                // Send the idKey to messageActivity with LocalBroadcast
                Intent intent = new Intent("editMsg");
                intent.putExtra("id", modelList.get(pos).getIdKey());
                intent.putExtra("listener", "yes");

                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                // to close the keyboard
//                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                holder.constraintChatTop.setVisibility(View.GONE);

            }
        });

        // delete option
        holder.imageViewDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteBody.setVisibility(View.VISIBLE);
                // Send the idKey to messageActivity with LocalBroadcast
                Intent intent = new Intent("editMsg");
                intent.putExtra("id", modelList.get(pos).getIdKey());

                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                holder.constraintChatTop.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    // use this option when you want to use CardView onClick
//    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView textViewShowMsg, textViewNewMsg, editNotify;
        ImageView seenMsg;
        ImageView imageViewReply, imageViewEdit, imageViewPin, imageViewForward;
        ImageView imageViewReact, imageViewCopy, imageViewDel;
        ConstraintLayout constraintChatTop, constraintMsgContainer, constraintNewMsg;
        ConstraintLayout constraintReplyCon;
        TextView textViewReplyMsg;
        CircleImageView circleSendMsg;
        EditText editTextMessage;

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

//                cardViewChatBox.setOnClickListener(this);  // to get cardView position when clicked

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

                constraintReplyCon = itemView.findViewById(R.id.constriantReplyBox2);
                textViewReplyMsg = itemView.findViewById(R.id.textViewReply2);

//                cardViewChatBox.setOnClickListener(this); // to get cardView position when clicked
            }
        }

//        @Override
//        public void onClick(View view) {
//            int cardPosition = (int) view.getTag();
////            timeMsg.setText("check " + cardPosition);
//        }
    }

    //------------ this method is used because we have 2 view card (card_msg and card_receiver) to use
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


