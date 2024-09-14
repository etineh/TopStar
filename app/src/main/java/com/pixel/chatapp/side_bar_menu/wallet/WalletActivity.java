package com.pixel.chatapp.side_bar_menu.wallet;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.pixel.chatapp.activities.CaptureAct;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.FundTransferUserAdapter;
import com.pixel.chatapp.utils.BarcodeUtils;
import com.pixel.chatapp.utils.PhoneUtils;
import com.pixel.chatapp.model.FundTransferUser;
import com.pixel.chatapp.peer2peer.exchange.P2pExchangeActivity;

import java.util.ArrayList;
import java.util.List;

public class WalletActivity extends AppCompatActivity implements FundTransferUserAdapter.ProceedToTransferPage {

    private ImageView resetPinButton, customerCareButton, arrowBack, hideAmount_IV;
    private TextView totalAssetAmount_TV, localCurrencyAmount_TV,dollarAmount_TV, gameAmount_TV, bonusAmount_TV,
            merchantAmount_TV, lockAmount_TV;
    private CardView depositCardView,withdrawCardView, transferCardView, convertCardView, p2pCardView;
    private CardView cardViewLocalAmount, cardViewUDSTAmount, cardViewGameAmount, cardViewBonusAmount,
            cardViewP2PMerchant, cardViewLock;

    private ConstraintLayout usdtPaymentOption, p2pPaymentOption, withdrawOrDepositContainer, trans_background;
    private TextView withdrawOrDeposit_TV, p2pInfo_TV, usdtInfo_TV, gameCurrency_TV;

    //  ----------  transfer var
    private ConstraintLayout transferPayeeListLayout;
    private ImageView closeTransferArrow, searchButton, cancleTransferButton;
    private RadioButton localCurrencyRadioButton, usdtRadioButton;
    private EditText searchUser_ET, transferAmount_ET;
    @SuppressLint("StaticFieldLeak")
    private static ScrollView transferPageLayout;
    private TextView getTransferConvertAmount_TV, sendTransferButton;
//    private ImageView fingerprint_trans_IV;


    //  withdraw variables
    private ScrollView withdrawPageLayout;
    private ImageView cancleWithdraw, scanAddress;
    private EditText usdtWithdrawAddress_ET, withdrawAmount_ET;
    private Spinner spinner;
    private TextView sendWithdrawal, withdrawConvertAmount_TV;
    RadioGroup radioGroupWithdraw;

    // usdt deposit network selection
    private ScrollView usdtNetworkLayout;
    private ImageView closeUsdtNetwork;
    private ConstraintLayout bep20NetworkOption, trc20NetworkOption;
    private String networkSelectionIs;

    // usdt deposit page
    private ScrollView depositPageLayout;
    private ImageView closeDeposit_IV, barCode, copyAddress, changeNetwork;
    private TextView setUsdtAddress_TV, copyAddress_TV;

    //  transfer recycler adapter
    private RecyclerView recyclerViewTransfer;
    private FundTransferUserAdapter fundTransferUserAdapter;

    private List<FundTransferUser> userList;

    private String userSelection;

    //  acknowledge layout for withdraw and send
    RelativeLayout acknowledgeLayout;
    EditText enterPin_ET;
    ImageView cancelAct_IV;
    TextView sendTransactionButton, forgetPin_Button;

    // biometric fingerprint
//    private Executor executor;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
//
//        ViewStub viewStub = findViewById(R.id.viewstub);
//
//        new Handler().postDelayed(()->
//        {
//            view = viewStub.inflate();
//            if (view != null) {
//                Toast.makeText(this, "Done inflating all views", Toast.LENGTH_SHORT).show();
//            }
//        }, 50);

