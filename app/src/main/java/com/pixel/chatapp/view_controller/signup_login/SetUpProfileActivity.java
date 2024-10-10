package com.pixel.chatapp.view_controller.signup_login;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pixel.chatapp.permission.AppPermission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.K;
import com.pixel.chatapp.utilities.IdTokenUtil;
import com.pixel.chatapp.utilities.UsernameTextListener;
import com.pixel.chatapp.services.api.dao_interface.ProfileApiDao;
import com.pixel.chatapp.services.api.model.SetUpM;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.interface_listeners.ImageListener;
import com.pixel.chatapp.dataModel.MessageModel;
import com.pixel.chatapp.view_controller.photos_video.UploadProfileImage;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetUpProfileActivity extends AppCompatActivity implements ImageListener {

    private CircleImageView imageViewCircle;
    private TextInputEditText editTextEmailRegister, editTextPasswordRegister, editTextUsername, displayNames_ET;
    private Button setUpButton;
    ProgressBar progressBarSetUp, progressBarSearch;
    TextView textViewImage, checkUsername_TV;
    private static String imageLink;
    private Boolean imageCheck = false;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference refUsers, usernameRef;

    // storage for image
    FirebaseStorage storage;
    StorageReference storageReference;

    int close = 0;
    TextWatcher textWatcher;

    AppPermission permissions = new AppPermission();
    ActivityResultLauncher<Intent> activityResultLauncherForSelectImage;

    SharedPreferences resetLoginSharePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profile);

        imageViewCircle = findViewById(R.id.imageViewCircle);
        editTextEmailRegister = findViewById(R.id.editTextEmailSignup);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordSignup);
        editTextUsername = findViewById(R.id.editTextUserNameSignup);
        textViewImage = findViewById(R.id.textViewImage);
        setUpButton = findViewById(R.id.buttonRegister);
        progressBarSetUp = findViewById(R.id.progressBarSetUp);
        progressBarSearch = findViewById(R.id.progressBarSearch);
        checkUsername_TV = findViewById(R.id.checkUsername_TV);
        displayNames_ET = findViewById(R.id.displayNames_ET);

        resetLoginSharePref = this.getSharedPreferences(K.RESET_LOGIN, Context.MODE_PRIVATE);
        

        // --------------------- activating Firebase and database
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        refUsers = database.getReference("Users");
        usernameRef = FirebaseDatabase.getInstance().getReference("usernames");

        // --------------------- activating Firebase for image storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // register photo launcher
        registerActivityForSelectImage();

        textWatcher = UsernameTextListener.userNameListener(editTextUsername, this, checkUsername_TV, progressBarSearch);  // username check listener
        editTextUsername.addTextChangedListener(textWatcher);

        imageViewCircle.setOnClickListener(view -> imageChooser());

        textViewImage.setOnClickListener(view -> imageChooser());

        setUpButton.setOnClickListener(view -> {
//auth.signOut();
            String email = editTextEmailRegister.getText().toString();
            String password = editTextPasswordRegister.getText().toString();
            String username = editTextUsername.getText().toString();
            String displayName = displayNames_ET.getText().toString();

            if (!email.isEmpty() && !password.isEmpty() && !username.isEmpty())
            {
                if(password.length() >= 6)
                {
                    if(editTextUsername.length() > 3)
                    {
                        setUpButton.setVisibility(View.INVISIBLE);
                        progressBarSetUp.setVisibility(View.VISIBLE);
                        usernameRef.child(username.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){

                                    Toast.makeText(SetUpProfileActivity.this, getString(R.string.usernameExist), Toast.LENGTH_SHORT).show();
                                    setUpButton.setVisibility(View.VISIBLE);
                                    progressBarSetUp.setVisibility(View.GONE);

                                } else {    // set up user account

                                    setUpAccount(email, password, username, displayName);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                setUpButton.setVisibility(View.VISIBLE);
                                progressBarSetUp.setVisibility(View.GONE);
                                Toast.makeText(SetUpProfileActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else Toast.makeText(SetUpProfileActivity.this, getString(R.string.userNameError), Toast.LENGTH_SHORT).show();

                } else Toast.makeText(SetUpProfileActivity.this, getString(R.string.passwordAbove5), Toast.LENGTH_SHORT).show();

            } else Toast.makeText(SetUpProfileActivity.this, getString(R.string.fieldEmpty), Toast.LENGTH_SHORT).show();

        });

        getOnBackPressedDispatcher().addCallback(callback);
    }


    public void imageChooser()
    {
        if(!permissions.isStorageOk(this)){
            permissions.requestStorage(this);
        } else {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncherForSelectImage.launch(intent);
        }

    }

    private void registerActivityForSelectImage() {

        activityResultLauncherForSelectImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RESULT_OK && data != null){

                        Intent intent = new Intent(this, UploadProfileImage.class);
                        intent.putExtra(K.PICKED_IMAGE_URI_PATH, data.getData().toString());
                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == K.STORAGE_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncherForSelectImage.launch(intent);

        } else {
            Toast.makeText(this, getString(R.string.permission) + grantResults[0], Toast.LENGTH_SHORT).show();
        }
    }

    private void signInNewEmail(String email, String password){

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
           if(task.isSuccessful()) {

                savePhoto();

           } else {
               System.out.println("what is login error: " + task.getException().getMessage());
               Toast.makeText(this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
           }
        });
    }

    public void setUpAccount(String email, String password, String userName, String displayName)
    {
        ProfileApiDao profileApiDao = K.retrofit.create(ProfileApiDao.class);

        IdTokenUtil.generateToken(token -> {

            SetUpM setUpM = new SetUpM(token, email, password, displayName, userName);

            profileApiDao.setUp(setUpM).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if(response.isSuccessful()){

                        signInNewEmail(email, password);

                        SharedPreferences myProfileShareRef = getSharedPreferences(auth.getUid(), Context.MODE_PRIVATE);
                        myProfileShareRef.edit().putString(K.PROFILE_USERNAME, userName).apply();
                        myProfileShareRef.edit().putString(K.PROFILE_DISNAME, displayName).apply();

                    } else{
                        Toast.makeText(SetUpProfileActivity.this, getString(R.string.errorOccur), Toast.LENGTH_SHORT).show();
                    }

                    setUpButton.setVisibility(View.VISIBLE);
                    progressBarSetUp.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable throwable) {
                    setUpButton.setVisibility(View.VISIBLE);
                    progressBarSetUp.setVisibility(View.GONE);
                    System.out.println("what is SetUpA L258: " + throwable.getMessage());

                    if(throwable.getMessage().contains("Failed to connect")) {  // server error
                        Toast.makeText(SetUpProfileActivity.this, getString(R.string.serverError), Toast.LENGTH_SHORT).show();
                    } else{     // no internet connection | timeout
                        Toast.makeText(SetUpProfileActivity.this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }, this);

    }

    private void savePhoto(){

        // check if the user has chosen an image
        if(imageCheck)
        {
            // -------- create an ID that each image will have
            UUID randomID = UUID.randomUUID();
            String imagePath = "images/"+ auth.getUid() + "/" +randomID+".jpg";

            storageReference.child(imagePath).putFile(Uri.parse(imageLink)).addOnSuccessListener(taskSnapshot ->
            {
                //  Create another StorageReference to get the url link where the image has been save in the storage
                StorageReference myStorageRef = storage.getReference(imagePath);

                myStorageRef.getDownloadUrl().addOnSuccessListener(uri ->
                {
                    String filePath = uri.toString();
                    if(auth.getUid() == null) return;
                    refUsers.child(auth.getUid()).child("general").child("image").setValue(filePath);
                    refUsers.child(auth.getUid()).child("general").child("imageUUID").setValue(imagePath);

                    Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Toast.makeText(SetUpProfileActivity.this, getString(R.string.profileSetUpSuccess), Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e ->
                {
                    Toast.makeText(SetUpProfileActivity.this, getString(R.string.imageUploadError), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });

            });

        }
        else{
            Intent intent = new Intent(SetUpProfileActivity.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(SetUpProfileActivity.this, getString(R.string.profileSetUpSuccess), Toast.LENGTH_SHORT).show();
            finish();
        }

        resetLoginSharePref.edit().remove(auth.getUid()).apply();

    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(close == 0){
                Toast.makeText(SetUpProfileActivity.this, getString(R.string.pressAgain), Toast.LENGTH_SHORT).show();
                close = 1;
                new Handler().postDelayed( ()-> close = 0, 5_000);
            } else {
                finish();
            }
        }
    };

    // ====     image interface
    @Override
    public void sendImageData(Uri imageUriPath) {
        Picasso.get().load(imageUriPath).into(imageViewCircle);

        imageLink = imageUriPath.toString();
        imageCheck = true;
    }

    @Override
    public void getCurrentModelChat(MessageModel messageModel, int position) {

    }

    @Override
    public void onColorChange(int color) {

    }

    @Override
    public void onImageClick() {

    }


}













