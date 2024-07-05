package com.pixel.chatapp.side_bar_menu.settings;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.activities.OTPActivity;
import com.pixel.chatapp.utils.CountryNumCodeUtils;
import com.pixel.chatapp.utils.IdTokenUtil;
import com.pixel.chatapp.utils.NumberSpacing;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.utils.UsernameTextListener;
import com.pixel.chatapp.api.Dao_interface.ApiService;
import com.pixel.chatapp.api.Dao_interface.ProfileApiDao;
import com.pixel.chatapp.api.Dao_interface.UserDao;
import com.pixel.chatapp.api.model.VerificationResponse;
import com.pixel.chatapp.api.model.RequestBody;
import com.pixel.chatapp.api.model.incoming.ResultApiM;
import com.pixel.chatapp.api.model.TwoValueM;
import com.pixel.chatapp.api.model.UserSearchM;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.ImageListener;
import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.photos.UploadProfileImage;
import com.pixel.chatapp.photos.ZoomImage;
import com.pixel.chatapp.signup_login.ForgetActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity implements OTPActivity.UpdateFieldListener, ImageListener {

    ConstraintLayout verifyLayout, pinContainerHome;
    ImageView cancelPinOption_IV;
    EditText newDetails_ET, enterPassword_ET;
    TextView saveButton, forgetPassword_TV, errorStatus_TV, infoPassword;
    TextView openCountryCode_Click, checkUsername_TV;
    ImageView arrowCountryCode;
    Spinner spinnerCountryCode;
    String countryCode;
    CircleImageView circleUserPhoto;
    TextView setDisplayName_TV, setUsernameTV, setPhoneNumberTV, setLegalNameTV, setEmailTV, previous_data_TV, setHintTV;
    ProgressBar progressBarProfile, progressBarSearch, progressBarPhoto;

    ImageView copyDisplayName, copyUsername, copyEmail, copyNumber, copyLegalName;

    FirebaseAuth auth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    String active, previousData;

    private static String imageLink;
    private String imageFirebasePath;
    private String newToken;

    DatabaseReference myDataRef, usernameRef;
    private ValueEventListener profileListener; // Declare the listener as a class variable

    List<String> countryList;

    TextWatcher textWatcher;

    AdapterView.OnItemSelectedListener addSpinnerListener;

    ActivityResultLauncher<Intent> activityResultLauncherForSelectImage;
    Permission permissions = new Permission();
//    private static String imageLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();

        ConstraintLayout kycClick = findViewById(R.id.kycClick);
        ConstraintLayout displayNameChange = findViewById(R.id.displayNameChange);
        ConstraintLayout hintChange = findViewById(R.id.hintChange);
        ConstraintLayout usernameClick = findViewById(R.id.usernameClick);
        ConstraintLayout emailClick = findViewById(R.id.emailClick);
        ConstraintLayout phoneNumberClick = findViewById(R.id.phoneNumberClick);
        ImageView backPress = findViewById(R.id.arrowBackP);
        ImageView sharePhotoIV = findViewById(R.id.sharePhotoIV);
        ImageView editPhoto = findViewById(R.id.editPhoto);
        TextView changeTitle_TV = findViewById(R.id.changeTitle_TV);

        circleUserPhoto = findViewById(R.id.circleUserPhoto);
        progressBarPhoto = findViewById(R.id.progressBarPhoto);

        verifyLayout = findViewById(R.id.profile_verifyLayout);
        cancelPinOption_IV = verifyLayout.findViewById(R.id.cancelPinOption_IV);
        newDetails_ET = verifyLayout.findViewById(R.id.newDetails_ET);
        enterPassword_ET = verifyLayout.findViewById(R.id.enterPassword_ET);
        saveButton = verifyLayout.findViewById(R.id.saveButton);
        pinContainerHome = verifyLayout.findViewById(R.id.pinContainerHome);
        forgetPassword_TV = verifyLayout.findViewById(R.id.forgetPassword_TV);
        errorStatus_TV = verifyLayout.findViewById(R.id.errorStatus_TV);
        openCountryCode_Click = verifyLayout.findViewById(R.id.openCountryCode_Click);
        arrowCountryCode = verifyLayout.findViewById(R.id.arrowCountryCode);
        spinnerCountryCode = verifyLayout.findViewById(R.id.spinnerCountryCode);
        infoPassword = verifyLayout.findViewById(R.id.infoPassword);
        checkUsername_TV = verifyLayout.findViewById(R.id.checkUsername_TV);
        progressBarSearch = verifyLayout.findViewById(R.id.progressBarSearch);
        previous_data_TV = verifyLayout.findViewById(R.id.previous_data_TV);

        setHintTV = findViewById(R.id.setHint_TV);
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
        usernameRef = FirebaseDatabase.getInstance().getReference("usernames");

        getProfileSharePref();
        new Handler().postDelayed(this::getUserInfo, 2000);

        countryList = new ArrayList<>();

        countryCode = CountryNumCodeUtils.getUserCountry(this);
        openCountryCode_Click.setText(countryCode);

        CountryNumCodeUtils.getCountryCode(countryCodes -> countryList = countryCodes);
        spinnerListener();

        getOnBackPressedDispatcher().addCallback(this, callback);

        OTPActivity.updateFieldListener = this;

        // ======== photo update    =========

        registerActivityForSelectImage();   // register the launcher

        UploadProfileImage.imageListener = this;

        sharePhotoIV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        editPhoto.setOnClickListener(v -> {
            imageChooser();
//            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        circleUserPhoto.setOnClickListener(v -> {
            Intent i = new Intent(this, ZoomImage.class);
            i.putExtra("otherName", "My Profile Photo");
            i.putExtra("imageLink", imageLink);
            i.putExtra("from", "profilePix");
            startActivity(i);
        });

        kycClick.setOnClickListener(v -> {
            startActivity(new Intent(this, KycActivity.class));
            Toast.makeText(this, "verifying...", Toast.LENGTH_SHORT).show();

        });

        forgetPassword_TV.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();

        });

        hintChange.setOnClickListener(v -> {
            hidePasswordContainer();
            active = "hint";
            changeTitle_TV.setText(getString(R.string.changeHint));
            previous_data_TV.setText(getString(R.string.hintInfo));

            newDetails_ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            newDetails_ET.setMaxLines(5);
            newDetails_ET.setText(setHintTV.getText().toString());

            newDetails_ET.setSelection(newDetails_ET.length());

        });

        displayNameChange.setOnClickListener(v -> {
            hidePasswordContainer();
            active = "displayName";
            changeTitle_TV.setText(getString(R.string.changeDisplayName));
            newDetails_ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            newDetails_ET.setMaxLines(2);
            newDetails_ET.setHint(R.string.newDisplayName);
            previousData = setDisplayName_TV.getText().toString();
            previous_data_TV.setText(previousData);

        });

        usernameClick.setOnClickListener(v -> {

            showPasswordContainer();
            changeTitle_TV.setText(getString(R.string.changeUserName));
            newDetails_ET.setHint(R.string.newUsername);
            newDetails_ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            newDetails_ET.setMaxLines(2);
            active = "username";
            previousData = setUsernameTV.getText().toString();
            previous_data_TV.setText("@" + previousData);

            textWatcher = UsernameTextListener.userNameListener(newDetails_ET, this, checkUsername_TV, progressBarSearch);
            newDetails_ET.addTextChangedListener(textWatcher);

            // Set drawable start
            Drawable drawable = getResources().getDrawable(R.drawable.email); // Replace R.drawable.your_drawable with your drawable resource
            newDetails_ET.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);

        });

        emailClick.setOnClickListener(v -> {
            generateIdToken();
            showPasswordContainer();
            changeTitle_TV.setText(getString(R.string.changeEmail));
            newDetails_ET.setHint(R.string.newEmail);
            newDetails_ET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            active = "email";
            previousData = setEmailTV.getText().toString();
            previous_data_TV.setText(previousData);
        });

        phoneNumberClick.setOnClickListener(v -> {

            showPasswordContainer();
            changeTitle_TV.setText(getString(R.string.changePhoneNumber));
            newDetails_ET.setHint(R.string.number);
            newDetails_ET.setInputType(InputType.TYPE_CLASS_PHONE);
            active = "phoneNumber";
            previousData = setPhoneNumberTV.getText().toString();

            String spaceNumber = NumberSpacing.formatPhoneNumber(previousData, 3, 3);
            previous_data_TV.setText(spaceNumber);

            arrowCountryCode.setVisibility(View.VISIBLE);
            openCountryCode_Click.setVisibility(View.VISIBLE);

            if(setSpinnerCountryCOdeAdapter() < 1){
                setSpinnerCountryCOdeAdapter();
            }

        });

        openCountryCode_Click.setOnClickListener(v -> {
            v.setBackgroundColor(ContextCompat.getColor(this, R.color.cool_orange));
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(10).withEndAction(()->{

                if(countryList.size() == 0){
                    Toast.makeText(this,  getString(R.string.clickAgain), Toast.LENGTH_SHORT).show();
                } else {
                    if(setSpinnerCountryCOdeAdapter() < 1){
                        setSpinnerCountryCOdeAdapter();
                    }
                    spinnerCountryCode.performClick();
                    spinnerCountryCode.setOnItemSelectedListener(addSpinnerListener);
                }

                v.setBackgroundColor(0);

            });
        });

        saveButton.setOnClickListener(v -> {
            if(active != null){

                if(active.equals("displayName") || active.equals("hint"))
                {
                    if(newDetails_ET.length() > 4){
                        progressBarProfile.setVisibility(View.VISIBLE);

                        String getDisplayName = newDetails_ET.getText().toString();

                        myDataRef.child(user.getUid()).child("general")
                                .child(active).setValue(getDisplayName).addOnCompleteListener(task ->
                                {
                                    if(task.isSuccessful())
                                    {
                                        if(active.equals("displayName"))
                                            setDisplayName_TV.setText(getDisplayName);
                                        else
                                            setHintTV.setText(getDisplayName);
                                        onBackPressed();
                                        Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();

                                    } else Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();

                                    progressBarProfile.setVisibility(View.GONE);
                                });

                    } else {
                        Toast.makeText(this, getString(R.string.fieldInputError), Toast.LENGTH_SHORT).show();
                    }

                } else if (active.equals("username"))
                {
                    if(newDetails_ET.getError() == null)
                    {
                        if(newDetails_ET.length() > 3){
                            if(enterPassword_ET.length() >= 6){
                                progressBarProfile.setVisibility(View.VISIBLE);

                                String getUsername = newDetails_ET.getText().toString();
                                if(getUsername.startsWith("@")){
                                    getUsername = newDetails_ET.getText().toString().substring(1);
                                }

                                verifyUsername(getUsername);

                            } else Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();

                        } else Toast.makeText(this, getString(R.string.userNameError), Toast.LENGTH_SHORT).show();

                    } else Toast.makeText(this, getString(R.string.invalidUsername), Toast.LENGTH_SHORT).show();

                } else if (active.equals("email"))
                {
                    String email = newDetails_ET.getText().toString();
                    if (email.contains("@") && email.contains(".")) {

                        authenticateAndProceed(email);   // for email

                    } else {
                        Toast.makeText(this, getString(R.string.emailError), Toast.LENGTH_SHORT).show();
                    }

                } else if (active.equals("phoneNumber"))
                {
                    String number = newDetails_ET.getText().toString().replaceAll("\\s+", "");

                    if(newDetails_ET.length() > 7)
                    {

                        String finalCodeNumber = countryCode + number;

                        if(number.startsWith("0")){
                            finalCodeNumber = countryCode + number.substring(1);
                        } else if (number.startsWith(countryCode)) {
                            finalCodeNumber = number;
                        }

                        if(user.getPhoneNumber() == null || (user.getPhoneNumber() != null && !user.getPhoneNumber().equals(number) ) )
                        {
//                            Toast.makeText(this, ""+finalCodeNumber, Toast.LENGTH_SHORT).show();
                            authenticateAndProceed(finalCodeNumber);    // for phone number
                        }
                        else Toast.makeText(this, getString(R.string.phoneNumberExist), Toast.LENGTH_SHORT).show();

                    } else Toast.makeText(this, getString(R.string.phoneError), Toast.LENGTH_SHORT).show();

                }

            } else onBackPressed();

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

        forgetPassword_TV.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgetActivity.class));
        });

        backPress.setOnClickListener(v ->  getOnBackPressedDispatcher().onBackPressed() );
        cancelPinOption_IV.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

