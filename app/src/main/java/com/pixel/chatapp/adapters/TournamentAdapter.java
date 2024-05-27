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

import com.pixel.chatapp.R;
import com.pixel.chatapp.model.TournamentModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.TourViewHolder> {

    int ongoing;
    int upcoming;
    int session;
    int status;

    List<TournamentModel> modelList;
    Context context;

    public TournamentAdapter(List<TournamentModel> modelList, Context context)
    {
        this.modelList = modelList;
        this.context = context;

        ongoing = 1;
        upcoming = 2;
        session = 3;

        status = upcoming;

    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView;

        if(viewType == ongoing){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ongoing_tournament_card, parent, false);
        } else if (viewType == upcoming) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_tournament_card, parent, false);

        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tour_state_card, parent, false);
        }

        return new TourViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {

        TournamentModel tourModel = modelList.get(position);
        if(tourModel.getType() == session){
            holder.sessionTV.setText(null);

        } else {

            holder.gameTitle_TV.setText(null);
            holder.rewardPool_TV.setText(null);
            holder.sponsoredBy_TV.setText(null);
//            holder.gameLogo_IV.setImageResource(0);

            if(tourModel.getType() == upcoming){
                holder.startDateTV.setText(null);
                holder.entryFeeTV.setText(null);
            }
        }

        String title = tourModel.getTitle();
        String entryFee = tourModel.getEntryFee();
        String reward = tourModel.getReward();
        String gameType = tourModel.getGameType();
        String startDate = tourModel.getStartDate();
        String numOfParticipant = tourModel.getNumOfParticipant();
        String stage = tourModel.getStage();
        String tourLink = tourModel.getTourLink();
        String communityLink = tourModel.getCommunityLink();
        String sponsoredBy = tourModel.getSponsoredBy();
        String remark = tourModel.getRemark();
        String sessionState = tourModel.getSessionState();
        int type = tourModel.getType();

        //  ongoing = 1;    upcoming = 2;   session = 3;
        if(tourModel.getType() == session)
        {
            holder.sessionTV.setText(sessionState);

        } else {

            holder.gameTitle_TV.setText(title);

            String rewardS = context.getString(R.string.rewardPool) + " " + reward;
            holder.rewardPool_TV.setText(rewardS);

            String sponsor = context.getString(R.string.sponsorBy) + " " + sponsoredBy;
            holder.sponsoredBy_TV.setText(sponsor);
//            holder.gameLogo_IV.setImageResource(0);

            if(tourModel.getType() == upcoming){
                String entry = context.getString(R.string.entryFee) + " " + entryFee;
                holder.entryFeeTV.setText(entry);

                String start = context.getString(R.string.starts) + " " + startDate;
                holder.startDateTV.setText(start);
            }
        }

        holder.itemView.setOnClickListener(v -> {

            if(type == ongoing)
            {
                Toast.makeText(context, "live view in progress", Toast.LENGTH_SHORT).show();

            } else if (type == upcoming)
            {
                Toast.makeText(context, "upcoming register in progress", Toast.LENGTH_SHORT).show();
            }

        });

        holder.itemView.setOnLongClickListener(v -> true);

    }

    //  ==========      methods

    private void StartDate(long lastTime, TourViewHolder holder)
    {
        Date d = new Date(lastTime);    // convert the timestamp to current time
        DateFormat formatter = new SimpleDateFormat("h:mm a");
        String time = formatter.format(d);

        long currentTimeMillis = System.currentTimeMillis();

        long timeDifferenceMillis = currentTimeMillis - lastTime;

        // Convert milliseconds to hours
        long timeDifferenceHours = timeDifferenceMillis / (1000 * 60 * 60);

        if (timeDifferenceHours < 24) {
            holder.startDateTV.setText(time);
        }else if (timeDifferenceHours <= 48) {
            holder.startDateTV.setText(context.getString(R.string.yesterday));
        } else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
            String formattedDate = sdf.format(d);
            holder.startDateTV.setText(formattedDate);
        }

    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class TourViewHolder extends RecyclerView.ViewHolder {

        TextView gameTitle_TV;
        TextView rewardPool_TV;
        TextView sponsoredBy_TV;
        TextView startDateTV;
        TextView entryFeeTV;
        TextView sessionTV;
        ImageView gameLogo_IV;

        public TourViewHolder(@NonNull View itemView) {

            super(itemView);


            if(status == session){
                sessionTV = itemView.findViewById(R.id.sessionTV);

            } else
            {
                gameTitle_TV = itemView.findViewById(R.id.gameTitle_TV);
                rewardPool_TV = itemView.findViewById(R.id.rewardPool_TV);
                sponsoredBy_TV = itemView.findViewById(R.id.sponsoredByTV);
                gameLogo_IV = itemView.findViewById(R.id.gameLogo_IV);

                if (status == upcoming) {
                    startDateTV = itemView.findViewById(R.id.startDateTV);
                    entryFeeTV = itemView.findViewById(R.id.entryFeeTV);
                }
            }

        }


    }


    @Override
    public int getItemViewType(int position) {

        //  ongoing = 1;    upcoming = 2;   session = 3;

        TournamentModel tourModel = modelList.get(position);

        if(tourModel.getType() == ongoing)
        {
            status = ongoing;

        } else if (tourModel.getType() == upcoming)
        {
            status = upcoming;

        } else {
            status = session;
        }

        return status;

    }

}
