package com.pixel.chatapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.TimeUtils;
import com.pixel.chatapp.model.GameRankM;
import com.pixel.chatapp.model.PlayerModel;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.internal.Util;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private Context context;
    private final List<PlayerModel> playerModelList;

    private int status;
    private final int fromWhere;
    private final int player_card;

    public PlayerAdapter(Context context, List<PlayerModel> playerModelList) {
        this.context = context;
        this.playerModelList = playerModelList;

        fromWhere = 1;
        player_card = 2;

        status = player_card;

    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        if(viewType == player_card){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.players_card, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.from_where_card, parent, false);
        }

        return new PlayerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {

        PlayerModel playerModel = playerModelList.get(position);

        if(playerModel.getType() == player_card)
        {
            holder.playerName_TV.setText(null);
            holder.mode_TV.setText(null);
            holder.stakeAmount_TV.setText(null);
            holder.stakeAmount_TV.setVisibility(View.GONE);
            holder.gameType_TV.setText(null);
            holder.timeCreated_TV.setText(null);
//            holder.playerPhoto_IV.setImageResource(0);

        } else {
            holder.fromWhereTV.setText(null);
        }

        String fromUID = playerModel.getFromUID();
        String playerName = playerModel.getPlayerName();
        String gameMode = playerModel.getMode();
        String stakeAmount = playerModel.getAmount();
        String gameTypes = playerModel.getGameType();
        long timeCreated = playerModel.getTimeCreated();
        String photoLink = playerModel.getPhotoLink();
        String fromWhere_ = playerModel.getFromWhere();

        if (playerModel.getType() == player_card){

            holder.playerName_TV.setText(playerName);
            holder.mode_TV.setText(gameMode);

            if(gameMode.toLowerCase().contains("stake") && stakeAmount != null){
                holder.stakeAmount_TV.setText(stakeAmount);
                holder.stakeAmount_TV.setVisibility(View.VISIBLE);
            }

            holder.gameType_TV.setText(gameTypes);

            timeAndDateSent(timeCreated, holder);

//        holder.playerPhoto_IV.setImageResource(0);


            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show();
            });

        } else {
            holder.fromWhereTV.setText(fromWhere_);
            holder.dropDownIV.setOnClickListener(v -> {
                Toast.makeText(context, "work in progress", Toast.LENGTH_SHORT).show();
            });

        }


        holder.itemView.setOnLongClickListener(v -> true);


    }

    //  ======  methods

    private void timeAndDateSent(long lastTime, PlayerViewHolder holder) {
        // Convert the timestamp to the Date object
        Date d = new Date(lastTime);
        // Format the time part
        DateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        String time = timeFormatter.format(d);

        if (TimeUtils.compareDays(lastTime) == 0) {
            holder.timeCreated_TV.setText(time);
        } else if (TimeUtils.compareDays(lastTime) == 1) {
            holder.timeCreated_TV.setText(context.getString(R.string.yesterday));
        } else {
            @SuppressLint("SimpleDateFormat") DateFormat dateFormatter = new SimpleDateFormat("MMMM dd");
            String formattedDate = dateFormatter.format(d);
            holder.timeCreated_TV.setText(formattedDate);
        }

    }



    @Override
    public int getItemCount() {
        return playerModelList.size();
    }

    public class PlayerViewHolder extends RecyclerView.ViewHolder {

        private TextView playerName_TV, mode_TV, stakeAmount_TV, gameType_TV, timeCreated_TV, fromWhereTV;
        private ImageView playerPhoto_IV, dropDownIV;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);

            if(status == player_card)
            {
                playerName_TV = itemView.findViewById(R.id.playerName_TV);
                mode_TV = itemView.findViewById(R.id.mode_TV);
                stakeAmount_TV = itemView.findViewById(R.id.stakeAmount_TV);
                gameType_TV = itemView.findViewById(R.id.gameType_TV);
                timeCreated_TV = itemView.findViewById(R.id.timeCreated_TV);
                playerPhoto_IV = itemView.findViewById(R.id.playerPhoto_IV);

            } else {
                fromWhereTV = itemView.findViewById(R.id.fromWhereTV);
                dropDownIV = itemView.findViewById(R.id.dropDownIV);
                
            }

        }
    }


    @Override
    public int getItemViewType(int position) {

        //  1 is fromWhere, 2 is player cardVIew

        PlayerModel playerModel = playerModelList.get(position);

        if(playerModel.getType() == fromWhere)
        {
            status = fromWhere;
            return fromWhere;

        } else {
            status = player_card;
            return player_card;
        }

    }


}
