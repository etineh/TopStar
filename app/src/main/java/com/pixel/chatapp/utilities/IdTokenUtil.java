package com.pixel.chatapp.utilities;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.pixel.chatapp.R;

import java.util.Objects;

public class IdTokenUtil {

    public static void generateToken(GetTokenListener tokenListener, Context context){

        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())

                .getIdToken(true)

                .addOnSuccessListener(result -> tokenListener.getIdToken(result.getToken()))
                .addOnFailureListener((fail)-> Toast.makeText(context, context.getString(R.string.checkInternet), Toast.LENGTH_SHORT).show());
    }


    public interface GetTokenListener {

        void getIdToken(String token);
    }

}
