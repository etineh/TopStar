package com.pixel.chatapp.side_bar_menu.wallet;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.pixel.chatapp.CaptureAct;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.FundTransferUserAdapter;
import com.pixel.chatapp.all_utils.BarcodeUtils;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.model.FundTransferUser;

import java.util.ArrayList;
import java.util.List;

public class WalletActivity extends AppCompatActivity implements FundTransferUserAdapter.ProceedToTransferPage {

    private ImageView resetPinButton, customerCareButton, historyButton, arrowBack;
    private TextView totalAssetAmount_TV, localCurrencyAmount_TV,dollarAmount_TV, gameAmount_TV,bonusAmount_TV;
    private CardView depositCardView,withdrawCardView, transferCardView, convertCardView, p2pCardView;
    private CardView cardViewLocalAmount, cardViewUDSTAmount, cardViewGameAmount, cardViewBonusAmount;

    private ConstraintLayout usdtPaymentOption, p2pPaymentOption, withdrawOrDepositContainer, trans_background;
    private TextView withdrawOrDeposit_TV, p2pInfo_TV, usdtInfo_TV, gameCurrency_TV;

    //  ----------  transfer var
    private ConstraintLayout transferLayout;
    private ImageView closeTransferArrow, searchButton, cancleTransferButton;
    private RadioButton localCurrencyRadioButton, usdtRadioButton;
    private EditText searchUser_ET, enterTransferAmount_ET, enterPin_ET;
    @SuppressLint("StaticFieldLeak")
    private static ScrollView transferAmountLayout;
    private TextView getTransferConvertAmount_TV, sendTransferButton, errorInfo;

    // transfer successful page
    private ScrollView transferSuccessLayout;
    private TextView amount_TV, date_TV, time_TV, receiver_TV, sender_TV, back, goHistory, trxId, trxType;
    private ImageView copyId;

    //  withdraw variables
    private ScrollView withdrawPageLayout;
    private ImageView cancleWithdraw, scanAddress;
    private EditText usdtWithdrawAddress_ET, withdrawAmount_ET, withdrawPin_ET;
    private Spinner spinner;
    private TextView sendWithrawal, withdrawConvertAmount_TV;
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

    //  convert currency page
    private ScrollView convertPageLayout;
    private EditText enterConvertAmount_ET;
    private ImageView switchBetweenAssets, cancelConvert;
    private TextView convertAssetButton, assetWillReceive_TV, available_balance_TV;
    private Spinner chooseAssetSpinner, selectToAssetSpinner;
    //    private String getSelectedAssetBalance = getString(R.string.convertForLocalAsset);
    private String selectedAsset;

    //  transfer recycler adapter
    private RecyclerView recyclerViewTransfer;
    private FundTransferUserAdapter fundTransferUserAdapter;

    private List<FundTransferUser> userList;

    private String userSelection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        resetPinButton = findViewById(R.id.resetPin_IV);
        customerCareButton = findViewById(R.id.support_IV);
        resetPinButton = findViewById(R.id.history_IV);
        arrowBack = findViewById(R.id.arrowBack_IV);
        totalAssetAmount_TV = findViewById(R.id.totalAssetAmount_TV);
        localCurrencyAmount_TV = findViewById(R.id.localCurrencyAmount_TV);
        dollarAmount_TV = findViewById(R.id.dollarAmount_TV);
        gameAmount_TV = findViewById(R.id.gameAmount_TV);
        bonusAmount_TV = findViewById(R.id.bonusAmount_TV);

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

        // transfer ids
        transferLayout = findViewById(R.id.transferLayout);
        closeTransferArrow = transferLayout.findViewById(R.id.arrowCloseButton);
        searchButton = transferLayout.findViewById(R.id.searchButton);
        searchUser_ET = transferLayout.findViewById(R.id.seachUser_ET);
        // transfer page (amount and pin)
        transferAmountLayout = findViewById(R.id.transferPageLayout);
        cancleTransferButton = transferAmountLayout.findViewById(R.id.cancleTransferButton);
        localCurrencyRadioButton = transferAmountLayout.findViewById(R.id.localCurrencyRadioButton);
        usdtRadioButton = transferAmountLayout.findViewById(R.id.usdtRadioButton);
        enterTransferAmount_ET = transferAmountLayout.findViewById(R.id.enterAmount_ET);
        enterPin_ET = transferAmountLayout.findViewById(R.id.enterPin_ET);
        getTransferConvertAmount_TV = transferAmountLayout.findViewById(R.id.getTransferConvertAmount_TV);
        sendTransferButton = transferAmountLayout.findViewById(R.id.sendButton);
        errorInfo = transferAmountLayout.findViewById(R.id.errorInfo);
        RadioGroup radioGroupTransfer = transferAmountLayout.findViewById(R.id.radioGroup);
//        RadioButton localRadio = transferLayout.findViewById(R.id.localCurrencyRadioButton);
        radioGroupTransfer.check(R.id.localCurrencyRadioButton); // Set the default selection to the localCurrencyRadioButton

