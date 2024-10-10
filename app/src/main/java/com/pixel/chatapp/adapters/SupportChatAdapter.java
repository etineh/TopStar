package com.pixel.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pixel.chatapp.R;
import com.pixel.chatapp.dataModel.SupportChatM;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportChatAdapter extends RecyclerView.Adapter<SupportChatAdapter.ChatViewHolder>{

    List<SupportChatM> chatMList;
    Context context;

    private final String myId;
    FirebaseUser user;

    private int status;
    private final int send;
    private final int sendPhoto;
    private final int receive;
    private final int receivePhoto;

    Map<String, Integer> dateMonth, dateNum;
    
    
    public SupportChatAdapter(List<SupportChatM> chatMList, Context context) {
        this.chatMList = chatMList;
        this.context = context;


        send = 1;
        receive = 2;
        sendPhoto = 3;
        receivePhoto = 4;

        status = send;

        user = FirebaseAuth.getInstance().getCurrentUser();
        myId = user.getUid();

        dateMonth = new HashMap<>();
        dateNum = new HashMap<>();

    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView;

        // Inflate a new view if the cache is empty or the view type doesn't match
        int layer;

        if(viewType == send) layer = R.layout.s_text_send_card;

        else if (viewType == sendPhoto) layer = R.layout.s_photo_send_card;

        else if (viewType == receivePhoto) layer = R.layout.s_photo_receive_card;

        else layer = R.layout.s_text_receive_card;

        itemView = LayoutInflater.from(context).inflate(layer, parent, false);

        return new SupportChatAdapter.ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

        holder.linearLayoutReplyBox.setVisibility(View.GONE);
        holder.replySenderNameTV.setText(null);
        holder.chatTime_TV.setText(null);
        holder.replyChat_TV.setText(null);
        if (holder.deliveryStatus_IV != null) holder.deliveryStatus_IV.setImageResource(0);
        if (holder.textChat_TV != null) holder.textChat_TV.setText(null);
        if (holder.photoChat_TV != null) {
            holder.photoChat_TV.setText(null);
            holder.photoChat_TV.setVisibility(View.GONE);
        }
        if (holder.loadPhotoProgressTV != null) {
            holder.loadPhotoProgressTV.setVisibility(View.GONE);
            holder.loadPhotoProgressTV.setText(null);
        }
        if (holder.progressBarLoad != null){
            holder.progressBarLoad.setVisibility(View.GONE);
        }
        if (holder.photo_IV != null) holder.photo_IV.setImageResource(0);


        String idKey = chatMList.get(position).getIdKey();
        String fromUid = chatMList.get(position).getFromUid();
        int type = chatMList.get(position).getType();
        String message = chatMList.get(position).getMessage();
        String from = chatMList.get(position).getFrom();
        String replyID = chatMList.get(position).getReplyID();
        String replyFrom = chatMList.get(position).getReplyFrom();
        String replyMsg = chatMList.get(position).getReplyMsg();
        long timeSent = chatMList.get(position).getTimeSent();
        String deliveryStatus = chatMList.get(position).getDeliveryStatus();
        String photoThumb = chatMList.get(position).getPhotoThumb();
        String photoUri = chatMList.get(position).getPhotoUri();
        String imageSize = chatMList.get(position).getImageSize();

        
        
        if (replyID != null)
        {
            holder.linearLayoutReplyBox.setVisibility(View.VISIBLE);
            holder.replySenderNameTV.setText(replyFrom);
            holder.replyChat_TV.setText(replyMsg);
        }

        // set the date and time last chat was sent
        holder.chatTime_TV.setText( chatTime(timeSent) );   //  set time

        // set msgStatus if last chat was from own user
        int deliveryIcon = R.drawable.message_load;
        if(deliveryStatus.equals("700024"))
        {   // delivery
            deliveryIcon = R.drawable.message_tick_one;
        } else if (deliveryStatus.equals("700016"))
        {  // read
            deliveryIcon = R.drawable.baseline_grade_24;
        } else if(deliveryStatus.equals("0")) {
            deliveryIcon = 0;
        }

        if(holder.deliveryStatus_IV != null)
            holder.deliveryStatus_IV.setImageResource(deliveryIcon);    //  set delivery Status


        if (type == 1)  // set chat
        {
            holder.textChat_TV.setText(message);
        } else if (type == 2)
        {
            holder.photoChat_TV.setText(message);
            holder.photoChat_TV.setVisibility(View.VISIBLE);
        }


        // set photo
        if(photoThumb != null){
            Picasso.get().load(photoThumb).into(holder.photo_IV);
        }

    }
    
    
    //  =============   methods

    public void addNewMessageDB(SupportChatM newMessage) {

//        // Check if the message ID is already present in the HashSet
//        if (!messageIdSet.contains(newMessage.getIdKey())) {
//            // Add the new message ID to the HashSet
//            messageIdSet.add(newMessage.getIdKey());
//
//            // Add the new message to the list
//            modelList.add(newMessage);
//        } else {
//            // Display a toast message to indicate that the chat message already exists
//            Toast.makeText(mContext, "Chat message already exists", Toast.LENGTH_SHORT).show();
//        }

        if(!chatMList.contains(newMessage)) {
            chatMList.add(newMessage);
//            chatMList
        } else {
//            Toast.makeText(context, "Chat can't duplicate", Toast.LENGTH_SHORT).show();
        }

    }


    private String chatTime(long timeDate){
        Date d = new Date(timeDate); //complete Data -- Mon 2023 -03 - 06 12.32pm
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);
        String previousDateString = String.valueOf(d);
        int dateLast = Integer.parseInt(previousDateString.substring(8, 10));   // 1 - 30 days

        // months
        dateMonth.put("Jan", 1);
        dateMonth.put("Feb", 2);
        dateMonth.put("Mar", 3);
        dateMonth.put("Apr", 4);
        dateMonth.put("May", 5);
        dateMonth.put("Jun", 6);
        dateMonth.put("Jul", 7);
        dateMonth.put("Aug", 8);
        dateMonth.put("Sep", 9);
        dateMonth.put("Oct", 10);
        dateMonth.put("Nov", 11);
        dateMonth.put("Dec", 12);

        int lastMonth = dateMonth.get(previousDateString.substring(4,7));
//        String lastYear = previousDateString.substring(32, 34);  // year

        String joinTimeAndDate = time.toLowerCase() + " | " + dateLast +"/"+ lastMonth;

        return joinTimeAndDate;
    }
    
    
    @Override
    public int getItemCount() {
        return chatMList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayoutReplyBox;
        TextView replySenderNameTV, replyChat_TV, textChat_TV, photoChat_TV, chatTime_TV, loadPhotoProgressTV;
        ImageView photo_IV, deliveryStatus_IV, replyIcon;
        ProgressBar progressBarLoad;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            // general
            linearLayoutReplyBox = itemView.findViewById(R.id.linearLayoutReplyBox);
            replySenderNameTV = itemView.findViewById(R.id.senderNameTV);
            replyChat_TV = itemView.findViewById(R.id.textViewReply);
            chatTime_TV = itemView.findViewById(R.id.chatTime_TV);
            deliveryStatus_IV = itemView.findViewById(R.id.imageViewSeen);  // check null

            if(status == send || status == receive)
            {
                textChat_TV = itemView.findViewById(R.id.chat_TV);
//                textChat_TV = itemView.findViewById(R.id.chat_TV);

            } else if (status == sendPhoto || status == receivePhoto)
            {
                photo_IV = itemView.findViewById(R.id.photoCardSender);
                photoChat_TV = itemView.findViewById(R.id.photoChatSender_TV);
                progressBarLoad = itemView.findViewById(R.id.progressBarLoad1);
                loadPhotoProgressTV = itemView.findViewById(R.id.loadPhotoProgressTV);
                replyIcon = itemView.findViewById(R.id.replyIcon);           
            }

        }


    }


    @Override
    public int getItemViewType(int position) {

        // type 1 is for just text-chat, type 2 is photo

        System.out.println("what is uid " + chatMList.get(position).getFromUid());
        SupportChatM chat = chatMList.get(position);
        // check if the chat is from me via my uid
        if(chat.getFromUid().equals(myId))
        {
            if(chat.getType() == 2)
            {
                status = sendPhoto;
                return sendPhoto;
            } else
            {
                status = send;
                return send;
            }

        } else
        {    //  chat is from other user
            System.out.println("what is not uid " + myId);

            if(chat.getType() == 2)
            {
                status = receivePhoto;
                return receivePhoto;
            } else
            {
                status = receive;
                return receive;
            }
        }

//        return super.getItemViewType(position);
    }
}