        resetPinButton = findViewById(R.id.resetPin_IV);
        customerCareButton = findViewById(R.id.support_IV);
        arrowBack = findViewById(R.id.arrowBack_IV);
        hideAmount_IV = findViewById(R.id.hideAmount_IV);
        totalAssetAmount_TV = findViewById(R.id.totalAssetAmount_TV);
        localCurrencyAmount_TV = findViewById(R.id.localCurrencyAmount_TV);
        dollarAmount_TV = findViewById(R.id.dollarAmount_TV);
        gameAmount_TV = findViewById(R.id.gameAmount_TV);
        bonusAmount_TV = findViewById(R.id.bonusAmount_TV);
        merchantAmount_TV = findViewById(R.id.merchantAmount_TV);
        lockAmount_TV = findViewById(R.id.lockAmount_TV);

        // asset containers
        depositCardView = findViewById(R.id.depositCardView);
        withdrawCardView = findViewById(R.id.withdrawCardView);
        transferCardView = findViewById(R.id.transferCardView);
        convertCardView = findViewById(R.id.convertCardView);
        p2pCardView = findViewById(R.id.p2pCardView);

        // asset amounts
        cardViewLocalAmount = findViewById(R.id.cardViewLocalCurrency);
        cardViewUDSTAmount = findViewById(R.id.cardViewUDSTAsset);
        cardViewGameAmount = findViewById(R.id.cardViewGameAsset);
        cardViewBonusAmount = findViewById(R.id.cardViewBonusAsset);
        cardViewP2PMerchant = findViewById(R.id.cardViewP2PMerchantAsset);
        cardViewLock = findViewById(R.id.cardViewLockAsset);

        p2pInfo_TV = findViewById(R.id.p2pInfo_TV);
        usdtInfo_TV = findViewById(R.id.usdtInfo_TV);
        gameCurrency_TV = findViewById(R.id.gameCurrency_TV);

        // deposit and withdraw option
        usdtPaymentOption = findViewById(R.id.usdtPaymentOption);
        p2pPaymentOption = findViewById(R.id.p2pPaymentOption);
        trans_background = findViewById(R.id.trans_background);
        withdrawOrDepositContainer = findViewById(R.id.withdrawOrDepositContainer);
        withdrawOrDeposit_TV = findViewById(R.id.withdrawOrDeposit_TV);
        bonusAmount_TV = findViewById(R.id.bonusAmount_TV);

        // transfer ids and layoutPage
        transferPayeeListLayout = findViewById(R.id.transferPayeeListLayout);
        closeTransferArrow = transferPayeeListLayout.findViewById(R.id.arrowCloseButton);
        searchButton = transferPayeeListLayout.findViewById(R.id.searchButton);
        searchUser_ET = transferPayeeListLayout.findViewById(R.id.seachUser_ET);
        // transfer page (amount and pin)   -- layout page
        transferPageLayout = findViewById(R.id.transferPageLayout);
        cancleTransferButton = transferPageLayout.findViewById(R.id.cancleTransferButton);
        transferAmount_ET = transferPageLayout.findViewById(R.id.transferAmount_ET);
        localCurrencyRadioButton = transferPageLayout.findViewById(R.id.localCurrencyRadioButton);
        usdtRadioButton = transferPageLayout.findViewById(R.id.usdtRadioButton);
        getTransferConvertAmount_TV = transferPageLayout.findViewById(R.id.getTransferConvertAmount_TV);
        sendTransferButton = transferPageLayout.findViewById(R.id.sendButton);
        RadioGroup radioGroupTransfer = transferPageLayout.findViewById(R.id.radioGroup);
//        RadioButton localRadio = transferPayeeListLayout.findViewById(R.id.localCurrencyRadioButton);
        radioGroupTransfer.check(R.id.localCurrencyRadioButton); // Set the default selection to the localCurrencyRadioButton