        // transfer successful ids
        transferSuccessLayout = findViewById(R.id.transferSuccessfulPage);
        amount_TV = transferSuccessLayout.findViewById(R.id.amount_TV);
        date_TV = transferSuccessLayout.findViewById(R.id.date_TV);
        time_TV = transferSuccessLayout.findViewById(R.id.time_TV);
        receiver_TV = transferSuccessLayout.findViewById(R.id.receiver_TV);
        sender_TV = transferSuccessLayout.findViewById(R.id.sender_TV);
        back = transferSuccessLayout.findViewById(R.id.back);
        goHistory = transferSuccessLayout.findViewById(R.id.historySuccess);
        copyId = transferSuccessLayout.findViewById(R.id.copyId);
        trxId = transferSuccessLayout.findViewById(R.id.transactionID_TV);
        trxType = transferSuccessLayout.findViewById(R.id.transactionType_TV);

        // withdraw ids
        withdrawPageLayout = findViewById(R.id.withdrawPageLayout);
        cancleWithdraw = withdrawPageLayout.findViewById(R.id.cancleTransferButton);
        usdtWithdrawAddress_ET = withdrawPageLayout.findViewById(R.id.usdtAddress_ET);
        withdrawAmount_ET = withdrawPageLayout.findViewById(R.id.enterAmount_ET);
        withdrawPin_ET = withdrawPageLayout.findViewById(R.id.enterPin_ET);
        sendWithrawal = withdrawPageLayout.findViewById(R.id.sendButton);
        withdrawConvertAmount_TV = withdrawPageLayout.findViewById(R.id.getTransferConvertAmount_TV);
//        cancleWithdraw = withdrawPageLayout.findViewById(R.id.cancleTransferButton);
        scanAddress = withdrawPageLayout.findViewById(R.id.scanAddress);
        spinner = withdrawPageLayout.findViewById(R.id.chooseNetwork_TV);
        spinner.setSelection(0); // Set the default selection to the first item in the array
        radioGroupWithdraw = withdrawPageLayout.findViewById(R.id.radioGroup);
        radioGroupWithdraw.check(R.id.usdtRadioButton); // Set the default selection to the localCurrencyRadioButton

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

        //  convert currency page
        convertPageLayout = findViewById(R.id.convertPageLayout);
        enterConvertAmount_ET = convertPageLayout.findViewById(R.id.enterConvertAmount_ET);
        cancelConvert = convertPageLayout.findViewById(R.id.cancleConvertButton);
        switchBetweenAssets = convertPageLayout.findViewById(R.id.switchBetweenAssets);
        convertAssetButton = convertPageLayout.findViewById(R.id.convertButton);
        available_balance_TV = convertPageLayout.findViewById(R.id.available_balance);
        assetWillReceive_TV = convertPageLayout.findViewById(R.id.assetWillReceive_TV);
        chooseAssetSpinner = convertPageLayout.findViewById(R.id.chooseAssetSpinner);
        chooseAssetSpinner.setSelection(0); // Set the default selection to the first item in the array
        selectToAssetSpinner = convertPageLayout.findViewById(R.id.selectToAssetSpinner);
        selectToAssetSpinner.setSelection(1); // Set the default selection to the first item in the array

        //  transfer recyclerView
        recyclerViewTransfer = transferLayout.findViewById(R.id.recyclerViewTransfer);
        recyclerViewTransfer.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();

        selectedAsset = getString(R.string.convertForUSDTAsset);

