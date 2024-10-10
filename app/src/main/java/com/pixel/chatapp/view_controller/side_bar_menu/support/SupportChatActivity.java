package com.pixel.chatapp.view_controller.side_bar_menu.support;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.SupportChatAdapter;
import com.pixel.chatapp.dataModel.SupportChatM;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SupportChatActivity extends AppCompatActivity {

    ImageView arrowBack, audioCall_IV, chatMenu, cancelReply_IV, editOrReplyIcon;
    CircleImageView circleImageLogo, fab;
    TextView supportNameTV, replyText_TV, replyFromTV, replyOrEditStatus_TV;
    RecyclerView recyclerViewChat;
    CardView cardViewReply;
    EditText chats_ET;

    SupportChatAdapter chatAdapter;
    List<SupportChatM> chatMList;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat);

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        arrowBack = findViewById(R.id.imageViewBackArrow);
        circleImageLogo = findViewById(R.id.circleImageLogo);
        supportNameTV = findViewById(R.id.textViewName);
//        hint = findViewById(R.id.textViewBio);
        audioCall_IV = findViewById(R.id.audioCall_IV);
        chatMenu = findViewById(R.id.imageViewUserMenu2);

        cardViewReply = findViewById(R.id.cardViewReply);
        replyText_TV = findViewById(R.id.textViewReplyText);
        cancelReply_IV = findViewById(R.id.imageViewCancleReply);
        editOrReplyIcon = findViewById(R.id.editOrReplyImage);
        replyFromTV = findViewById(R.id.fromTV);
        replyOrEditStatus_TV = findViewById(R.id.textReplying);
        chats_ET = findViewById(R.id.editTextMessage);
        fab = findViewById(R.id.fab);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));

        getOnBackPressedDispatcher().addCallback(this, callback);

        chatMList = new ArrayList<>();

        chatAdapter = new SupportChatAdapter(chatMList, this);
        recyclerViewChat.setAdapter(chatAdapter);

        fab.setOnClickListener(v -> {

            String chat = chats_ET.getText().toString().trim();

            if(chat.isEmpty()){
                Toast.makeText(this, getString(R.string.fieldEmpty_), Toast.LENGTH_SHORT).show();
            } else {

                SupportChatM chatM = new SupportChatM(null, user.getUid(), 1, chat, null,
                        null, null, System.currentTimeMillis(), "700024",
                        null, null, null, null);


                chatAdapter.addNewMessageDB(chatM);

                recyclerViewChat.scrollToPosition(chatAdapter.getItemCount()-1);

                chats_ET.setText(null);

            }


        });

        cancelReply_IV.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }



    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed()
        {

            setEnabled(false);
            getOnBackPressedDispatcher().onBackPressed();
            setEnabled(true);
        }
    };

}