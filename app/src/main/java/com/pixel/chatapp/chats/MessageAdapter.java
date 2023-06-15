package com.pixel.chatapp.chats;

import android.util.Log;
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
    FirebaseUser user;
    DatabaseReference refCheckMsgDelivery;

    public MessageAdapter(List<MessageModel> modelList, String userName, String uId) {
        this.modelList = modelList;
        this.userName = userName;
        this.uId = uId;

        status = false;
        send = 1;
        receive = 2;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refCheckMsgDelivery = FirebaseDatabase.getInstance().getReference("Checks");
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
        Date d = new Date(convert); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

        holder.timeMsg.setText(time.toLowerCase());       // show the time each msg was sent

        // show all the previous messages in positions
        holder.textViewShowMsg.setText(modelList.get(position).getMessage());

        //  Delivery and seen settings
        refCheckMsgDelivery.child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long msgCount = (long) snapshot.child(user.getUid()).child("unreadMsg").getValue() + 1;

                if(pos <= (modelList.size() - msgCount) ){
                    holder.seenMsg.setImageResource(R.drawable.baseline_grade_24);
                }

                if(pos > (modelList.size() - msgCount)) {
                    holder.seenMsg.setImageResource(R.drawable.message_tick_one);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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


