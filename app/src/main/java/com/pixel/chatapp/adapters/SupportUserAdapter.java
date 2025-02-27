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
import com.pixel.chatapp.dataModel.SupportUserM;
import com.pixel.chatapp.utilities.TimeUtils;
import com.pixel.chatapp.view_controller.side_bar_menu.support.SupportChatActivity;

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

        return new SupportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SupportUserAdapter.SupportViewHolder holder, int position) {

//        holder.supportPhoto_IV.setImageResource(0);
        holder.supportName_TV.setText(null);
//        holder.deliveryStatus_IV.setImageResource(0);
        holder.deliveryTime_TV.setText(null);
        holder.lastChat_TV.setText(null);
//        holder.lastChat_TV.setText(null);
        holder.newChatCount_TV.setText(null);

//        String supportUid = userMList.get(position).getSupportUid();
        String supportName = userMList.get(position).getSupportName();
        String deliveryStatus = userMList.get(position).getDeliveryStatus();
        long time = userMList.get(position).getTime();
        String chat = userMList.get(position).getChat();
        String chatCount = userMList.get(position).getChatCount();


        // set the date and time last chat was sent
        timeAndDateSent(time, holder);

        // set msgStatus if last chat was from own user
        int deliveryIcon = switch (deliveryStatus) {
            case "700024" ->    // delivery
                    R.drawable.message_tick_one;
            case "700016" ->   // read
                    R.drawable.baseline_grade_24;
            case "0" -> 0;
            default -> R.drawable.message_load;
        };

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

    private void timeAndDateSent(long lastTime, SupportViewHolder holder) {
        // Get current time and last message time as Date objects
        Date currentDate = new Date();
        Date lastMessageDate = new Date(lastTime);  // Convert lastTime (timestamp) to Date

        // Calculate time difference in days
        int dayDifference = TimeUtils.calculateDayDifference(currentDate, lastMessageDate);
        String time = TimeUtils.getFormattedTime(lastTime);

        if (TimeUtils.isSameYear(currentDate, lastMessageDate))
        {
            int monthDifference = TimeUtils.calculateMonthDifference(currentDate, lastMessageDate); // Add this
            if (TimeUtils.isSameMonth(currentDate, lastMessageDate))
            {
                if (dayDifference == 0) {
                    holder.deliveryTime_TV.setText(String.format("%s %s", context.getString(R.string.today), time.toLowerCase()));
                } else if (dayDifference == 1) {
                    holder.deliveryTime_TV.setText(String.format("%s %s", context.getString(R.string.yesterday), time.toLowerCase()));
                } else if (dayDifference < 7) {
                    holder.deliveryTime_TV.setText(String.format("%s %s", context.getString(R.string.daysAgo, dayDifference), time.toLowerCase()));
                } else {
                    holder.deliveryTime_TV.setText(context.getString(R.string.weeksAgo, dayDifference / 7));
                }
            } else { // Return "X months ago" if it's more than one month ago
                if (monthDifference == 1) {
                    holder.deliveryTime_TV.setText(context.getString(R.string.monthAgo)); // If it’s exactly 1 month ago
                } else {
                    holder.deliveryTime_TV.setText(context.getString(R.string.monthsAgo, monthDifference));
                }
            }

        } else {    // If it's from a previous year
            String lastDateString = TimeUtils.getShortFormattedDate(lastMessageDate);
            holder.deliveryTime_TV.setText(lastDateString);
        }
    }


    @Override
    public int getItemCount() {
        return userMList.size();
    }

    public static class SupportViewHolder extends RecyclerView.ViewHolder {

        ImageView supportPhoto_IV, deliveryStatus_IV;
        TextView supportName_TV, deliveryTime_TV, lastChat_TV,  newChatCount_TV;

        public SupportViewHolder(@NonNull View itemView) {
            super(itemView);

            supportPhoto_IV = itemView.findViewById(R.id.imageViewUsers);
            supportName_TV = itemView.findViewById(R.id.textViewUser);
            deliveryStatus_IV = itemView.findViewById(R.id.imageViewDelivery);
            deliveryTime_TV = itemView.findViewById(R.id.dateTime_TV);
            lastChat_TV = itemView.findViewById(R.id.textViewMsg);
            newChatCount_TV = itemView.findViewById(R.id.textViewMsgCount);
//            supportName_TV = itemView.findViewById(R.id.textViewUser);

        }

    }
    
    
    
}
