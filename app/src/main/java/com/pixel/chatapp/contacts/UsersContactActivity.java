package com.pixel.chatapp.contacts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.UsersAdapter;

import java.util.ArrayList;
import java.util.List;

public class UsersContactActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference dbReference;

    String userName;
    List<String> list;
//    List<String> names;
    UsersAdapter adapter;

    ImageView imageViewBack;
    CardView groupAndChannel, addContact, inviteFriends;

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
        dbReference = database.getReference();

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                onBackPressed();
                finish();
            }
        });

        dbReference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName = snapshot.child("userName").getValue().toString();
                adapter = new UsersAdapter(list, UsersContactActivity.this, userName);

//                ------- this will make the page load each sent
//                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getUsers();

    }

    public void getUsers(){

        dbReference.child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                // to get the position of each user child id
                String key = snapshot.getKey();
                if (!key.equals(user.getUid())){// if the key id is not mine, then it should fetch out other user id
                    list.add(key);
                    adapter.notifyDataSetChanged();
                }

//               declare the recyclerView here so as to make only the user id to load
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}






