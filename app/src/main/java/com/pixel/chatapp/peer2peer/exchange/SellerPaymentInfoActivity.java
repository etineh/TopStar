package com.pixel.chatapp.peer2peer.exchange;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.Permission.Permission;
import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.peer2peer.FingerprintActivity;
import com.pixel.chatapp.side_bar_menu.wallet.TransactionReceiptActivity;

import java.util.concurrent.Executor;

public class SellerPaymentInfoActivity extends AppCompatActivity {

    TextView markOrderNotify_TV, progressInfo_TV, timeCount_TV, amount_TV, fee_TV;
    LinearLayout alertTrader_linearLay;

    RelativeLayout acknowledgeLayout;
    TextView payAmountNotice_TV, userAccountName_TV, accountNumber_TV, bankName_TV;
    TextView iHavePaid_Button, cancleOrder_TV, order_ID_TV, orderTime_TV;
    ImageView copy_Order_ID_IV, copyAccountNumber, copy_Name, copy_BankName_IV, backArrow;
    ImageView cancelAct_IV;
    CheckBox checkBoxTerms_, checkBoxTerms_2;
    TextView completedButton, releaseFund_Button;
    ConstraintLayout pinContainerAck;
    EditText enterAckPin_ET;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    Permission permission_ = new Permission();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_payment_info);

        markOrderNotify_TV = findViewById(R.id.markOrderNotify_TV); // not used yet
        progressInfo_TV = findViewById(R.id.progressInfo_TV);
        timeCount_TV = findViewById(R.id.timeCount_TV);
        alertTrader_linearLay = findViewById(R.id.alertTrader_linearLay);
        amount_TV = findViewById(R.id.amount_TV);
        fee_TV = findViewById(R.id.fee_TV);

        order_ID_TV = findViewById(R.id.order_ID_TV);
        orderTime_TV = findViewById(R.id.orderTime_TV);
        payAmountNotice_TV = findViewById(R.id.payAmountNotice_TV);
        userAccountName_TV = findViewById(R.id.userAccountName_TV);
        accountNumber_TV = findViewById(R.id.accountNumber_TV);
        bankName_TV = findViewById(R.id.bankName_TV);
        backArrow = findViewById(R.id.backArrow);
        releaseFund_Button = findViewById(R.id.releaseFund_Button);

        iHavePaid_Button = findViewById(R.id.iHavePaid_Button);
        cancleOrder_TV = findViewById(R.id.cancleOrder_TV);
        copy_Order_ID_IV = findViewById(R.id.copy_Order_ID_IV);
        copyAccountNumber = findViewById(R.id.copyAccountNumber);
        copy_Name = findViewById(R.id.copy_Name);
        copy_BankName_IV = findViewById(R.id.copy_BankName_IV);
        alertTrader_linearLay = findViewById(R.id.alertTrader_linearLay);

        acknowledgeLayout = findViewById(R.id.acknowledge_payment_layout);
        cancelAct_IV = acknowledgeLayout.findViewById(R.id.cancelAct_IV);
        checkBoxTerms_ = acknowledgeLayout.findViewById(R.id.checkBoxTerms_);
        checkBoxTerms_2 = acknowledgeLayout.findViewById(R.id.checkBoxTerms_2);
        completedButton = acknowledgeLayout.findViewById(R.id.completedButton);
        pinContainerAck = acknowledgeLayout.findViewById(R.id.pinContainerAck);
        enterAckPin_ET = acknowledgeLayout.findViewById(R.id.enterAckPin_ET);

        // Create an executor
        executor = ContextCompat.getMainExecutor(this);


        releaseFund_Button.setOnClickListener(v -> {
            acknowledgeLayout.setVisibility(View.VISIBLE);
            enterAckPin_ET.setText(null);

            new Handler().postDelayed( ()-> enterAckPin_ET.requestFocus(), 500);
        });


        // go to pin / fingerprint activity
        completedButton.setOnClickListener(v -> {
            if(checkBoxTerms_.isChecked() && checkBoxTerms_2.isChecked()){

                if(enterAckPin_ET.length() == 4) {   // validate pin later
                    // send
                    completeOrder();
                } else {
                    Toast.makeText(this, getString(R.string.pin_4), Toast.LENGTH_SHORT).show();
                }

//                if(pinContainerAck.getVisibility() == View.VISIBLE){
//
//
//                } else{ // use fingerprint for verification
//                    showFingerPrint();
//                }

            } else {
                Toast.makeText(this, getString(R.string.tick_box), Toast.LENGTH_SHORT).show();
            }
        });

        // copy orderID
        copy_Order_ID_IV.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                PhoneUtils.copyText(this, order_ID_TV);
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        //copy account name
        copy_Name.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                PhoneUtils.copyText(this, userAccountName_TV);
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        //copy account number
        copyAccountNumber.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                PhoneUtils.copyText(this, accountNumber_TV);
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        // copy bank name
        copy_BankName_IV.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                PhoneUtils.copyText(this, bankName_TV);
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        cancelAct_IV.setOnClickListener(v -> onBackPressed());

        backArrow.setOnClickListener(v -> onBackPressed());


    }



    //  ==========      methods     =================