        // withdraw ids and layoutPage
        withdrawPageLayout = findViewById(R.id.withdrawPageLayout);
        cancleWithdraw = withdrawPageLayout.findViewById(R.id.cancleTransferButton);
        usdtWithdrawAddress_ET = withdrawPageLayout.findViewById(R.id.usdtAddress_ET);
        withdrawAmount_ET = withdrawPageLayout.findViewById(R.id.enterAmount_ET);
        sendWithdrawal = withdrawPageLayout.findViewById(R.id.sendButton);
        withdrawConvertAmount_TV = withdrawPageLayout.findViewById(R.id.getTransferConvertAmount_TV);
//        cancleWithdraw = withdrawPageLayout.findViewById(R.id.cancleTransferButton);
        scanAddress = withdrawPageLayout.findViewById(R.id.scanAddress);
        spinner = withdrawPageLayout.findViewById(R.id.chooseNetwork_TV);
        spinner.setSelection(0); // Set the default selection to the first item in the array
        radioGroupWithdraw = withdrawPageLayout.findViewById(R.id.radioGroup);
        radioGroupWithdraw.check(R.id.usdtRadioButton); // Set the default selection to the localCurrencyRadioButton

        // acknowledge layout
        acknowledgeLayout = findViewById(R.id.acknowledgeLayout);
        enterPin_ET = acknowledgeLayout.findViewById(R.id.enterPin_ET);
        cancelAct_IV = acknowledgeLayout.findViewById(R.id.cancelAct_IV);
        sendTransactionButton = acknowledgeLayout.findViewById(R.id.sendTransactionButton);
        forgetPin_Button = acknowledgeLayout.findViewById(R.id.forgetPin_Button);

        // usdt deposit network selection
        usdtNetworkLayout = findViewById(R.id.usdtNetworkLayout);
        closeUsdtNetwork = usdtNetworkLayout.findViewById(R.id.closeNetwork_IV);
        bep20NetworkOption = usdtNetworkLayout.findViewById(R.id.bep20NetworkOption);
        trc20NetworkOption = usdtNetworkLayout.findViewById(R.id.trc20NetworkOption);

        // usdt deposit page
        depositPageLayout = findViewById(R.id.depositPageLayout);
        closeDeposit_IV = depositPageLayout.findViewById(R.id.closeDeposit_IV);
        barCode = depositPageLayout.findViewById(R.id.barCode);
        copyAddress = depositPageLayout.findViewById(R.id.copyAddress_IV);
        changeNetwork = depositPageLayout.findViewById(R.id.changeNetwork);
        setUsdtAddress_TV = depositPageLayout.findViewById(R.id.usdtAddress_TV);
        copyAddress_TV = depositPageLayout.findViewById(R.id.copyAddress_TV);

        //  transfer recyclerView
        recyclerViewTransfer = transferPayeeListLayout.findViewById(R.id.recyclerViewTransfer);
        recyclerViewTransfer.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        getOnBackPressedDispatcher().addCallback(this, callback);

        // top buttons
        resetPinButton.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            }).start();

        });

        customerCareButton.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            }).start();

        });

        hideAmount_IV.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            }).start();

        });

        // Create an executor
