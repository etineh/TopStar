package com.pixel.chatapp.view_controller.side_bar_menu.settings;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.R;
import com.pixel.chatapp.utilities.OpenActivityUtil;
import com.pixel.chatapp.constants.Ki;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.view_controller.peer2peer.P2pSetupActivity;
import com.pixel.chatapp.view_controller.signup_login.EmailOrPhoneLoginActivity;

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

        arrowBackS.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        profileClick.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, ProfileActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        privacyClick.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, PrivacyActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        soundClick.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, SoundActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        storageClick.setOnClickListener(v -> {
            Intent intent = new Intent(this, StorageActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        p2pClick.setOnClickListener(v -> {
            Intent intent = new Intent(this, P2pSetupActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        securityClick.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecurityActivity.class);
            OpenActivityUtil.openColorHighlight(v, this, intent);
        });

        terms_TV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        logOut.setOnClickListener(v -> logoutOption());

        getOnBackPressedDispatcher().addCallback(callback);

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
            startActivity(new Intent(this, EmailOrPhoneLoginActivity.class));
            finish();
            MainActivity.myUserNamePreferences.edit().remove(Ki.USERNAME).apply();    // remove username
        });
        builder.setPositiveButton("Back", (dialogInterface, i) -> dialogInterface.cancel());
        builder.create().show();
    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Intent mainActivityIntent = new Intent(SettingsActivity.this, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(mainActivityIntent);
            finish();
        }
    };


}












