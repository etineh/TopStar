package com.pixel.chatapp.side_bar_menu.settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pixel.chatapp.UploadProfileImage;
import com.pixel.chatapp.interface_listeners.ImageListener;
import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.ZoomImage;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.MessageModel;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements ImageListener {

    private static CircleImageView imageViewUpdate;
    private static TextInputEditText userName_ET;
    private static TextView textViewImageUpdate, textViewInfo;
    private ProgressBar progressBar;
    private Button buttonUpdate;
    private String imageFirebasePath;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageReference;

    Permission permissions = new Permission();
    public ProfileActivity profileActivity = this;
    private static Boolean imageCheck = false;

    private static String imageLink;

    ActivityResultLauncher<Intent> activityResultLauncherForSelectImage;

    private ValueEventListener profileListener; // Declare the listener as a class variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewUpdate = findViewById(R.id.imageViewUpdate);
        userName_ET = findViewById(R.id.userName_ET);
        textViewImageUpdate = findViewById(R.id.textViewImageUpdate);
        textViewInfo = findViewById(R.id.textViewInfo);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        progressBar = findViewById(R.id.progressBar);

        //  activate firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        user = auth.getCurrentUser();
        // --------------------- activating Firebase for image storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // register the launcher
        registerActivityForSelectImage();

        getUserInfo();

        textViewImageUpdate.setOnClickListener(view -> imageChooser());

        imageViewUpdate.setOnClickListener(view -> {
            Intent i = new Intent(this, ZoomImage.class);
            i.putExtra("otherName", "My Profile Photo");
            i.putExtra("imageLink", imageLink);
            startActivity(i);
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    //   -------------------------  method  ---------------------------

    private Uri resolveContentUri(Uri contentUri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        try (Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                String filePath = cursor.getString(columnIndex);
                return Uri.parse(filePath);
            } else {
                Log.e("resolveContentUri", "Cursor is null or empty");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("resolveContentUri", "Failed to resolve content URI: " + contentUri, e);
        }
        return null;
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

//                            selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                            imageViewUpdate.setImageBitmap(selectedImage);

                    }
                }
        );
    }

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
    //  -----------     upload image start
    public void imageChooser()
    {
        if(!permissions.isStorageOk(ProfileActivity.this)){
            permissions.requestStorage(ProfileActivity.this);
        } else {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncherForSelectImage.launch(intent);
        }

    }

    public void getUserInfo()
    {
        // get the saved details from firebase database
        profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {      // snapshot get access to the user details saved

                String name = snapshot.child("userName").getValue().toString();
                // remember we convert the img to uri link. So the url link will fetch the image from the firebase storage
                imageLink = snapshot.child("image").getValue().toString();
                // get the image path that leads to the fireStore i.e images/448f87cd-8264-46c4-b106-2071339bb302.jpg
                imageFirebasePath = snapshot.child("imageUUID").getValue().toString();
                userName_ET.setText(name);       // display the database userName to the input field

                userName_ET.requestFocus();
                // Set the cursor to the end of the text
                userName_ET.setSelection(userName_ET.getText().length());

                if(imageLink.equals("null") ){
                    imageViewUpdate.setImageResource(R.drawable.person);
                } else{
                    Picasso.get().load(imageLink).into(imageViewUpdate);    // get the image into the imageView xml id
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        reference.child("Users").child(user.getUid()).addValueEventListener(profileListener);
    }

    //      --------- update the profile
    public void updateProfile(){

        String userName = userName_ET.getText().toString();
        reference.child("Users").child(user.getUid()).child("userName").setValue(userName);
        // save new username to local db
        MainActivity.myUserNamePreferences.edit().putString(AllConstants.USERNAME, userName).apply();


        // ---------- uploading image to the database
        if(imageCheck)
        {
            progressBar.setVisibility(View.VISIBLE);
            buttonUpdate.setVisibility(View.GONE);

            storageReference.child(imageFirebasePath).putFile(Uri.parse(imageLink))
                    .addOnSuccessListener(taskSnapshot -> {
                        //  get the uri where the image was saved in database
                        StorageReference myStorageRef = storage.getReference(imageFirebasePath);
                        myStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Get the image uri link and save to user DB
                                String imageLink_ = uri.toString();

                                reference.child("Users").child(auth.getUid())
                                        .child("image").setValue(imageLink_)
                                        .addOnSuccessListener(unused -> {

                                            Toast.makeText(ProfileActivity.this,
                                                    "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                            imageCheck = false;
                                            progressBar.setVisibility(View.GONE);
                                            buttonUpdate.setVisibility(View.VISIBLE);

                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(ProfileActivity.this,
                                                    "Image didn't change", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
                    });
        }

        // change the username local storage
//        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
//        intent.putExtra("userName", userName);
//        startActivity(intent);
//        finish();
    }

    @Override
    public void sendImageData(Uri imagePath) {
//        userName_ET.setText(imagePath+"");
        Picasso.get().load(imagePath).into(imageViewUpdate);

        imageLink = imagePath.toString();
        imageCheck = true;
    }

    @Override
    public void getCurrentModelChat(MessageModel messageModel, int position) {

    }

    @Override
    public void onColorChange(int color) {

    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
//        {
//            imagePathOnPhone = data.getData();  // get the file path
//            //  we need Picasso to get the uri of the image saved on the database
//            Picasso.get().load(imagePathOnPhone).into(imageViewUpdate);     // which xml id it should display
//            imageCheck = true;
//        } else imageCheck = false;
//    }
    //  -----------     upload image finish


    @Override
    protected void onPause() {
        super.onPause();
        if (profileListener != null) {
            reference.child("Users").child(user.getUid()).removeEventListener(profileListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (profileListener != null) {
            reference.child("Users").child(user.getUid()).removeEventListener(profileListener);
        }
        finish();

        // Start or bring MainActivity to the front
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainActivityIntent);
    }
}









