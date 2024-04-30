package com.pixel.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.model.GameRankM;
import com.pixel.chatapp.model.UpcomingHostM;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GameRankAdapter extends RecyclerView.Adapter<GameRankAdapter.RankViewHolder> {

    private Context context;
    private final List<GameRankM> hostMList;

    public GameRankAdapter(Context context, List<GameRankM> hostMList) {
        this.context = context;
        this.hostMList = hostMList;
    }


    @NonNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_rank_card, parent, false);
        return new GameRankAdapter.RankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder holder, int position) {

        holder.gameHeading.setText(null);
        holder.totalPlay_TV.setText(null);
        holder.totalWin_TV.setText(null);
        holder.totalLoss_TV.setText(null);
        holder.worldRank_TV.setText(null);


        String gameHeading = hostMList.get(position).getGameHeading();
        String totalPlay = hostMList.get(position).getTotalPlay();
        String totalWin = hostMList.get(position).getTotalWin();
        String totalLoss = hostMList.get(position).getTotalLoss();
        String worldRank = hostMList.get(position).getWorldRank();

        holder.gameHeading.setText(gameHeading);
        holder.totalPlay_TV.setText(totalPlay);
        holder.totalWin_TV.setText(totalWin);
        holder.totalLoss_TV.setText(totalLoss);
        holder.worldRank_TV.setText(worldRank);
    }

    @Override
    public int getItemCount() {
        return hostMList.size();
    }

    public class RankViewHolder extends RecyclerView.ViewHolder {

//        private CircleImageView gameLogo_IV;
        private TextView gameHeading, totalPlay_TV, totalWin_TV, totalLoss_TV, worldRank_TV;

        public RankViewHolder(@NonNull View itemView) {
            super(itemView);
            gameHeading = itemView.findViewById(R.id.gameHeading);
            totalPlay_TV = itemView.findViewById(R.id.totalPlay_TV);
            totalWin_TV = itemView.findViewById(R.id.totalWin_TV);
            totalLoss_TV = itemView.findViewById(R.id.totalLoss_TV);
            worldRank_TV = itemView.findViewById(R.id.worldRank_TV);

        }
    }

}