//        executor = ContextCompat.getMainExecutor(this);

        // withdraw or deposit via selection method   ====================
        p2pPaymentOption.setOnClickListener(v -> {

            trans_background.setVisibility(View.GONE);
            withdrawOrDepositContainer.setVisibility(View.GONE);

            Intent intent = new Intent(this, P2pExchangeActivity.class);
            startActivity(intent);

        });

        usdtPaymentOption.setOnClickListener(v -> {

            if(userSelection.equals("withdraw"))
            {

                trans_background.setVisibility(View.GONE);
                withdrawOrDepositContainer.setVisibility(View.GONE);

                withdrawPageLayout.setVisibility(View.VISIBLE);

                withdrawAmount_ET.addTextChangedListener(listenToConvertAmount("withdraw"));

                withdrawRadioAndSpinner();

            } else if (userSelection.equals("deposit"))
            {
                trans_background.setVisibility(View.GONE);
                withdrawOrDepositContainer.setVisibility(View.GONE);
                usdtNetworkLayout.setVisibility(View.VISIBLE);
            }
            
        });

        //  ===============   Acknowledge -- confirm pin for withdraw or transfer      =======================
        sendTransactionButton.setOnClickListener(v -> {

            if(enterPin_ET.length() == 4){
                enterPin_ET.setText(null);
                withdrawAmount_ET.setText(null);
                transferAmount_ET.setText(null);

                transferAmount_ET.removeTextChangedListener(listenToConvertAmount("transfer"));
                withdrawAmount_ET.removeTextChangedListener(listenToConvertAmount("withdraw"));
                PhoneUtils.hideKeyboard(this, enterPin_ET);

                spinner.setSelection(0); // Set the default selection to the first item in the array

                if(userSelection.equals("transfer")){
                    sendTransferMethod();
                } else if (userSelection.equals("withdraw")){
                    sendWithdrawalMethod();
                }
                Toast.makeText(this, "successfully sent", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed( () -> {
                    withdrawPageLayout.setVisibility(View.GONE);
                    transferPageLayout.setVisibility(View.GONE);
                    transferPayeeListLayout.setVisibility(View.GONE);
                }, 1000);

                acknowledgeLayout.setVisibility(View.GONE);

            } else {
                Toast.makeText(this, getString(R.string.incorrectPin), Toast.LENGTH_SHORT).show();
            }

        });

        forgetPin_Button.setOnClickListener(v -> {
            Toast.makeText(this, "work in progress", Toast.LENGTH_SHORT).show();
        });

        cancelAct_IV.setOnClickListener(v -> onBackPressed());

        //  ===============     Make a deposit      =======================
        depositCardView.setOnClickListener(v -> {

            userSelection = "deposit";

            trans_background.setVisibility(View.VISIBLE);
            withdrawOrDepositContainer.setVisibility(View.VISIBLE);
            withdrawOrDeposit_TV.setText(getString(R.string.depositVia));
            usdtInfo_TV.setText(getString(R.string.usdtDepositInfo));
            p2pInfo_TV.setText(getString(R.string.p2pDepositInfo));

        });

        bep20NetworkOption.setOnClickListener(v -> {

            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                usdtNetworkLayout.setVisibility(View.GONE);
                networkSelectionIs = "bep20";
                depositPageLayout.setVisibility(View.VISIBLE);

                Bitmap barcodeBitmap = BarcodeUtils.generateBarcode("change to bep20 address later", 350, 350);
                barCode.setImageBitmap(barcodeBitmap); // Assuming imageView is an ImageView in your layout

                // pass the network selected later

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            }).start();

        });

        trc20NetworkOption.setOnClickListener(v -> {

            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                usdtNetworkLayout.setVisibility(View.GONE);
                networkSelectionIs = "trc20";
                depositPageLayout.setVisibility(View.VISIBLE);

                Bitmap barcodeBitmap = BarcodeUtils.generateBarcode("change to trc20 address later", 350, 350);
                barCode.setImageBitmap(barcodeBitmap); // Assuming imageView is an ImageView in your layout

                // pass the network selected later

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            }).start();

        });

        // show the network layout if user want to change the network
        changeNetwork.setOnClickListener(v -> {
            usdtNetworkLayout.setVisibility(View.VISIBLE);
        });

        // copy the usdt address
        View.OnClickListener copyUsdtAddress = v -> {

            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(() ->
            {
                PhoneUtils.copyText(this, setUsdtAddress_TV);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 500);

            }).start();

            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
        };
        copyAddress.setOnClickListener(copyUsdtAddress);
        copyAddress_TV.setOnClickListener(copyUsdtAddress);
