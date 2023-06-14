package com.pixel.chatapp.chats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<MessageModel> modelList;
    String uId;
    String userName;
    Boolean status;
    private int send;
    private int receive;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;

    public MessageAdapter(List<MessageModel> modelList, String userName, String uId) {
        this.modelList = modelList;
        this.userName = userName;
        this.uId = uId;

        status = false;
        send = 1;
        receive = 2;

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");
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

        int pos = holder.getAdapterPosition();     //   to get the position of each msg
        holder.cardViewSend.setTag(pos);        //     to get cardView position

        long convert = (long) modelList.get(position).timeSent;
        Date d = new Date(convert);
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

        holder.timeMsg.setText(time.toLowerCase());       // show the time each msg was sent

        holder.textViewShowMsg.setText(modelList.get(position).getMessage());     // show all the previous messages in positions

        // check seen delivery massage
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.child(uId).child(user.getUid()).child("seen").getValue().equals("in")){
//                    // all previous seen msg should remain seen except the last one
//                    holder.seenMsg.setImageResource(R.drawable.baseline_grade_24);  // all seen
//
//                    String checkReal =   snapshot.child(user.getUid()).child(uId).child("realr").getValue().toString();
//                    long msgCount = (long) snapshot.child(user.getUid()).child(uId).child("msgCount").getValue() + 1;
//
//                    // let's decide what happen to the unread last messages
//                    if(pos > (modelList.size() - msgCount) && checkReal.equals("in")) {
//                        holder.seenMsg.setImageResource(R.drawable.baseline_grade_24); // seen
//                    }
//                    else if(pos > (modelList.size() - msgCount) && checkReal.equals("out")) {
//                        holder.seenMsg.setImageResource(R.drawable.sent_pin);   // not seen yet
//                    }
//                }
//                // checking if the other user disable his view or if he has view my message
//                if(snapshot.child(uId).child(user.getUid()).child("show").getValue().equals("in")){
//                    reference.child("Users").child(user.getUid()).child(uId)  // set seen msg
//                            .child("realr").setValue("in");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        // saving last message to both users
//        reference.child(user.getUid()).child(uId).child("lastMsg").
//                setValue(modelList.get(modelList.size()-1).getMessage()).toString();
//
//        reference.child(uId).child(user.getUid()).child("lastMsg").
//                setValue(modelList.get(modelList.size()-1).getMessage()).toString();
//
//        // saving their username to know who sent the last message
//        reference.child(user.getUid()).child(uId).child("check").  // change later from "check" to "lastMsgSender"
//                setValue(modelList.get(modelList.size()-1).getFrom()).toString();
//
//        reference.child(uId).child(user.getUid()).child("check").
//                setValue(modelList.get(modelList.size()-1).getFrom()).toString();
//
//        // saving the last time sent message
//        reference.child(user.getUid()).child(uId).child("check2"). // change later from check2 to timeCheck
//                setValue(modelList.get(modelList.size()-1).getTimeSent()).toString();
//
//        reference.child(uId).child(user.getUid()).child("check2").
//                setValue(modelList.get(modelList.size()-1).getTimeSent()).toString();
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textViewShowMsg;
        ImageView seenMsg;
        TextView timeMsg;
        CardView cardViewSend;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if(status){
                timeMsg = itemView.findViewById(R.id.textViewChatTime);
                seenMsg = itemView.findViewById(R.id.imageViewSeen);
                textViewShowMsg = itemView.findViewById(R.id.textViewSend);
                cardViewSend = itemView.findViewById(R.id.cardViewSend);
                cardViewSend.setOnClickListener(this);  // to get cardView position when clicked
            } else {
                timeMsg = itemView.findViewById(R.id.textViewChatTime2);
                cardViewSend = itemView.findViewById(R.id.cardViewReceived);
                seenMsg = itemView.findViewById(R.id.imageViewSeen2);
                textViewShowMsg = itemView.findViewById(R.id.textViewReceived);
                cardViewSend.setOnClickListener(this); // to get cardView position when clicked
            }
        }

        @Override
        public void onClick(View view) {
            int cardPosition = (int) view.getTag();
            timeMsg.setText("check " + cardPosition);
        }
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


