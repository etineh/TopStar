package com.pixel.chatapp;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.pixel.chatapp.adapters.ChatListAdapter;

import java.util.List;

public interface FragmentListener {
    void onFragmentAction();
    void logoutOption();
    void updateTextView(String data);

    void onItemClick(List<String> itemList);

    void onClickOpenMessage(ConstraintLayout itemLayout);

}
