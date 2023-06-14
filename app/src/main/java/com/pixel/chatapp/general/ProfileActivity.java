package com.pixel.chatapp.general;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import com.pixel.chatapp.R;
import com.pixel.chatapp.home.MainActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView imageViewUpdate;
    private TextInputEditText editTextUserNameUpdate;
    private TextView textViewImageUpdate, textViewInfo;
    private Button buttonUpdate;
    private String imageUUID;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri imageUri;
    private Boolean imageCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewUpdate = findViewById(R.id.imageViewUpdate);
        editTextUserNameUpdate = findViewById(R.id.editTextUserNameUpdate);
        textViewImageUpdate = findViewById(R.id.textViewImageUpdate);
        textViewInfo = findViewById(R.id.textViewInfo);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        //  activate firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        user = auth.getCurrentUser();
        // --------------------- activating Firebase for image storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        getUserInfo();

        textViewImageUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

        //   -------------------------  method  ---------------------------
    public void getUserInfo()
    {
        // get the saved details from firebase database
        reference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {      // snapshot get access to the user details saved

                String name = snapshot.child("userName").getValue().toString();
                // remember we convert the img to uri link string. So the url link will fetch the image from the firebase storage
                String image = snapshot.child("image").getValue().toString();
                // get the UUID image number
                imageUUID = snapshot.child("imageUUID").getValue().toString();

                editTextUserNameUpdate.setText(name);       // display the database userName to the input field

                if(image.equals("null") ){
                    imageViewUpdate.setImageResource(R.drawable.person);
                } else{
                    Picasso.get().load(image).into(imageViewUpdate);    // get the image into the imageView xml id
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //      --------- update the profile
    public void updateProfile(){
        String userName = editTextUserNameUpdate.getText().toString();
        reference.child("Users").child(user.getUid()).child("userName").setValue(userName);

        // ---------- uploading image to the database
        if(imageCheck)
        {
            storageReference.child(imageUUID).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //  we already use the Picasso to save the uri of the image to the database
                    StorageReference myStorageRef = storage.getReference(imageUUID);
                    myStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();       // convert img to string and save to FirebaseDataBase
                            reference.child("Users").child(auth.getUid()).child("image").setValue(filePath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Image didn't change", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            });
        }
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.putExtra("userName", userName);
        startActivity(intent);
        finish();
    }

    //--------------    another method to get permission from user phone
    //  -----------     upload image start
    public void imageChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();  // get the file path
            //  we need Picasso to get the uri of the image saved on the database
            Picasso.get().load(imageUri).into(imageViewUpdate);     // which xml id it should display
            imageCheck = true;
        } else imageCheck = false;
    }
    //  -----------     upload image finish
}