//        getResponse();

        SharedPreferences sharedPreferences = getSharedPreferences("KYC", Context.MODE_PRIVATE);

//        String data = sharedPreferences.getString("data", "");

    }



    //  ========     methods

    private void getResponse(){

        String baseUrl = "https://api.shuftipro.com/";
        String clientId = "82eeb940d3c995e3582d80721ecd042bced8fb54741bd21bab611fc5d565217e";
        String secretKey = "iR7XCWIqQfQ7PtNyA1xqRR7rbpwXkRgZ";
        String reference = "Unique-Reference7";

        // Create OkHttpClient with basic authentication
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            String credentials = clientId + ":" + secretKey;
            String basicAuth = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
            }
            okhttp3.Request original = chain.request();
            okhttp3.Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", basicAuth)
                    .method(original.method(), original.body());
            okhttp3.Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        // Build Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create ApiService instance
        ApiService apiService = retrofit.create(ApiService.class);

        // Create request body
        RequestBody requestBody = new RequestBody(reference);
        String authorization = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            authorization = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + secretKey).getBytes());
        }

        // Make API call
        Call<VerificationResponse> call = apiService.getStatus(authorization, requestBody);
        call.enqueue(new Callback<VerificationResponse>() {
            @Override
            public void onResponse(Call<VerificationResponse> call, Response<VerificationResponse> response) {
                if (response.isSuccessful()) {

                } else {
                    Toast.makeText(ProfileActivity.this, "error here", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerificationResponse> call, Throwable t) {
                System.out.println("what is error : " + t.getMessage());
            }
        });
    }


    private void registerActivityForSelectImage() {

        activityResultLauncherForSelectImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RESULT_OK && data != null){

                        Intent intent = new Intent(this, UploadProfileImage.class);
                        intent.putExtra(AllConstants.PICKED_IMAGE_URI_PATH, data.getData().toString());
                        startActivity(intent);

                    }
                }
        );
    }

    public void updatePhoto(){

        progressBarPhoto.setVisibility(View.VISIBLE);

        UUID randomID = UUID.randomUUID();
        String imagePath = "images/"+ auth.getUid() + "/" +randomID+".jpg";

        if(imageFirebasePath != null) imagePath = imageFirebasePath;

        String finalPath = imagePath;
        storageRef.child(finalPath).putFile(Uri.parse(imageLink))

                .addOnSuccessListener(taskSnapshot -> {
                    //  get the uri where the image was saved in database
                    StorageReference myStorageRef = FirebaseStorage.getInstance().getReference(finalPath);

                    myStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get the image uri link and save to user DB
                        String imageLink_ = uri.toString();

                        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");

                        refUsers.child(auth.getUid()).child("general")
                                .child("image").setValue(imageLink_);

                        if(imageFirebasePath == null) {
                            refUsers.child(auth.getUid()).child("general")
                                    .child("imageUUID").setValue(finalPath);
                        }

                        new Handler().postDelayed(() -> progressBarPhoto.setVisibility(View.GONE),1000);
                        Toast.makeText(this, getString(R.string.photoSaved), Toast.LENGTH_SHORT).show();
                    });

                }).addOnFailureListener(e -> {
                    progressBarPhoto.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                });


    }

    private void verifyUsername(String getUsername) {
        usernameRef.child(getUsername.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Toast.makeText(ProfileActivity.this, getString(R.string.usernameExist), Toast.LENGTH_SHORT).show();

                } else {
                    auth.signInWithEmailAndPassword(Objects.requireNonNull(auth.getCurrentUser().getEmail()),
                                    enterPassword_ET.getText().toString())
                            .addOnCompleteListener(task ->
                            {
                                if(task.isSuccessful()) usernameApi(getUsername);
                            })
                            .addOnFailureListener(e -> {
                                if(e.getMessage().startsWith("A network error")){
                                    Toast.makeText(ProfileActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                                } else if(e.getMessage().startsWith("We have blocked")) {
                                    Toast.makeText(ProfileActivity.this, getString(R.string.accountDisable), Toast.LENGTH_SHORT).show();
                                    errorStatus_TV.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(ProfileActivity.this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();
                                }
                                progressBarProfile.setVisibility(View.GONE);
//                                                        System.out.println("what is " + e.getMessage());
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarProfile.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void usernameApi(String getUsername) {

        IdTokenUtil.generateToken(token ->
        {
            ProfileApiDao apiDao = AllConstants.retrofit.create(ProfileApiDao.class);

            TwoValueM valueM = new TwoValueM(token, getUsername);

            apiDao.username(valueM).enqueue(new Callback<ResultApiM>() {
                @Override
                public void onResponse(Call<ResultApiM> call, Response<ResultApiM> response) {

                    if(response.isSuccessful()){
                        String result = response.body().getResult();
                        
                        if(result.equals("success")){
                            setDisplayName_TV.setText(getUsername);
                            onBackPressed();
                            Toast.makeText(ProfileActivity.this, getString(R.string.saved), Toast.LENGTH_SHORT).show();

                        } else if (result.equals("username exist")) {
                            Toast.makeText(ProfileActivity.this, getString(R.string.usernameExist), Toast.LENGTH_SHORT).show();

                        } else{
                            Toast.makeText(ProfileActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                        }
                        progressBarProfile.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onFailure(Call<ResultApiM> call, Throwable throwable) {
                    progressBarProfile.setVisibility(View.GONE);
                    if(throwable.getMessage().contains("Failed to connect"))
                    {  // server error
                        Toast.makeText(ProfileActivity.this, getString(R.string.serverError), Toast.LENGTH_SHORT).show();

                    } else{     // no internet connection | timeout
                        Toast.makeText(ProfileActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                    }

                    System.out.println("what is err CreateAcc: L220 " + throwable.getMessage());

                }
            });

        });
    }

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

    private int setSpinnerCountryCOdeAdapter(){

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapter);

        return adapter.getCount();
    }

    private void spinnerListener(){
        // Set a listener for spinner item selection
        addSpinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                // Get the selected item
                String selectedItem = parentView.getItemAtPosition(position).toString();

                // Extract only the country code and phone number prefix
                int startIndex = selectedItem.indexOf("(");
                if (startIndex != -1) {
                    String countryCodeAndPrefix = selectedItem.substring(startIndex).trim();

                    openCountryCode_Click.setText(countryCodeAndPrefix);

                    String[] splitCode = countryCodeAndPrefix.split(" ");
                    countryCode = splitCode[1];

                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }

        };

    }

    private void getProfileSharePref(){

        // update share_preference
        String username = MainActivity.myProfileShareRef.getString(AllConstants.PROFILE_USERNAME, "---");
        String displayName = MainActivity.myProfileShareRef.getString(AllConstants.PROFILE_DISNAME, "---");
        String hint = MainActivity.myProfileShareRef.getString(AllConstants.PROFILE_HINT, getString(R.string.hint2));
//        String email = MainActivity.myProfileShareRef.getString(AllConstants.PROFILE_EMAIL, "---");

        setDisplayName_TV.setText(displayName);
        setUsernameTV.setText(username);       // display the database userName to the input field
        setEmailTV.setText(user.getEmail());
        setPhoneNumberTV.setText(user.getPhoneNumber());
        setHintTV.setText(hint);
    }

    private void getUserInfo()
    {
        // get the saved details from firebase database
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {      // snapshot get access to the user details saved

                if(snapshot.child("userName").exists()){

                    String hint = snapshot.child("hint").exists() ?
                            snapshot.child("hint").getValue().toString() : getString(R.string.hint2);

                    String username = snapshot.child("userName").exists() ?
                            snapshot.child("userName").getValue().toString() : "---";

                    imageLink = snapshot.child("image").exists() && !snapshot.child("image").getValue().toString().equals("null")
                            ? snapshot.child("image").getValue().toString() : null;

                    // get the image path that leads to the fireStore i.e images/448f87cd-8264-46c4-b106-2071339bb302.jpg
                    imageFirebasePath = snapshot.child("imageUUID").exists() ?
                            snapshot.child("imageUUID").getValue().toString() : null;

                    String displayName = snapshot.child("displayName").exists() ?
                            snapshot.child("displayName").getValue().toString() : "---";

                    setDisplayName_TV.setText(displayName);
                    setUsernameTV.setText(username);       // display the database userName to the input field
                    setEmailTV.setText(user.getEmail());
                    setPhoneNumberTV.setText(user.getPhoneNumber());
                    setHintTV.setText(hint);

                    if(imageLink != null) Picasso.get().load(imageLink).into(circleUserPhoto);

                    // update share_preference
                    MainActivity.myProfileShareRef.edit().putString(AllConstants.PROFILE_USERNAME, username).apply();
                    MainActivity.myProfileShareRef.edit().putString(AllConstants.PROFILE_DISNAME, displayName).apply();
                    MainActivity.myProfileShareRef.edit().putString(AllConstants.PROFILE_HINT, hint).apply();
//                    MainActivity.myProfileShareRef.edit().putString(AllConstants.PROFILE_EMAIL, user.getEmail()).apply();

                } else {
                    onBackPressed();
                    Toast.makeText(ProfileActivity.this, getString(R.string.userNotFound), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        myDataRef.child(user.getUid()).child("general").addValueEventListener(profileListener);

    }

    private void checkNumberExist(String number){
        UserDao userDao = AllConstants.retrofit.create(UserDao.class);

        userDao.fineUser(number).enqueue(new Callback<UserSearchM>() {
            @Override
            public void onResponse(Call<UserSearchM> call, Response<UserSearchM> response) {
                
                if(response.isSuccessful()){
                    Toast.makeText(ProfileActivity.this, getString(R.string.numberExist), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String error = response.errorBody().string();

                        JsonObject jsonObject = new Gson().fromJson(error, JsonObject.class);
                        String message = jsonObject.get("message").getAsString();
                        
                        if(message.equals("user not found")){

                            Intent intent = new Intent(ProfileActivity.this, OTPActivity.class);
                            intent.putExtra("type", active);
                            intent.putExtra("token", newToken);
                            intent.putExtra("previousData", previousData);
                            intent.putExtra("newData", number);
                            intent.putExtra("pass", enterPassword_ET.getText().toString());

                            startActivity(intent);
                            onBackPressed();

                        } else {
                            progressBarProfile.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        progressBarProfile.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                        throw new RuntimeException(e);
                    }
                }
                
            }

            @Override
            public void onFailure(Call<UserSearchM> call, Throwable throwable) {
                if(throwable.getMessage().contains("failed to connect")) {  // server error
                    Toast.makeText(ProfileActivity.this, getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                } else{     // no internet connection | timeout
                    Toast.makeText(ProfileActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                }
                progressBarProfile.setVisibility(View.GONE);

//                System.out.println("what is err ProfileAct: L550 " + throwable.getMessage());
            }
        });
        
    }
    
    private void authenticateAndProceed(String getInput){

        if(enterPassword_ET.length() > 3)
        {
            progressBarProfile.setVisibility(View.VISIBLE);
            PhoneUtils.hideKeyboard(this, enterPassword_ET);

            if(!auth.getCurrentUser().getEmail().isEmpty())
            {
                auth.signInWithEmailAndPassword(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()),
                                enterPassword_ET.getText().toString())
                        .addOnCompleteListener(task -> {

                            if(task.isSuccessful()){

                                if(active.equals("email")){

                                    Intent intent = new Intent(this, OTPActivity.class);
                                    intent.putExtra("type", active);
                                    intent.putExtra("token", newToken);
                                    intent.putExtra("previousData", previousData);
                                    intent.putExtra("newData", getInput);
                                    intent.putExtra("pass", enterPassword_ET.getText().toString());

                                    startActivity(intent);
                                    onBackPressed();

                                } else {
                                    checkNumberExist(getInput);
                                }
                            }
                            progressBarProfile.setVisibility(View.GONE);

                        })
                        .addOnFailureListener(e -> {
                            if(e.getMessage().startsWith("A network error"))
                            {
                                Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                            } else if(e.getMessage().startsWith("We have blocked"))
                            {
                                Toast.makeText(this, getString(R.string.accountDisable), Toast.LENGTH_SHORT).show();
                                errorStatus_TV.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();
                            }
                            progressBarProfile.setVisibility(View.GONE);
                            System.out.println("what is " + e.getMessage());
                        });

            } else Toast.makeText(this, getString(R.string.emailError), Toast.LENGTH_SHORT).show();

        } else Toast.makeText(this, getString(R.string.invalidPassword), Toast.LENGTH_SHORT).show();

    }

    private void generateIdToken(){
        // Generate a new authentication token (e.g., ID token) using Firebase Authentication
        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getIdToken(true)
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
        forgetPassword_TV.setVisibility(View.GONE);
        infoPassword.setVisibility(View.GONE);
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


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {

            if(verifyLayout.getVisibility() == View.VISIBLE){
                verifyLayout.setVisibility(View.GONE);
                newDetails_ET.clearFocus();
                newDetails_ET.setText(null);
                enterPassword_ET.clearFocus();
                enterPassword_ET.setText(null);
                arrowCountryCode.setVisibility(View.GONE);
                infoPassword.setVisibility(View.GONE);
                openCountryCode_Click.setVisibility(View.GONE);
                checkUsername_TV.setVisibility(View.GONE);
                progressBarSearch.setVisibility(View.GONE);
                previousData = null;
                previous_data_TV.setText(null);

                newDetails_ET.setError(null);
                PhoneUtils.hideKeyboard(ProfileActivity.this, newDetails_ET);

                // Remove drawable start
                newDetails_ET.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

                spinnerCountryCode.setOnItemSelectedListener(null);

                if(textWatcher != null) newDetails_ET.removeTextChangedListener(textWatcher);

            } else {
                if(profileListener != null){
                    myDataRef.child(user.getUid()).child("general").removeEventListener(profileListener);
                }
                // The next 3 lines replace: super.onBackPressed();
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
                finish();
            }

        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == AllConstants.STORAGE_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncherForSelectImage.launch(intent);

        } else {
            Toast.makeText(this, "Go to phone settings and give WinnerChat permission to access Gallery " + grantResults[0], Toast.LENGTH_SHORT).show();
        }
    }

    //--------------    another method to get permission from user phone
    public void imageChooser()
    {
        if(!permissions.isStorageOk(this)){
            permissions.requestStorage(this);
        } else {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncherForSelectImage.launch(intent);
        }

    }


    //  === interface ===========
    @Override
    public void updateEmail(String value) {
        setEmailTV.setText(value);
    }

    @Override
    public void updatePhoneNumber(String value) {
        setPhoneNumberTV.setText(value);
    }

    // for image

    @Override
    public void sendImageData(Uri imageUriPath) {
        Picasso.get().load(imageUriPath).into(circleUserPhoto);
        imageLink = imageUriPath.toString();

        updatePhoto();
    }


    @Override
    public void getCurrentModelChat(MessageModel messageModel, int position) {

    }

    @Override
    public void onColorChange(int color) {

    }
}