        // withdraw or deposit via selection method   ====================
        p2pPaymentOption.setOnClickListener(v -> {
            trans_background.setVisibility(View.GONE);
            withdrawOrDepositContainer.setVisibility(View.GONE);

            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
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
                ClipboardManager clipboard =  (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", setUsdtAddress_TV.getText());

                if (clipboard == null || clip == null) return;
                clipboard.setPrimaryClip(clip);

                new Handler().postDelayed( ()-> {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                }, 1000);

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

        // send the withdrawal
        sendWithrawal.setOnClickListener(v -> {

            withdrawPageLayout.setVisibility(View.GONE);
            transferSuccessLayout.setVisibility(View.VISIBLE);

            spinner.setSelection(0); // Set the default selection to the first item in the array
            withdrawAmount_ET.removeTextChangedListener(listenToConvertAmount("withdraw"));
            withdrawPin_ET.setText(null);
            withdrawAmount_ET.setText(null);

            PhoneUtils.hideKeyboard(this, withdrawAmount_ET);
            Toast.makeText(this, "successfully sent", Toast.LENGTH_SHORT).show();
        });

        // ============ transfer funds to user in topstar    -------------------------------------
        transferCardView.setOnClickListener(v -> {

            userList.clear();
            userSelection = "transfer";

            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(20).withEndAction(() ->
            {
                transferLayout.setVisibility(View.VISIBLE);
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

            enterTransferAmount_ET.addTextChangedListener(listenToConvertAmount("transfer"));

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
            transferLayout.setVisibility(View.GONE);

        });
//
//        localCurrencyRadioButton.setOnClickListener(v -> {
//
//            Toast.makeText(this, "in progress" + localCurrencyRadioButton.getText(), Toast.LENGTH_SHORT).show();
//        });
//
//        usdtRadioButton.setOnClickListener(v -> {
//            Toast.makeText(this, "in progress" + usdtRadioButton.getText(), Toast.LENGTH_SHORT).show();
//        });

        sendTransferButton.setOnClickListener(v -> {
            if(enterPin_ET.length() == 0){
                errorInfo.setVisibility(View.VISIBLE);
                errorInfo.setText(getString(R.string.incorrectPin));
            } else {
                enterPin_ET.setText(null);
                enterTransferAmount_ET.setText(null);
                transferAmountLayout.setVisibility(View.GONE);
                transferLayout.setVisibility(View.GONE);
                transferSuccessLayout.setVisibility(View.VISIBLE);

                PhoneUtils.hideKeyboard(this, enterTransferAmount_ET);
                Toast.makeText(this, "successfully sent!", Toast.LENGTH_SHORT).show();
            }
        });

        goHistory.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        copyId.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });


        //  ========    convert between currency    ================================================
        convertCardView.setOnClickListener(v -> {
            convertPageLayout.setVisibility(View.VISIBLE);

            enterConvertAmount_ET.addTextChangedListener(listenToConvertAmount("convert"));

            // activate both listener;
            convertSpinner();

            new Handler().postDelayed(()-> enterConvertAmount_ET.requestFocus(), 500);
        });

        switchBetweenAssets.setOnClickListener(v -> {

            int getChooseAssetSpinner = chooseAssetSpinner.getSelectedItemPosition();
            int getSelectToAssetSpinner = selectToAssetSpinner.getSelectedItemPosition();
            chooseAssetSpinner.setSelection(getSelectToAssetSpinner);
            selectToAssetSpinner.setSelection(getChooseAssetSpinner);

            if(selectToAssetSpinner.getSelectedItemPosition() == 0){
                selectedAsset = getString(R.string.convertForLocalAsset);
            } else if (selectToAssetSpinner.getSelectedItemPosition() == 1) {
                selectedAsset = getString(R.string.convertForUSDTAsset);
            } else if (selectToAssetSpinner.getSelectedItemPosition() == 2) {
                selectedAsset = getString(R.string.convertForGameAsset);
            } else if (selectToAssetSpinner.getSelectedItemPosition() == 3) {
                selectedAsset = getString(R.string.convertForBonus);
            }

            enterConvertAmount_ET.setText("");
            assetWillReceive_TV.setText(selectedAsset);
            Toast.makeText(WalletActivity.this, getString(R.string.assetSwitch), Toast.LENGTH_SHORT).show();

        });

        convertAssetButton.setOnClickListener(v -> {
            // hide keyboard
            PhoneUtils.hideKeyboard(this, enterConvertAmount_ET);
            convertPageLayout.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.convertSuccessfully), Toast.LENGTH_SHORT).show();
        });

        //  ========    peer to peer p2p    ================================================
        p2pCardView.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        //  ========    open various assets    ================================================
        cardViewLocalAmount.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        cardViewUDSTAmount.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        cardViewGameAmount.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        // change the game currency
        gameCurrency_TV.setOnClickListener(v -> {

            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        cardViewBonusAmount.setOnClickListener(v -> {
            Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
        });

        // onBackPress
        View.OnClickListener backPress = v -> {
            onBackPressed();
        };
        arrowBack.setOnClickListener(backPress);
        back.setOnClickListener(backPress);
        cancleTransferButton.setOnClickListener(backPress);
        arrowBack.setOnClickListener(backPress);
        cancleWithdraw.setOnClickListener(backPress);
        closeUsdtNetwork.setOnClickListener(backPress);
        closeDeposit_IV.setOnClickListener(backPress);
        cancelConvert.setOnClickListener(backPress);

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
    private void convertSpinner() {
        selectToAssetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = parent.getItemAtPosition(position).toString();
                if(selectedItem.equals("Local Asset")){
                    selectedAsset = getString(R.string.convertForLocalAsset);
                } else if (selectedItem.equals("USDT Asset")) {
                    selectedAsset = getString(R.string.convertForUSDTAsset);
                } else if (selectedItem.equals("Game Rewards")) {
                    selectedAsset = getString(R.string.convertForGameAsset);
                } else if (selectedItem.equals("Bonus/Giveaway")) {
                    selectedAsset = getString(R.string.convertForBonus);
                }
                enterConvertAmount_ET.setText("");
                assetWillReceive_TV.setText(selectedAsset);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });

        chooseAssetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                enterConvertAmount_ET.setText("");

                //  get the available balance later

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where nothing is selected (optional)
            }
        });
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

                } else if (type.equals("convert"))
                {
                    String assetConvert = selectedAsset + " " + s;
                    assetWillReceive_TV.setText(assetConvert);
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

    @Override
    public void onBackPressed() {
        if(trans_background.getVisibility() == View.VISIBLE){
            trans_background.setVisibility(View.GONE);
            withdrawOrDepositContainer.setVisibility(View.GONE);

        } else if (transferSuccessLayout.getVisibility() == View.VISIBLE) {
            transferAmountLayout.setVisibility(View.GONE);
            transferLayout.setVisibility(View.GONE);
            transferSuccessLayout.setVisibility(View.GONE);
            enterTransferAmount_ET.removeTextChangedListener(listenToConvertAmount("transfer"));
            spinner.setSelection(0); // Set the default selection to the first item in the array

        } else if (transferAmountLayout.getVisibility() == View.VISIBLE) {
            transferAmountLayout.setVisibility(View.GONE);
            enterTransferAmount_ET.setText(null);
            enterPin_ET.setText(null);
            errorInfo.setVisibility(View.GONE);
            enterTransferAmount_ET.removeTextChangedListener(listenToConvertAmount("transfer"));
            spinner.setSelection(0); // Set the default selection to the first item in the array

        }  else if (usdtNetworkLayout.getVisibility() == View.VISIBLE) {
            usdtNetworkLayout.setVisibility(View.GONE);

        } else if (depositPageLayout.getVisibility() ==View.VISIBLE) {
            depositPageLayout.setVisibility(View.GONE);

        } else if (withdrawPageLayout.getVisibility() == View.VISIBLE) {
            trans_background.setVisibility(View.GONE);
            withdrawOrDepositContainer.setVisibility(View.GONE);
            withdrawPageLayout.setVisibility(View.GONE);
            usdtWithdrawAddress_ET.setText(null);
            withdrawAmount_ET.setText(null);
            withdrawPin_ET.setText(null);
            spinner.setSelection(0); // Set the default selection to the first item in the array

            withdrawAmount_ET.removeTextChangedListener(listenToConvertAmount("withdraw"));

        } else if (convertPageLayout.getVisibility() == View.VISIBLE)
        {
            convertPageLayout.setVisibility(View.GONE);
            enterConvertAmount_ET.removeTextChangedListener(listenToConvertAmount("convert"));
            enterConvertAmount_ET.setText(null);

        } else if (transferLayout.getVisibility() == View.VISIBLE) {
            transferLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
            finish();
        }
    }


    @Override
    public void openTransferPage(FundTransferUser fundTransferUser) {
        transferAmountLayout.setVisibility(View.VISIBLE);
    }
}














