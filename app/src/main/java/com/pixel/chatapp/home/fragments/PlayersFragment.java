package com.pixel.chatapp.home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;

public class PlayersFragment extends Fragment {

    public static PlayersFragment newInstance(){
        return new PlayersFragment();
    }

    RecyclerView recyclerViewPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.players_fragment, container, false);

        recyclerViewPlayer = view.findViewById(R.id.recyclerViewPlayer);

        recyclerViewPlayer.setLayoutManager(new LinearLayoutManager(getContext()));




        return view;

    }




}













