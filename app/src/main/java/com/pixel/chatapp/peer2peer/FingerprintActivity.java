package com.pixel.chatapp.peer2peer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.peer2peer.exchange.P2pExchangeActivity;

import java.util.concurrent.Executor;

public class FingerprintActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    Permission permission_ = new Permission();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        // Create an executor
        executor = ContextCompat.getMainExecutor(this);

        // call the perm
        showFingerPrint();

    }


    private void showFingerPrint(){
        if(permission_.isBiometricOk(this)){

            // Create a BiometricPrompt instance
            biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(FingerprintActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(FingerprintActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                    // Handle authentication success
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(FingerprintActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            });

            // Create a BiometricPrompt.PromptInfo instance
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric authentication")
                    .setSubtitle("Scan your fingerprint now.")
                    .setNegativeButtonText("Or\n Verify by PIN")
                    .build();

            // Show the fingerprint authentication dialog
            biometricPrompt.authenticate(promptInfo);

        } else {
            permission_.requestBiometric(this);
        }

    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AllConstants.BIOMETRIC_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "I have verify!", Toast.LENGTH_SHORT).show();
                showFingerPrint();
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied to use biometric authentication", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
}