//        setUsdtAddress_TV.setOnClickListener(copyUsdtAddress);

        // make a withdrawal
        withdrawCardView.setOnClickListener(v -> {
            userSelection = "withdraw";

            trans_background.setVisibility(View.VISIBLE);
            withdrawOrDepositContainer.setVisibility(View.VISIBLE);
            withdrawOrDeposit_TV.setText(getString(R.string.withdrawVia));
            usdtInfo_TV.setText(getString(R.string.usdtWithdrawInfo));
            p2pInfo_TV.setText(getString(R.string.p2pWithdrawInfo));

        });

        // hide the deposit/withdraw background when done
        trans_background.setOnClickListener(v -> {
            trans_background.setVisibility(View.GONE);
            withdrawOrDepositContainer.setVisibility(View.GONE);
        });


        // scan usdt address
        scanAddress.setOnClickListener(v -> {
            // Start barcode scanning
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan USDT Address");
            options.setBeepEnabled(false);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);
            barcodeLauncher.launch(options);
        });


        // send the withdrawal  ==-- proceed
        sendWithdrawal.setOnClickListener(v -> {

            if(withdrawAmount_ET.length() >= 2 ){ // verify pin later
                acknowledgeLayout.setVisibility(View.VISIBLE);
                withdrawAmount_ET.clearFocus();
                enterPin_ET.requestFocus();
            } else {
                Toast.makeText(this, getString(R.string.invalidAmount), Toast.LENGTH_SHORT).show();
            }

        });

        // ============ transfer funds to user in topstar    -------------------------------------
        transferCardView.setOnClickListener(v -> {

            userList.clear();
            userSelection = "transfer";

            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(() ->
            {
                transferPayeeListLayout.setVisibility(View.VISIBLE);
                searchUser_ET.requestFocus();

                // set data
                userList.add(new FundTransferUser(null, "Ndifreke Sunday", "@ndifreke", "12"));
                userList.add(new FundTransferUser(null, "Frank Umoro", "@umoro", "12"));
                userList.add(new FundTransferUser(null, "Favour Monday", "@favourFromGod", "12"));
                userList.add(new FundTransferUser(null, "King Solomon", "@kingsolomon", "12"));
                userList.add(new FundTransferUser(null, "Bassey Sunday", "@sundaybro", "12"));
                userList.add(new FundTransferUser(null, "Ruona Tin", "@tin_ruona", "12"));
                userList.add(new FundTransferUser(null, "Godstime Wisdom", "@wisdomTime", "12"));

                fundTransferUserAdapter = new FundTransferUserAdapter(userList, this);

                fundTransferUserAdapter.setTransferPageListener( (FundTransferUserAdapter.ProceedToTransferPage) WalletActivity.this );

                recyclerViewTransfer.setAdapter(fundTransferUserAdapter);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 50);

            }).start();

            radioGroupTransfer.setOnCheckedChangeListener((group, checkedId) -> {
                // Handle the checked radio button
                if (checkedId == R.id.localCurrencyRadioButton) {
                    // Do something when localCurrencyRadioButton is selected
//                    Toast.makeText(this, "local " + checkedId , Toast.LENGTH_SHORT).show();
                } else if (checkedId == R.id.usdtRadioButton) {
//                    Toast.makeText(this, "usdt " + checkedId , Toast.LENGTH_SHORT).show();
                }
            });

            transferAmount_ET.addTextChangedListener(listenToConvertAmount("transfer"));

        });
        
        // search the user you want to send the fund to     // transition available
        searchButton.setOnClickListener(v -> {
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(() ->
            {

                Toast.makeText(this, "Work in progress", Toast.LENGTH_SHORT).show();
                
                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        closeTransferArrow.setOnClickListener(v -> {
            transferPayeeListLayout.setVisibility(View.GONE);

        });

        // proceed with the transfer
        sendTransferButton.setOnClickListener(v -> {

            if(transferAmount_ET.length() >= 2){ // verify pin later
                acknowledgeLayout.setVisibility(View.VISIBLE);
                transferAmount_ET.clearFocus();
                enterPin_ET.requestFocus();
            } else {
                Toast.makeText(this, getString(R.string.invalidAmount), Toast.LENGTH_SHORT).show();
            }

        });

        //  ========    convert between currency    ================================================
        convertCardView.setOnClickListener(v -> {
            // open convert activity
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, ConvertFundActivity.class);
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();

        });

//        localCurrencyRadioButton.setOnClickListener(v -> {
//
//            Toast.makeText(this, "in progress" + localCurrencyRadioButton.getText(), Toast.LENGTH_SHORT).show();
//        });
//
//        usdtRadioButton.setOnClickListener(v -> {
//            Toast.makeText(this, "in progress" + usdtRadioButton.getText(), Toast.LENGTH_SHORT).show();
//        });


        //  ========    peer to peer p2p    ================================================
        p2pCardView.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, P2pExchangeActivity.class);
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();

        });

        //  ========    open various assets    ================================================
        cardViewLocalAmount.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, EachAssetActivity.class);
                intent.putExtra("assetType", "localAsset");
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        cardViewUDSTAmount.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, EachAssetActivity.class);
                intent.putExtra("assetType", "USDTAsset");
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        // open game wallet activity
        cardViewGameAmount.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, EachAssetActivity.class);
                intent.putExtra("assetType", "gameAsset");
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        // change the game currency to naira or usdt
        gameCurrency_TV.setOnClickListener(v -> {

            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        cardViewBonusAmount.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, EachAssetActivity.class);
                intent.putExtra("assetType", "BonusAsset");
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        cardViewP2PMerchant.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, EachAssetActivity.class);
                intent.putExtra("assetType", "MerchantAsset");
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });

        cardViewLock.setOnClickListener(v -> {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {

                Intent intent = new Intent(this, EachAssetActivity.class);
                intent.putExtra("assetType", "LockAsset");
                startActivity(intent);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 200);

            }).start();
        });



        // onBackPress
        View.OnClickListener backPress = v -> {
            getOnBackPressedDispatcher().onBackPressed();
        };
        arrowBack.setOnClickListener(backPress);
        cancleTransferButton.setOnClickListener(backPress);
        arrowBack.setOnClickListener(backPress);
        cancleWithdraw.setOnClickListener(backPress);
        closeUsdtNetwork.setOnClickListener(backPress);
        closeDeposit_IV.setOnClickListener(backPress);

    }

    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    usdtWithdrawAddress_ET.setText(result.getContents());
                }
            });

    //`========     methods     ==================

    private void sendWithdrawalMethod(){

        Intent intent = new Intent(this, TransactionReceiptActivity.class);
        intent.putExtra("from", "withdraw");
        startActivity(intent);

    }

    private void sendTransferMethod(){

        Intent intent = new Intent(this, TransactionReceiptActivity.class);
        intent.putExtra("from", "transfer");
        startActivity(intent);

    }

    private void withdrawRadioAndSpinner() {
        radioGroupWithdraw.setOnCheckedChangeListener((group, checkedId) -> {
            // Handle the checked radio button
            if (checkedId == R.id.localCurrencyRadioButton) {
                // Do something when localCurrencyRadioButton is selected
                withdrawAmount_ET.setHint(getString(R.string.localMin));

//                Toast.makeText(this, "local " + checkedId , Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.usdtRadioButton) {
                withdrawAmount_ET.setHint(getString(R.string.usdtMin));
//                Toast.makeText(this, "usdt " + checkedId , Toast.LENGTH_SHORT).show();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                // Do something with the selected item
//                Toast.makeText(getApplicationContext(), "Selected spiner: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });
    }

    private TextWatcher listenToConvertAmount(String type){

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // implement convert logic later
                if(type.equals("transfer"))
                {
                    String transferConvert = getString(R.string.convertForUser) + " " + s;
                    getTransferConvertAmount_TV.setText(transferConvert);

                } else if(type.equals("withdraw"))
                {
                    String withdrawConvert = getString(R.string.convertForAddress) + " " + s;
                    withdrawConvertAmount_TV.setText(withdrawConvert);

                }

            }
        };

        return textWatcher;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle scan result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Handle canceled scan
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
            } else {
                // Handle scan result
                String scannedData = result.getContents();
                System.out.println("Scanned data: " + scannedData);
                Toast.makeText(this, "Scanned data: " + scannedData, Toast.LENGTH_SHORT).show();
            }
        }
    }


