package com.pixel.chatapp.side_bar_menu.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.pixel.chatapp.activities.OTPActivity;
import com.pixel.chatapp.activities.OTPGenerator;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilesActivity extends AppCompatActivity implements OTPActivity.UpdateFieldListener {

    ConstraintLayout verifyLayout, pinContainerHome;
    ImageView cancelPinOption_IV;
    EditText newDetails_ET, enterPassword_ET;
    TextView saveButton, infoPassword, forgetPassword_TV, errorStatus_TV;
    CircleImageView circleUserPhoto;
    TextView setDisplayName_TV, setUsernameTV, setPhoneNumberTV, setLegalNameTV, setEmailTV;
//    @SuppressLint("StaticFieldLeak")
//    private static TextView setEmailTV;
    ProgressBar progressBarProfile;

    ImageView copyDisplayName, copyUsername, copyEmail, copyNumber, copyLegalName;

    FirebaseAuth auth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String active;

    private static String imageLink;
    private String imageFirebasePath;
    private String newToken;

    DatabaseReference myDataRef;
    private ValueEventListener profileListener; // Declare the listener as a class variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        auth = FirebaseAuth.getInstance();

        ConstraintLayout kycClick = findViewById(R.id.kycClick);
        ConstraintLayout displayNameChange = findViewById(R.id.displayNameChange);
        ConstraintLayout usernameClick = findViewById(R.id.usernameClick);
        ConstraintLayout emailClick = findViewById(R.id.emailClick);
        ConstraintLayout phoneNumberClick = findViewById(R.id.phoneNumberClick);
        ImageView backPress = findViewById(R.id.arrowBackP);
        ImageView sharePhotoIV = findViewById(R.id.sharePhotoIV);
        ImageView editPhoto = findViewById(R.id.editPhoto);

        circleUserPhoto = findViewById(R.id.circleUserPhoto);

        verifyLayout = findViewById(R.id.profile_verifyLayout);
        cancelPinOption_IV = verifyLayout.findViewById(R.id.cancelPinOption_IV);
        newDetails_ET = verifyLayout.findViewById(R.id.newDetails_ET);
        enterPassword_ET = verifyLayout.findViewById(R.id.enterPassword_ET);
        saveButton = verifyLayout.findViewById(R.id.saveButton);
        pinContainerHome = verifyLayout.findViewById(R.id.pinContainerHome);
        infoPassword = verifyLayout.findViewById(R.id.infoPassword);
        forgetPassword_TV = verifyLayout.findViewById(R.id.forgetPassword_TV);
        errorStatus_TV = verifyLayout.findViewById(R.id.errorStatus_TV);

        setDisplayName_TV = findViewById(R.id.setDisplayName_TV);
        setUsernameTV = findViewById(R.id.setUsernameTV);
        setEmailTV = findViewById(R.id.setEmailTV);
        setPhoneNumberTV = findViewById(R.id.setPhoneNumberTV);
        setLegalNameTV = findViewById(R.id.setLegalNameTV);
        progressBarProfile = findViewById(R.id.progressBarProfile);

        copyDisplayName = findViewById(R.id.copyDisplayName);
        copyUsername = findViewById(R.id.copyUsername);
        copyEmail = findViewById(R.id.copyEmail);
        copyNumber = findViewById(R.id.copyNumber);
        copyLegalName = findViewById(R.id.copyLegalName);

        myDataRef = FirebaseDatabase.getInstance().getReference("Users");

        OTPActivity.updateFieldListener = this;

        sharePhotoIV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        editPhoto.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        kycClick.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        forgetPassword_TV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        displayNameChange.setOnClickListener(v -> {
            hidePasswordContainer();
            active = "displayName";
            newDetails_ET.setInputType(InputType.TYPE_CLASS_TEXT);
            newDetails_ET.setHint(R.string.newDisplayName);
        });

        usernameClick.setOnClickListener(v -> {

            showPasswordContainer();
            newDetails_ET.setHint(R.string.newUsername);
            newDetails_ET.setInputType(InputType.TYPE_CLASS_TEXT);
            active = "username";
        });

        emailClick.setOnClickListener(v -> {
            generateIdToken();
            showPasswordContainer();
            newDetails_ET.setHint(R.string.newEmail);
            newDetails_ET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            active = "email";
        });

        phoneNumberClick.setOnClickListener(v -> {
            showPasswordContainer();
            newDetails_ET.setHint(R.string.newPhoneNumber);
            newDetails_ET.setInputType(InputType.TYPE_CLASS_PHONE);
            active = "phoneNumber";
        });

        saveButton.setOnClickListener(v -> {
            if(active != null){

                if(active.equals("displayName"))
                {
                    if(newDetails_ET.length() > 4){

                        progressBarProfile.setVisibility(View.VISIBLE);
                        String getDisplayName = newDetails_ET.getText().toString();
                        myDataRef.child(user.getUid()).child("displayName").setValue(getDisplayName).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                setDisplayName_TV.setText(getDisplayName);
                                onBackPressed();
                                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                            }
                            progressBarProfile.setVisibility(View.GONE);
                        });

                    } else {
                        Toast.makeText(this, getString(R.string.displayNameError), Toast.LENGTH_SHORT).show();
                    }

                } else if (active.equals("username"))
                {
                    if(newDetails_ET.length() > 3){
                        if(enterPassword_ET.length() > 3){
                            progressBarProfile.setVisibility(View.VISIBLE);

                            auth.signInWithEmailAndPassword(Objects.requireNonNull(auth.getCurrentUser().getEmail()),
                                            enterPassword_ET.getText().toString())
                                    .addOnCompleteListener(task -> {

                                        if(task.isSuccessful()){

                                            String getUsername = newDetails_ET.getText().toString();
                                            // save to database
                                            myDataRef.child(user.getUid()).child("userName").setValue(getUsername).addOnCompleteListener(task__ -> {
                                                if(task__.isSuccessful()){
                                                    setUsernameTV.setText(getUsername);
                                                    Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
                                                    onBackPressed();
                                                } else {
                                                    Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                                                }
                                                progressBarProfile.setVisibility(View.GONE);
                                            });

                                        }

                                    }).addOnFailureListener(e -> {
                                        if(e.getMessage().startsWith("A network error")){
                                            Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                                        } else if(e.getMessage().startsWith("We have blocked")) {
                                            Toast.makeText(this, getString(R.string.accountDisable), Toast.LENGTH_SHORT).show();
                                            errorStatus_TV.setVisibility(View.VISIBLE);
                                        } else {
                                            Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();
                                        }
                                        progressBarProfile.setVisibility(View.GONE);
                                        System.out.println("what is " + e.getMessage());
                                    });

                        } else {
                            Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, getString(R.string.userNameError), Toast.LENGTH_SHORT).show();
                    }

                } else if (active.equals("email")) {
                    String email = newDetails_ET.getText().toString();
                    if (email.contains("@") && email.contains(".")) {

                        authenticateAndProceed(email);   // for email

                    } else {
                        Toast.makeText(this, getString(R.string.emailError), Toast.LENGTH_SHORT).show();
                    }

                } else if (active.equals("phoneNumber"))
                {
                    String number = newDetails_ET.getText().toString();
                    if(newDetails_ET.length() > 9){

                        authenticateAndProceed(number);   // for phone number

                    } else {
                        Toast.makeText(this, getString(R.string.phoneError), Toast.LENGTH_SHORT).show();
                    }
                }

            } else {

                onBackPressed();
            }

        });

        // ========= copy details   ==============
        copyDisplayName.setOnClickListener(v -> {
            copy(v, setDisplayName_TV);
        });

        copyUsername.setOnClickListener(v -> {
            copy(v, setUsernameTV);
        });

        copyEmail.setOnClickListener(v -> {
            copy(v, setEmailTV);
        });

        copyNumber.setOnClickListener(v -> {
            copy(v, setPhoneNumberTV);
        });

        copyLegalName.setOnClickListener(v -> {
            copy(v, setLegalNameTV);
        });

        backPress.setOnClickListener(v -> onBackPressed());
        cancelPinOption_IV.setOnClickListener(v -> onBackPressed());

        getUserInfo();

