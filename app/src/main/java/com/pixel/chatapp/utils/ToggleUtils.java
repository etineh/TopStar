package com.pixel.chatapp.utils;

import android.view.View;

import com.pixel.chatapp.R;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.MessageModel;

public class ToggleUtils {

    public static void togglePinAndShareIcon(MessageModel model, int totalSize, int chatPosition, String myId)
    {
        int positionCheck = totalSize - chatPosition;    // e.g 1000 - 960 => 40
        if(positionCheck < 100)
        {
            MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_mode_edit_24);
            if(model.getFromUid().equals(myId)) {
                MainActivity.editChatOption_IV.setVisibility(View.VISIBLE);
            }

        } else {
            MainActivity.editChatOption_IV.setVisibility(View.GONE);
        }

        if (model.getType() != 0 || (!model.getFromUid().equals(myId) && model.getType() != 0) )
        {   // set image and document => type 0 is for just text-chat, type 1 is voice_note, type 2 is photo, type 3 is document, type 4 is audio (mp3)
            MainActivity.editChatOption_IV.setImageResource(R.drawable.baseline_share_24);
            MainActivity.onShare = true;

            MainActivity.editChatOption_IV.setVisibility(View.VISIBLE);

        } else {
            MainActivity.onShare = false;
        }

        MainActivity.modelChatsOption = model;  // assign the last model
    }
}