//    private void showFingerPrint(String type){
//        // Create a BiometricPrompt instance
//        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
//            @Override
//            public void onAuthenticationError(int errorCode, CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//                // push user to pin page    --10 is back press, 13 is using pin
//                if(type.equals("withdraw"))
//                {
//                    if(errorCode == 13){
//                        pinWithdContainer.setVisibility(View.VISIBLE);
//                        orTV.setVisibility(View.GONE);
//                        fingerprint_IV.setVisibility(View.GONE);
//                    } else {
//                        pinWithdContainer.setVisibility(View.VISIBLE);
//                        orTV.setVisibility(View.VISIBLE);
//                        fingerprint_IV.setVisibility(View.VISIBLE);
//                    }
//                }
////                else {    // transfer
////                    if(errorCode == 13){
////                        pinTransContainer.setVisibility(View.VISIBLE);
////                        or_trans_TV.setVisibility(View.GONE);
////                        fingerprint_trans_IV.setVisibility(View.GONE);
////                    } else {
////                        pinTransContainer.setVisibility(View.VISIBLE);
////                        or_trans_TV.setVisibility(View.VISIBLE);
////                        fingerprint_trans_IV.setVisibility(View.VISIBLE);
////                    }
////                }
//
//            }
//
//            @Override
//            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
//                super.onAuthenticationSucceeded(result);
//
//                if(type.equals("withdraw")){
//                    sendWithdrawalMethod();  // for finger print
//                } else {
//                    sendTransferMethod();
//                }
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//                Toast.makeText(WalletActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Create a BiometricPrompt.PromptInfo instance
//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Biometric authentication")
//                .setSubtitle("If fingerprint is dirty, you can choose 'verify by PIN'.")
//                .setNegativeButtonText("Or\n Verify by PIN")
//                .build();
//
//        // Show the fingerprint authentication dialog
//        biometricPrompt.authenticate(promptInfo);
//    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {

            if(trans_background.getVisibility() == View.VISIBLE)
            {
                trans_background.setVisibility(View.GONE);
                withdrawOrDepositContainer.setVisibility(View.GONE);

            } else if (acknowledgeLayout.getVisibility() == View.VISIBLE)
            {
                acknowledgeLayout.setVisibility(View.GONE);
                enterPin_ET.setText(null);

            } else if (transferPageLayout.getVisibility() == View.VISIBLE)
            {
                transferPageLayout.setVisibility(View.GONE);
                transferAmount_ET.setText(null);
                enterPin_ET.setText(null);
                transferAmount_ET.removeTextChangedListener(listenToConvertAmount("transfer"));
//            errorInfo.setVisibility(View.GONE);
                spinner.setSelection(0); // Set the default selection to the first item in the array

            }  else if (usdtNetworkLayout.getVisibility() == View.VISIBLE)
            {
                usdtNetworkLayout.setVisibility(View.GONE);

            } else if (depositPageLayout.getVisibility() ==View.VISIBLE)
            {
                depositPageLayout.setVisibility(View.GONE);

            } else if (withdrawPageLayout.getVisibility() == View.VISIBLE)
            {
                trans_background.setVisibility(View.GONE);
                withdrawOrDepositContainer.setVisibility(View.GONE);
                withdrawPageLayout.setVisibility(View.GONE);
                usdtWithdrawAddress_ET.setText(null);
                withdrawAmount_ET.setText(null);
                enterPin_ET.setText(null);
                spinner.setSelection(0); // Set the default selection to the first item in the array

                withdrawAmount_ET.removeTextChangedListener(listenToConvertAmount("withdraw"));

            } else if (transferPayeeListLayout.getVisibility() == View.VISIBLE)
            {
                transferPayeeListLayout.setVisibility(View.GONE);
            } else {
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
                finish();
            }

        }
    };


//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == AllConstants.BIOMETRIC_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted
//                Toast.makeText(this, "Fingerprint verified!", Toast.LENGTH_SHORT).show();
//                showFingerPrint(userSelection);
//            } else {
//                // Permission denied
//                Toast.makeText(this, "Permission denied to use biometric authentication", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


    @Override
    public void openTransferPage(FundTransferUser fundTransferUser) {
        transferPageLayout.setVisibility(View.VISIBLE);
    }
}














