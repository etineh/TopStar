package com.pixel.chatapp.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.UsersAdapter;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.model.ContactModel;

import java.util.ArrayList;
import java.util.List;

public class UsersContactActivity extends AppCompatActivity implements UsersAdapter.BackButtonClickListener{

    RecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference refUsers;

    List<ContactModel> list;
//    List<String> names;
    UsersAdapter adapter;

    ImageView imageViewBack;
    CardView groupAndChannel, addContact, inviteFriends;

    MainActivity mainActivity = new MainActivity();
    private FragmentListener fragmentListener; // A field to store the listener reference


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_contact);

        groupAndChannel = findViewById(R.id.cardViewGroup);
        addContact = findViewById(R.id.cardViewAdd);
        inviteFriends = findViewById(R.id.cardViewInvite);
        imageViewBack = findViewById(R.id.imageViewBack);
        recyclerView = findViewById(R.id.recyclerViewContacts);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        refUsers = database.getReference();

        imageViewBack.setOnClickListener(view -> onBackPressed());

        fragmentListener = mainActivity.getMainActivityContext();

        adapter = new UsersAdapter(list, UsersContactActivity.this);
        adapter.setListener(fragmentListener);
        adapter.setBackButtonClickListener(UsersContactActivity.this); // "this" refers to the UserContactActivity

        recyclerView.setAdapter(adapter);

        getUsers();

    }

    //  ----------- interface   ------------
    @Override
    public void onBackButtonClicked() {
        // Close the activity
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainActivityIntent);
        finish();
    }

    // -----------  methods -----------
    public void getUsers(){

        refUsers.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                // get my username
                String myUserName = snapshot.child(user.getUid()).child("userName").getValue().toString();

                for (DataSnapshot userDetails : snapshot.getChildren()) {

                    // to get the uid all my contact user child
                    String key = userDetails.getKey();

                    if (!key.equals(user.getUid())){// if the key id is not mine, then it should fetch out other user id
                        ContactModel contactModel = userDetails.getValue(ContactModel.class);
                        contactModel.setOtherUid(key);
                        contactModel.setMyUserName(myUserName);
                        list.add(contactModel);
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onBackButtonClicked();
    }
}