//        System.out.println("what is email: " + user.getEmail());

    }


    //  ========     methods

    private void copy(View v, TextView textView){
        v.animate().scaleY(1.2f).scaleX(1.2f).setDuration(10).withEndAction(() ->
                {
                    PhoneUtils.copyText(this, textView);
                    Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();

                    // Reset the scale
                    new Handler().postDelayed(()-> {
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                    }, 200);
                }
        ).start();
    }

//    private void

    private void getUserInfo()
    {
        // get the saved details from firebase database
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {      // snapshot get access to the user details saved

                String displayName = "---";

                String name = snapshot.child("userName").getValue().toString();
                // remember we convert the img to uri link. So the url link will fetch the image from the firebase storage
                imageLink = snapshot.child("image").getValue().toString();
                // get the image path that leads to the fireStore i.e images/448f87cd-8264-46c4-b106-2071339bb302.jpg
                imageFirebasePath = snapshot.child("imageUUID").getValue().toString();

                if(snapshot.child("displayName").exists()){
                    displayName = snapshot.child("displayName").getValue().toString();
                }
                setDisplayName_TV.setText(displayName);
                setUsernameTV.setText(name);       // display the database userName to the input field
                setEmailTV.setText(user.getEmail());

                if(imageLink.equals("null") ){
                    circleUserPhoto.setImageResource(R.drawable.person);
                } else{
                    Picasso.get().load(imageLink).into(circleUserPhoto);    // get the image into the imageView xml id
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        myDataRef.child(user.getUid()).addValueEventListener(profileListener);
    }

    private void authenticateAndProceed(String getInput){

        if(enterPassword_ET.length() > 3) {    // verify password later


            progressBarProfile.setVisibility(View.VISIBLE);

            auth.signInWithEmailAndPassword(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()),
                            enterPassword_ET.getText().toString())
                    .addOnCompleteListener(task -> {

                        if(task.isSuccessful()){
                            // save to database later
                            Intent intent = new Intent(this, OTPActivity.class);
                            intent.putExtra("type", active);
                            intent.putExtra("token", newToken);
                            intent.putExtra("value", getInput);
                            intent.putExtra("pass", enterPassword_ET.getText().toString());

                            startActivity(intent);
                            onBackPressed();

                            progressBarProfile.setVisibility(View.GONE);
                        }

                    }).addOnFailureListener(e -> {
                        if(e.getMessage().startsWith("A network error")){
                            Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                        } else if(e.getMessage().startsWith("We have blocked")) {
                            Toast.makeText(this, getString(R.string.accountDisable), Toast.LENGTH_SHORT).show();
                            errorStatus_TV.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();
                        }
                        progressBarProfile.setVisibility(View.GONE);
                        System.out.println("what is " + e.getMessage());
                    });

        } else Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();

    }

    private void generateIdToken(){
        // Generate a new authentication token (e.g., ID token) using Firebase Authentication
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    // New authentication token obtained successfully
                    newToken = result.getToken();
                    System.out.println("what is token : " + newToken);

                })
                .addOnFailureListener(e -> {
                    // Handle token generation failure
                    System.out.println("what is token : fail to gen");

                });
    }

    private void hidePasswordContainer(){

        verifyLayout.setVisibility(View.VISIBLE);

        errorStatus_TV.setVisibility(View.GONE);
        pinContainerHome.setVisibility(View.GONE);
        infoPassword.setVisibility(View.GONE);
        forgetPassword_TV.setVisibility(View.GONE);

        newDetails_ET.requestFocus();
    }

    private void showPasswordContainer(){
        verifyLayout.setVisibility(View.VISIBLE);

        errorStatus_TV.setVisibility(View.GONE);
        pinContainerHome.setVisibility(View.VISIBLE);
        infoPassword.setVisibility(View.VISIBLE);
        forgetPassword_TV.setVisibility(View.VISIBLE);
        newDetails_ET.requestFocus();

    }

    @Override
    public void onBackPressed() {

        if(verifyLayout.getVisibility() == View.VISIBLE){
            verifyLayout.setVisibility(View.GONE);
            newDetails_ET.clearFocus();
            newDetails_ET.setText(null);
            enterPassword_ET.clearFocus();
            enterPassword_ET.setText(null);
            PhoneUtils.hideKeyboard(this, newDetails_ET);
        } else {
//            super.onBackPressed();
            myDataRef.child(user.getUid()).removeEventListener(profileListener);
            finish();
        }
    }


    //  === interface ===========
    @Override
    public void updateEmail(String value) {
        setEmailTV.setText(value);
    }


}















