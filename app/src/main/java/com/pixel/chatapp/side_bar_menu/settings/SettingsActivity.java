package com.pixel.chatapp.side_bar_menu.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.peer2peer.exchange.P2pExchangeActivity;
import com.pixel.chatapp.signup_login.LoginActivity;

public class SettingsActivity extends AppCompatActivity {


    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView arrowBackS = findViewById(R.id.arrowBackS);
        ConstraintLayout profileClick = findViewById(R.id.profileClick);
        ConstraintLayout privacyClick = findViewById(R.id.privacyClick);
        ConstraintLayout soundClick = findViewById(R.id.soundClick);
        ConstraintLayout storageClick = findViewById(R.id.storageClick);
        ConstraintLayout p2pClick = findViewById(R.id.p2pClick);
        ConstraintLayout securityClick = findViewById(R.id.securityClick);
        TextView terms_TV = findViewById(R.id.terms_TV);
        TextView logOut = findViewById(R.id.logOut_TV);

        DatabaseReference walletRef = FirebaseDatabase.getInstance().getReference("WalletApi");





        arrowBackS.setOnClickListener(v -> onBackPressed());

        profileClick.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
            {
                Intent intent = new Intent(this, ProfilesActivity.class);
                startActivity(intent);

                // Reset the scale
                new Handler().postDelayed(()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        privacyClick.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(() ->
            {
                walletRef.child(user.getUid()).child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        walletRef.child(user.getUid()).child("balance").setValue(150);
                        System.out.println("what is wallet " + snapshot.getValue());
                        Toast.makeText(SettingsActivity.this, "work in progress : " + snapshot.getValue(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // Reset the scale
                new Handler().postDelayed(()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();

        });

        soundClick.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        storageClick.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        p2pClick.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        securityClick.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        terms_TV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        logOut.setOnClickListener(v -> logoutOption());


    }



    //  ========    method      =======
    public void logoutOption()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("Are you sure you want to logout?");
        builder.setCancelable(false);
        builder.setNegativeButton("Logout", (dialogInterface, i) -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        builder.setPositiveButton("Back", (dialogInterface, i) -> dialogInterface.cancel());
        builder.create().show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}












