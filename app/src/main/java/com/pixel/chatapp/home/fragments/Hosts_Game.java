package com.pixel.chatapp.home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pixel.chatapp.R;

public class Hosts_Game extends Fragment {

    public static Hosts_Game newInstance(){
        return new Hosts_Game();
    }

    TextView textView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hosts_fragment, container, false);

        textView = view.findViewById(R.id.textView);

        return view;
    }

}
