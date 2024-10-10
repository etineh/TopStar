package com.pixel.chatapp.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.dataModel.LeagueModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LeagueAdapter extends RecyclerView.Adapter<LeagueAdapter.HostViewHolder> {

    List<LeagueModel> leagueModelList;
    Context context;

    public LeagueAdapter(List<LeagueModel> leagueModelList, Context context) {
        this.leagueModelList = leagueModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.league_card, parent, false);
        return new HostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {

        setColours(holder);

        LeagueModel leagueModel = leagueModelList.get(position);

        holder.hostTitle_TV.setText(null);
        holder.entryFee_TV.setText(null);
        holder.reward_TV.setText(null);
        holder.gameType_TV.setText(null);
        holder.timeCreated_TV.setText(null);
//        holder.shareIV.setImageResource(0);
//        holder.gamePhoto_IV.setImageResource(0);

        String title = leagueModel.getTitle();
        String entryFee = leagueModel.getEntryFee();
        String reward = leagueModel.getReward();
        String gameType = leagueModel.getGameType();
        long timeCreated = leagueModel.getTimeCreated();
        String whoCanParticipate = leagueModel.getWhoCanParticipate();
        String minimumSlot = leagueModel.getMinimumSlot();
        String leagueLink = leagueModel.getLeagueLink();
        String communityLink = leagueModel.getCommunityLink();
        String sponsoredBy = leagueModel.getSponsoredBy();
        String Remark = leagueModel.getRemark();


        holder.hostTitle_TV.setText(title);

        String entry = context.getString(R.string.entryFee) +" "+ entryFee;
        holder.entryFee_TV.setText(entry);

        String rewardS = context.getString(R.string.rewardPool) +" "+ reward;
        holder.reward_TV.setText(rewardS);

        String game = context.getString(R.string.game_) +" "+ gameType;
        holder.gameType_TV.setText(game);

        timeAndDateSent(timeCreated, holder);

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "work in progress", Toast.LENGTH_SHORT).show();
        });

        holder.shareIV.setOnClickListener(v -> {
            Toast.makeText(context, "Sharing in progress", Toast.LENGTH_SHORT).show();
        });

    }


    //  =======     methods

    private void setColours(HostViewHolder holder)
    {
        if(MainActivity.nightMood){
            holder.cardContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.blackApp));
            holder.hostTitle_TV.setTextColor(ContextCompat.getColor(context, R.color.cool_orange));
            holder.shareIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cool_orange)));
            holder.entryFee_TV.setTextColor(ContextCompat.getColor(context, R.color.defaultWhite));
            holder.reward_TV.setTextColor(ContextCompat.getColor(context, R.color.defaultWhite));
            holder.gameType_TV.setTextColor(ContextCompat.getColor(context, R.color.defaultWhite));
            holder.join.setBackgroundResource(R.drawable.round_button_dark);
            holder.lineH.setBackgroundColor(ContextCompat.getColor(context, R.color.blackLine));

        } else {
            holder.cardContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.hostTitle_TV.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.shareIV.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange)));
            holder.entryFee_TV.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.reward_TV.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.gameType_TV.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.join.setBackgroundResource(R.drawable.round_button_orange);
            holder.lineH.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

        }
    }


    private void timeAndDateSent(long lastTime, HostViewHolder holder)
    {
        Date d = new Date(lastTime);    // convert the timestamp to current time
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

        long currentTimeMillis = System.currentTimeMillis();

        long timeDifferenceMillis = currentTimeMillis - lastTime;

        // Convert milliseconds to hours
        long timeDifferenceHours = timeDifferenceMillis / (1000 * 60 * 60);

        String timeCreate;
        if (timeDifferenceHours < 24) {
            timeCreate = context.getString(R.string.created) + " " + time;
            holder.timeCreated_TV.setText(timeCreate);
        }else if (timeDifferenceHours <= 48) {
            timeCreate = context.getString(R.string.created) + " " +context.getString(R.string.yesterday);
            holder.timeCreated_TV.setText(timeCreate);
        } else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
            String formattedDate = sdf.format(d);
            timeCreate = context.getString(R.string.created) + " " + formattedDate;
            holder.timeCreated_TV.setText(timeCreate);
        }

    }


    @Override
    public int getItemCount() {
        if(leagueModelList != null){
            return leagueModelList.size();
        }
        return 0;
    }

    public class HostViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout cardContainer;
        TextView hostTitle_TV, lineH;
        TextView entryFee_TV;
        TextView reward_TV;
        TextView gameType_TV , join;
        TextView timeCreated_TV;
        ImageView shareIV;
        ImageView gamePhoto_IV;

        public HostViewHolder(@NonNull View itemView) {
            super(itemView);

            join = itemView.findViewById(R.id.playOnclick);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            hostTitle_TV = itemView.findViewById(R.id.hostTitle_TV);
            lineH = itemView.findViewById(R.id.lineH);
            entryFee_TV = itemView.findViewById(R.id.entryFee_TV);
            reward_TV = itemView.findViewById(R.id.reward_TV);
            gameType_TV = itemView.findViewById(R.id.gameType_TV);
            timeCreated_TV = itemView.findViewById(R.id.timeCreated_TV);
            shareIV = itemView.findViewById(R.id.shareIV);
            gamePhoto_IV = itemView.findViewById(R.id.gamePhoto_IV);
//            shareIV = itemView.findViewById(R.id.shareIV);

        }
    }

}
