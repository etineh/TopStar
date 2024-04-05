package com.pixel.chatapp.peer2peer.exchange;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;
import com.pixel.chatapp.all_utils.PhoneUtils;

public class BuyerPaymentInfoActivity extends AppCompatActivity {

    TextView progressInfo_TV, timeCount_TV, payCation_TV, amount_TV, fee_TV, order_ID_TV, orderTime_TV;
    TextView payAmountNotice_TV, userAccountName_TV, accountNumber_TV, bankName_TV;
    TextView iHavePaid_Button, cancleOrder_TV;
    ImageView copy_Order_ID_IV, copyAccountNumber, copy_Name, copy_BankName_IV, backArrow;
    LinearLayout alertTrader_linearLay;
    RelativeLayout acknowledgeLayout;
    ImageView cancelAct_IV, backArr;
    CheckBox checkBoxTerms_;
    TextView completedButton;
    ConstraintLayout cancel_trade_layout;
    TextView cancelNow_TV;
    RadioGroup radioGroup;
    EditText getOtherReason_ET;
    String reason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_payment_info);

        progressInfo_TV = findViewById(R.id.progressInfo_TV);
        timeCount_TV = findViewById(R.id.timeCount_TV);
        payCation_TV = findViewById(R.id.payCation_TV);
        amount_TV = findViewById(R.id.amount_TV);
        fee_TV = findViewById(R.id.fee_TV);
        order_ID_TV = findViewById(R.id.order_ID_TV);
        orderTime_TV = findViewById(R.id.orderTime_TV);
        payAmountNotice_TV = findViewById(R.id.payAmountNotice_TV);
        userAccountName_TV = findViewById(R.id.userAccountName_TV);
        accountNumber_TV = findViewById(R.id.accountNumber_TV);
        bankName_TV = findViewById(R.id.bankName_TV);
        backArrow = findViewById(R.id.backArrow);

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
        completedButton = acknowledgeLayout.findViewById(R.id.completedButton);

        cancel_trade_layout = findViewById(R.id.cancel_trade_layout);
        cancelNow_TV = cancel_trade_layout.findViewById(R.id.cancelNow_TV);
        radioGroup = cancel_trade_layout.findViewById(R.id.radioGroup);
        getOtherReason_ET = cancel_trade_layout.findViewById(R.id.getOtherReason_ET);
        backArr = cancel_trade_layout.findViewById(R.id.backArr);

        completedButton.setOnClickListener(v -> {
            acknowledgeLayout.setVisibility(View.GONE);
            iHavePaid_Button.setBackgroundResource(0);
            iHavePaid_Button.setText(getString(R.string.report));
            iHavePaid_Button.setTextColor(ContextCompat.getColor(this, R.color.orange));

            // reset time and text
            progressInfo_TV.setText(getString(R.string.payCompleted));
//            timeCount_TV.setText(getString(R.string.payCompleted));     // reset the time count
            payCation_TV.setText(getString(R.string.payCation));
            payAmountNotice_TV.setText(getString(R.string.youPaidTo));

        });

        // close the complete layout
        cancelAct_IV.setOnClickListener(v -> {
            acknowledgeLayout.setVisibility(View.GONE);
        });

        // I have paid button // or make report
        iHavePaid_Button.setOnClickListener(v -> {
            if(iHavePaid_Button.getText().toString().equals(getString(R.string.report))) {
                Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
            } else {
                // pop up the acknowledge layout to sign he has paid
                acknowledgeLayout.setVisibility(View.VISIBLE);
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


        backArrow.setOnClickListener(v -> onBackPressed());
        backArr.setOnClickListener(v -> onBackPressed());

        cancleOrder_TV.setOnClickListener(v -> {
            radioListener();
            cancel_trade_layout.setVisibility(View.VISIBLE);
//            Intent intent = new Intent(this, )
        });

        cancelNow_TV.setOnClickListener(v -> {
            // send the report to database later
            if(reason != null){
                PhoneUtils.hideKeyboard(this, getOtherReason_ET);
                if(reason.equals("Other") && getOtherReason_ET.getText().length() > 3){ // send the report to database later
                    reason = getOtherReason_ET.getText().toString();
                    Toast.makeText(this, getString(R.string.order_cancel), Toast.LENGTH_SHORT).show();
                    finish();
                } else if (!reason.equals("Other")) {   // send the report to database later
                    finish();
                    Toast.makeText(this, getString(R.string.order_cancel), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, getString(R.string.stateReason), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, getString(R.string.stateReason), Toast.LENGTH_SHORT).show();
            }
        });


    }



    //  =========       method      ==========

    private void radioListener(){
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Handle the checked radio button
            if (checkedId == R.id.otherReason) {
                // get the string on the radius
                getOtherReason_ET.setVisibility(View.VISIBLE);
                getOtherReason_ET.requestFocus();
                RadioButton radioButton = findViewById(checkedId);
                reason = radioButton.getText().toString();

            } else {
                getOtherReason_ET.setVisibility(View.GONE);
                RadioButton radioButton = findViewById(checkedId);
                reason = radioButton.getText().toString();
                getOtherReason_ET.clearFocus();
//                Toast.makeText(this, ""+ radioButton.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {

        if(acknowledgeLayout.getVisibility() == View.VISIBLE){
            acknowledgeLayout.setVisibility(View.GONE);
        } else if (cancel_trade_layout.getVisibility() ==View.VISIBLE) {
            cancel_trade_layout.setVisibility(View.GONE);

        } else {

            // pop up why do you want to cancel later

            super.onBackPressed();
            finish();
        }
    }
}












