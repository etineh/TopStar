package com.pixel.chatapp.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Database;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.signup_login.LoginActivity;
import com.pixel.chatapp.general.ProfileActivity;
import com.pixel.chatapp.R;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayoutGeneral;
    private ViewPager2 viewPager2General;
    private ImageView menuOpen, home, menuClose, imageViewLogo, imageViewUserPhoto;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
    ConstraintLayout scrollMenu, v;
    private TextView logout, textLightAndDay, textViewDisplayName, textViewUserName;
    Switch darkMoodSwitch;
    CardView cardViewSettings;
    SharedPreferences sharedPreferences;
    private Boolean nightMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayoutGeneral = findViewById(R.id.tabLayerMain);
        viewPager2General = findViewById(R.id.viewPageMain);
        menuOpen = findViewById(R.id.imageViewMenu);
        home = findViewById(R.id.imageViewHome);
        menuClose = findViewById(R.id.imageViewMenuClose);
        scrollMenu = findViewById(R.id.constraintMenu);
        logout = findViewById(R.id.textViewLogOut);
        imageViewLogo = findViewById(R.id.circleUserImage);
        imageViewUserPhoto = findViewById(R.id.imageViewUserPhoto);
        textViewDisplayName = findViewById(R.id.textViewDisplayName2);
        textViewUserName = findViewById(R.id.textViewUserName2);
        v = findViewById(R.id.v);
        darkMoodSwitch = findViewById(R.id.switch1);
        textLightAndDay = findViewById(R.id.textView13);

        cardViewSettings = findViewById(R.id.cardViewSettings);


        ViewPagerMainAdapter adapterV = new ViewPagerMainAdapter(getSupportFragmentManager(), getLifecycle());

        viewPager2General.setAdapter(adapterV);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayoutGeneral, viewPager2General, true, true,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position){
                            case 0:
                                tab.setText("Chats");
                                break;
                            case 1:
                                tab.setText("Tournaments");
                                break;
                            case 2:
                                tab.setText("Hosts");
                                break;
                        }
                    }
                });
        tabLayoutMediator.attach();

        // set my online presence to be true
        refUser.child(auth.getUid()).child("presence").setValue(1);

        // Dark mood setting
        sharedPreferences = this.getSharedPreferences("MOOD", Context.MODE_PRIVATE);
        nightMood = sharedPreferences.getBoolean("MoodStatus", false);

        if(nightMood){
            darkMoodSwitch.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            textLightAndDay.setText("Light");
        } else {
            textLightAndDay.setText("Dark");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        };

        darkMoodSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(nightMood){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        sharedPreferences.edit().putBoolean("MoodStatus", false).apply();
                } else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    sharedPreferences.edit().putBoolean("MoodStatus", true).apply();
                }
            }
        });

        // open the menu option
        menuOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scrollMenu.setVisibility(View.VISIBLE);
                viewPager2General.setVisibility(View.INVISIBLE);
//                v.setBackgroundColor(getResources().);
            }
        });

        // open menu option via logo too
        imageViewLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollMenu.setVisibility(View.VISIBLE);
                viewPager2General.setVisibility(View.INVISIBLE);
            }
        });

        // close the open option when background is clicked
        v.setOnClickListener(view -> {

            if (scrollMenu.getVisibility() == View.VISIBLE){
                scrollMenu.setVisibility(View.GONE);
                viewPager2General.setVisibility(View.VISIBLE);
            }
        });

        // close the open option
        menuClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewPager2General.setVisibility(View.VISIBLE);
                scrollMenu.setVisibility(View.GONE);
            }
        });

        //logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logoutOption();
            }
        });

        // settings
        cardViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });


        setUserDetails();

    }

    //  --------------- methods --------------------


        // set user image on settings
    private void setUserDetails(){
        refUser.child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String imageUrl = snapshot.child("image").getValue().toString();
                String userName = snapshot.child("userName").getValue().toString();

                if (imageUrl.equals("null")) {
                    imageViewUserPhoto.setImageResource(R.drawable.person_round);
                }
                else Picasso.get().load(imageUrl).into(imageViewUserPhoto);

                textViewDisplayName.setText(userName);      // change later to Display name
                textViewUserName.setText("@"+userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void logoutOption()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("GetMeh");
        builder.setMessage("Are you sure you want to logout?");
        builder.setCancelable(false);
        builder.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

//    @Override
//    public void onBackPressed() {
//        refUser.child(auth.getUid()).child("presence").setValue(ServerValue.TIMESTAMP);
//        super.onBackPressed();
//    }

    @Override
    protected void onPause() {
        super.onPause();
        new CountDownTimer(10000, 1000){
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                refUser.child(auth.getUid()).child("presence").setValue(ServerValue.TIMESTAMP);
            }
        }.start();

    }

    @Override
    protected void onResume() {
        new CountDownTimer(10300, 1000){
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {
                refUser.child(auth.getUid()).child("presence").setValue(1);
            }
        }.start();
        super.onResume();
    }
}










