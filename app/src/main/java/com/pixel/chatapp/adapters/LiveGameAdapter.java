package com.pixel.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.Hold;
import com.pixel.chatapp.R;
import com.pixel.chatapp.model.LiveGameM;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LiveGameAdapter extends RecyclerView.Adapter<LiveGameAdapter.LiveVIewHolder> {

    private List<LiveGameM> gameMList;
    private Context context;

    public LiveGameAdapter(List<LiveGameM> gameMList, Context context) {
        this.gameMList = gameMList;
        this.context = context;
    }

    @NonNull
    @Override
    public LiveVIewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_card, parent, false);
        return new LiveVIewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveVIewHolder holder, int position) {

        holder.totalWatch_TV.setText(null);
        holder.gameTitleTV.setText(null);
        holder.gameName_TV.setText(null);
        holder.totalPlayerTV.setText(null);
        holder.currentPlayer_TV.setText(null);
        holder.winnerPrize_TV.setText(null);
        holder.sponsoredBy_TV.setText(null);
//        holder.player1Photo_IV.setImageResource(0);
//        holder.player2Photo_IV.setImageResource(0);


        LiveGameM liveGameM = gameMList.get(position);

        String totalWatch = liveGameM.getTotalWatch();
        String gameTitle = liveGameM.getGameTitle();
        String gameName = liveGameM.getGameName();
        String totalPlayer = liveGameM.getTotalPlayer();
        String currentPlayer = liveGameM.getCurrentPlayer();
        String winnerPrize = liveGameM.getWinnerPrize();
        String sponsoredBy = liveGameM.getSponsoredBy();
        String player1Photo = liveGameM.getPlayer1Photo();
        String player2Photo = liveGameM.getPlayer2Photo();


        String totalWatchS = "👁 " + totalWatch + " " + context.getString(R.string.watching);
        holder.totalWatch_TV.setText(totalWatchS);

        holder.gameTitleTV.setText(gameTitle);

        String gameNameS = context.getString(R.string.game) + ": " + gameName;
        holder.gameName_TV.setText(gameNameS);

        String totalPlayerS = totalPlayer + " " + context.getString(R.string.players);
        holder.totalPlayerTV.setText(totalPlayerS);

        holder.currentPlayer_TV.setText(currentPlayer);

        String winnerPrizeS = context.getString(R.string.grandPrize) + " " + winnerPrize;
        holder.winnerPrize_TV.setText(winnerPrizeS);

        String sponsoredByS = context.getString(R.string.sponsorBy) + " " + sponsoredBy;
        holder.sponsoredBy_TV.setText(sponsoredByS);

        if (player1Photo != null && !player1Photo.isEmpty())
            Picasso.get().load(player1Photo).into(holder.player1Photo_IV);
        if (player2Photo != null && !player2Photo.isEmpty())
            Picasso.get().load(player2Photo).into(holder.player2Photo_IV);


        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show();
        });


        holder.itemView.setOnLongClickListener(v -> true);

    }

    @Override
    public int getItemCount() {
        if(gameMList.size() > 0){
            return gameMList.size();
        }
        return 0;
    }

    public class LiveVIewHolder extends RecyclerView.ViewHolder {

        TextView totalWatch_TV;
        TextView gameTitleTV;
        TextView gameName_TV;
        TextView totalPlayerTV;
        TextView currentPlayer_TV;
        TextView winnerPrize_TV;
        TextView sponsoredBy_TV;
        CircleImageView player1Photo_IV;
        CircleImageView player2Photo_IV;
        //        TextView totalWatch_TV;

        public LiveVIewHolder(@NonNull View itemView) {
            super(itemView);

            totalWatch_TV = itemView.findViewById(R.id.totalWatch_TV);
            gameTitleTV = itemView.findViewById(R.id.gameTitleTV);
            gameName_TV = itemView.findViewById(R.id.gameName_TV);
            totalPlayerTV = itemView.findViewById(R.id.totalPlayerTV);
            currentPlayer_TV = itemView.findViewById(R.id.currentPlayer_TV);
            winnerPrize_TV = itemView.findViewById(R.id.winnerPrize_TV);
            sponsoredBy_TV = itemView.findViewById(R.id.sponsoredBy_TV);
            player1Photo_IV = itemView.findViewById(R.id.player1_IV);
            player2Photo_IV = itemView.findViewById(R.id.player2_IV);
//            winnerPrize_TV = itemView.findViewById(R.id.winnerPrize_TV);

        }
    }


}
