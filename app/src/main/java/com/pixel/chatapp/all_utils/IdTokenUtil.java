package com.pixel.chatapp.all_utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Objects;

public class IdTokenUtil {

    public static void generateToken(GetTokenListener tokenListener){

        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())

                .getIdToken(true)

                .addOnSuccessListener(result -> tokenListener.getIdToken(result.getToken()));
    }


    public interface GetTokenListener {

        void getIdToken(String token);
    }

}
