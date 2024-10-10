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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.utilities.TimeUtils;
import com.pixel.chatapp.dataModel.PlayerModel;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private final Context context;
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
            setColours(holder);

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

            if(photoLink != null && !photoLink.isEmpty() && !photoLink.equals("null")) {
                Picasso.get().load(photoLink).into(holder.playerPhoto_IV);
            } else {
                holder.playerPhoto_IV.setImageResource(R.drawable.person_round);
            }


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

//System.out.println("what is printing: " + position);
    }

    private void setColours(PlayerViewHolder holder) {

        if(MainActivity.nightMood) {
            holder.playerContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.blackApp));
            holder.playerName_TV.setTextColor(ContextCompat.getColor(context, R.color.cool_orange));
            holder.lineH.setBackgroundColor(ContextCompat.getColor(context, R.color.blackLine));
            holder.gameType_TV.setTextColor(ContextCompat.getColor(context, R.color.defaultWhite));
            holder.mode_TV.setTextColor(ContextCompat.getColor(context, R.color.defaultWhite));
            holder.playOnclick.setBackgroundResource(R.drawable.round_button_dark);
            holder.stakeAmount_TV.setTextColor(ContextCompat.getColor(context, R.color.defaultWhite));

        } else {
            holder.playerContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.playerName_TV.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.lineH.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.gameType_TV.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.mode_TV.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.playOnclick.setBackgroundResource(R.drawable.round_button_orange);
            holder.stakeAmount_TV.setTextColor(ContextCompat.getColor(context, R.color.black));

        }

    }

    //  ======  methods

    private void timeAndDateSent(long lastTime, PlayerViewHolder holder) {
        // Convert the timestamp to the Date object
        Date d = new Date(lastTime);
        // Format the time part
        DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

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

        ConstraintLayout playerContainer;
        private TextView playerName_TV, mode_TV, stakeAmount_TV, gameType_TV, timeCreated_TV, fromWhereTV, lineH, playOnclick;
        private ImageView playerPhoto_IV, dropDownIV;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);

            if(status == player_card)
            {
                playerContainer = itemView.findViewById(R.id.playerContainer);
                playerName_TV = itemView.findViewById(R.id.playerName_TV);
                mode_TV = itemView.findViewById(R.id.mode_TV);
                stakeAmount_TV = itemView.findViewById(R.id.stakeAmount_TV);
                gameType_TV = itemView.findViewById(R.id.gameType_TV);
                timeCreated_TV = itemView.findViewById(R.id.timeCreated_TV);
                playerPhoto_IV = itemView.findViewById(R.id.playerPhoto_IV);
                lineH = itemView.findViewById(R.id.lineH);
                playOnclick = itemView.findViewById(R.id.playOnclick);
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