//    private void showFingerPrint(){
//        // Create a BiometricPrompt instance
//
//        if(permission_.isBiometricOk(this)){
//
//            biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
//                @Override
//                public void onAuthenticationError(int errorCode, CharSequence errString) {
//                    super.onAuthenticationError(errorCode, errString);
//                    // push user to pin page    --10 is back press, 13 is using pin
//                    if(errorCode == 13){    // user wants to use pin
//                        pinContainerAck.setVisibility(View.VISIBLE);
//                        enterAckPin_ET.requestFocus();
//
//                    }
//
//                }
//
//                @Override
//                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
//                    super.onAuthenticationSucceeded(result);
//                    completeOrder();
//                }
//
//                @Override
//                public void onAuthenticationFailed() {
//                    super.onAuthenticationFailed();
//                    Toast.makeText(SellerPaymentInfoActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            // Create a BiometricPrompt.PromptInfo instance
//            promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                    .setTitle("Biometric authentication")
//                    .setSubtitle("If fingerprint is dirty, you can choose 'verify by PIN'.")
//                    .setNegativeButtonText("Or\n Verify by PIN")
//                    .build();
//
//            // Show the fingerprint authentication dialog
//            biometricPrompt.authenticate(promptInfo);
//
//        } else {
//            permission_.requestBiometric(this);
//        }
//
//    }

    private void completeOrder() {
        Toast.makeText(SellerPaymentInfoActivity.this, getString(R.string.orderCompleted), Toast.LENGTH_SHORT).show();
        // Handle authentication success
        Intent intent = new Intent(SellerPaymentInfoActivity.this, TransactionReceiptActivity.class);
        intent.putExtra("from", "P2P");
        startActivity(intent);

        new Handler().postDelayed( () -> finish(), 1000);
    }


    @Override
    public void onBackPressed() {
        if(acknowledgeLayout.getVisibility() == View.VISIBLE){
            acknowledgeLayout.setVisibility(View.GONE);
            PhoneUtils.hideKeyboard(this, enterAckPin_ET);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pinContainerAck.getVisibility() == View.VISIBLE){
            new Handler().postDelayed( ()-> enterAckPin_ET.requestFocus(), 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(pinContainerAck.getVisibility() == View.VISIBLE){
            PhoneUtils.hideKeyboard(this, enterAckPin_ET);
            enterAckPin_ET.clearFocus();
        }
    }

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == AllConstants.BIOMETRIC_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//                Toast.makeText(this, "Fingerprint verified!", Toast.LENGTH_SHORT).show();
//                showFingerPrint();
//            } else {
//                // Permission denied
//                Toast.makeText(this, "Permission denied to use biometric authentication", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


}