package com.pixel.chatapp.home.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.TournamentAdapter;
import com.pixel.chatapp.model.TournamentModel;

import java.util.ArrayList;
import java.util.List;

public class TournamentsFragment extends Fragment {

    public static TournamentsFragment newInstance(){
        return new TournamentsFragment();
    }

    RecyclerView recyclerViewTour;
    ProgressBar progressBarTour;
    List<TournamentModel> tourModelList;
    TournamentAdapter tournamentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tournaments_fragment, container, false);

        recyclerViewTour = view.findViewById(R.id.recyclerViewTour);
        progressBarTour = view.findViewById(R.id.progressBarTour);

        recyclerViewTour.setLayoutManager(new LinearLayoutManager(getContext()));

        tourModelList = new ArrayList<>();

        setTourModelList();

        tournamentAdapter = new TournamentAdapter(tourModelList, getContext());
        new Handler().postDelayed(()->{

            recyclerViewTour.setAdapter(tournamentAdapter);
            progressBarTour.setVisibility(View.GONE);

        }, 50);

        return view;

    }

    private void setTourModelList()
    {
        //  ========    ongoing

        tourModelList.add(new TournamentModel("World Poker Tournament", "", "",
                "", "", "", "", "", "", "",
                "", 3, getString(R.string.ongoingTour)));

        tourModelList.add(new TournamentModel("Universities Chess Tournament", "$1", "$5000",
                "chess", "Jan 23", "2000", "Group stage", "", "", "@uniuyo",
                "", 1, getString(R.string.ongoingTour)));

        tourModelList.add(new TournamentModel("World Poker Tournament", "free", "$2000",
                "Whot", "Jan 23", "2000", "Group stage", "", "", "TopStar",
                "", 1, getString(R.string.ongoingTour)));

        //  ========    upcoming

        tourModelList.add(new TournamentModel("World Chess Tournament", "$1", "$3500",
                "", "", "", "", "", "", "",
                "", 3, getString(R.string.upcomingTour)));

        tourModelList.add(new TournamentModel("State Chess Tournament", "$1", "$3500",
                "chess", "Jul 03", "2000", "Group stage", "", "", "AfricInnovate Community",
                "", 2, getString(R.string.ongoingTour)));

        tourModelList.add(new TournamentModel("World Whot Tournament", "free", "$5500",
                "whot", "Sept 23", "2000", "Group stage", "", "", "GDG Uyo",
                "", 2, getString(R.string.ongoingTour)));

        tourModelList.add(new TournamentModel("World Riddle Tournament", "free", "$2500",
                "riddle", "Oct 23", "2000", "Group stage", "", "", "@KingChal",
                "", 2, getString(R.string.ongoingTour)));

        tourModelList.add(new TournamentModel("World Scrabble Tournament", "free", "$7100",
                "scrabble", "Jan 23", "2000", "Group stage", "", "", "Shuttle Community",
                "", 2, getString(R.string.ongoingTour)));
    }


}





