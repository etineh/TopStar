package com.pixel.chatapp.utils;

import com.google.firebase.auth.FirebaseAuth;

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
