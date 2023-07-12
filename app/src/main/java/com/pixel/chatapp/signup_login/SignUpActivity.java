package com.pixel.chatapp.signup_login;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pixel.chatapp.R;
import com.pixel.chatapp.home.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private CircleImageView imageViewCircle;
    private TextInputEditText editTextEmailRegister, editTextPasswordRegister, editTextUsername;
    private Button buttonSignUp;
    TextView textViewImage;
    Uri imageUri;
    private Boolean imageCheck = false;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;

    // storage for image
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        imageViewCircle = findViewById(R.id.imageViewCircle);
        editTextEmailRegister = findViewById(R.id.editTextEmailSignup);
        editTextPasswordRegister = findViewById(R.id.editTextPasswordSignup);
        editTextUsername = findViewById(R.id.editTextUserNameSignup);
        textViewImage = findViewById(R.id.textViewImage);
        buttonSignUp = findViewById(R.id.buttonRegister);

        // --------------------- activating Firebase and database
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        // --------------------- activating Firebase for image storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageViewCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        textViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = editTextEmailRegister.getText().toString();
                String password = editTextPasswordRegister.getText().toString();
                String username = editTextUsername.getText().toString();

                if (!email.equals("") && !password.equals("") && !username.equals("")){
                    signUp(email, password, username);
                }
            }
        });

    }

    //--------------    another method to get permission from user phone
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
            Picasso.get().load(imageUri).into(imageViewCircle);     // which xml id it should display
            imageCheck = true;
        } else imageCheck = false;
    }

    public void signUp(String email, String password, String userName)
    {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    //  --------- save the username to the database
                    reference.child("Users").child(auth.getUid()).child("userName").setValue(userName);

                    //  --------- save empty msg to the database
                    reference.child("Users").child(auth.getUid()).child("message").setValue("");

                    // check if the user has chosen an image
                    if(imageCheck)
                    {
                        // -------- create an ID that each image will have
                        UUID randomID = UUID.randomUUID();
                        String imageName = "images/"+randomID+".jpg";
                        //  Save the UUID number incase you want to change the image later
                        reference.child("Users").child(auth.getUid()).child("imageUUID").setValue(imageName);
                        // now save the image to the firebase
                        storageReference.child(imageName).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //  Create another StorageReference to get the url link where the image has been save in the storage
                                StorageReference myStorageRef = storage.getReference(imageName);

                                myStorageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String filePath = uri.toString();       // convert img to string and save to FirebaseDataBase
                                    reference.child("Users").child(auth.getUid()).child("image").setValue(filePath).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(SignUpActivity.this, "Image saved successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignUpActivity.this, "Image not saved successfully" + e, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            }
                        });
                    }
                    else{
                        reference.child("Users").child(auth.getUid()).child("image").setValue("null");
                    }

                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                    Toast.makeText(SignUpActivity.this, "Login fails... Check network or credentials.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}













