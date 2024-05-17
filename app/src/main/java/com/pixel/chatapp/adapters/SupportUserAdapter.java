package com.pixel.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.model.SupportUserM;
import com.pixel.chatapp.side_bar_menu.support.SupportChatActivity;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupportUserAdapter extends RecyclerView.Adapter<SupportUserAdapter.SupportViewHolder> {

    private final List<SupportUserM> userMList;
    private final Context context;

    Map<String, Integer> dateMonth, dateNum;


    public SupportUserAdapter(List<SupportUserM> userMList, Context context)
    {
        this.userMList = userMList;
        this.context = context;

        dateMonth = new HashMap<>();
        dateNum = new HashMap<>();

    }

    @NonNull
    @Override
    public SupportUserAdapter.SupportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.support_card, parent, false);

        return new SupportUserAdapter.SupportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupportUserAdapter.SupportViewHolder holder, int position) {

//        holder.supportPhoto_IV.setImageResource(0);
//        holder.deliveryStatus_IV.setImageResource(0);
        holder.supportName_TV.setText(null);
        holder.deliveryTime_TV.setText(null);
        holder.lastChat_TV.setText(null);
        holder.newChatCount_TV.setText(null);
//        holder.lastChat_TV.setText(null);

        String supportUid = userMList.get(position).getSupportUid();
        String supportName = userMList.get(position).getSupportName();
        String deliveryStatus = userMList.get(position).getDeliveryStatus();
        long time = userMList.get(position).getTime();
        String chat = userMList.get(position).getChat();
        String chatCount = userMList.get(position).getChatCount();


        // set the date and time last chat was sent
        timeAndDateSent(time, holder);

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

        holder.deliveryStatus_IV.setImageResource(deliveryIcon);

        holder.supportName_TV.setText(supportName);
        holder.lastChat_TV.setText(chat);
        holder.newChatCount_TV.setText(chatCount);

        
        holder.itemView.setOnClickListener(v -> {
//            OpenActivityUtil.
            context.startActivity(new Intent(context, SupportChatActivity.class));

        });

    }

    // method

    private void timeAndDateSent(long lastTime, SupportViewHolder holder)
    {

        // current date and time
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(stamp.getTime());
        String currentDateString = String.valueOf(date);

        // last user date and time
        Date d = new Date(lastTime);    // convert the timestamp to current time
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);
        String previousDateString = String.valueOf(d);

        dateMonth = new HashMap<>();     // months
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

        dateNum = new HashMap<>();      // days
        dateNum.put("Mon", 1);
        dateNum.put("Tue", 2);
        dateNum.put("Wed", 3);
        dateNum.put("Thu", 4);
        dateNum.put("Fri", 5);
        dateNum.put("Sat", 6);
        dateNum.put("Sun", 7);

        String lastYear = previousDateString.substring(30, 34);  // last year

        int curMonth = dateMonth.get(currentDateString.substring(4,7));    // Months
        int lastMonth = dateMonth.get(previousDateString.substring(4,7));

        int curDay = dateNum.get(currentDateString.substring(0,3));         // Mon - Sun
        int lastDay = dateNum.get(previousDateString.substring(0,3));


        int dateCur = Integer.parseInt(currentDateString.substring(8, 10));    // day 1 - 30
        int dateLast = Integer.parseInt(previousDateString.substring(8, 10));

        if (curMonth - lastMonth == 0)
        {
            if (dateCur - dateLast < 7)
            {
                if(curDay - lastDay == 0)
                {
                    String newTime = context.getString(R.string.today) + " " + time.toLowerCase();
                    holder.deliveryTime_TV.setText(newTime);

                } else if (curDay - lastDay == 1)
                {
                    String newTime = context.getString(R.string.yesterday) + " " + time.toLowerCase();
                    holder.deliveryTime_TV.setText(newTime);

                } else if (curDay - lastDay == 2)
                {
                    String newTime = context.getString(R.string.day2ago) + " " + time.toLowerCase();
                    holder.deliveryTime_TV.setText(newTime);

                } else if (curDay - lastDay == 3)
                {
                    String newTime = context.getString(R.string.day3ago) + " " + time.toLowerCase();
                    holder.deliveryTime_TV.setText(newTime);

                } else if (curDay - lastDay == 4)
                {
                    String newTime = context.getString(R.string.day4ago) + " " + time.toLowerCase();
                    holder.deliveryTime_TV.setText(newTime);

                } else if (curDay - lastDay == 5)
                {
                    String newTime = context.getString(R.string.day5ago) + " " + time.toLowerCase();
                    holder.deliveryTime_TV.setText(newTime);

                } else if (curDay - lastDay == 6)
                {
                    String newTime = context.getString(R.string.day6ago) + " " + time.toLowerCase();
                    holder.deliveryTime_TV.setText(newTime);
                }

            } else if (dateCur - dateLast >= 7 && dateCur - dateLast < 14)
            {
                String newTime = context.getString(R.string.week1ago);
                holder.deliveryTime_TV.setText(newTime);

            } else if (dateCur - dateLast >= 14 && dateCur - dateLast < 21)
            {
                String newTime = context.getString(R.string.week2ago);
                holder.deliveryTime_TV.setText(newTime);

            } else if (dateCur - dateLast >= 21 && dateCur - dateLast < 27)
            {
                String newTime = context.getString(R.string.week3ago);
                holder.deliveryTime_TV.setText(newTime);
            } else
            {
                String newTime = context.getString(R.string.monthsAgo);
                holder.deliveryTime_TV.setText(newTime);
            }

        } else
        {
            String lastD = dateLast +"/"+ lastMonth+"/"+ lastYear;
            holder.deliveryTime_TV.setText(lastD);
        }

    }



    @Override
    public int getItemCount() {
        return userMList.size();
    }

    public class SupportViewHolder extends RecyclerView.ViewHolder {

        ImageView supportPhoto_IV, deliveryStatus_IV;
        TextView supportName_TV, deliveryTime_TV, lastChat_TV,  newChatCount_TV;

        public SupportViewHolder(@NonNull View itemView) {
            super(itemView);

            supportPhoto_IV = itemView.findViewById(R.id.imageViewUsers);
            supportName_TV = itemView.findViewById(R.id.textViewUser);
            deliveryStatus_IV = itemView.findViewById(R.id.imageViewDelivery);
            deliveryTime_TV = itemView.findViewById(R.id.textViewTime);
            lastChat_TV = itemView.findViewById(R.id.textViewMsg);
            newChatCount_TV = itemView.findViewById(R.id.textViewMsgCount);
//            supportName_TV = itemView.findViewById(R.id.textViewUser);

        }

    }
    
    
    
}
