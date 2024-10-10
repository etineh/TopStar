package com.pixel.chatapp.view_controller.side_bar_menu.wallet;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pixel.chatapp.R;

public class ConvertFundActivity extends AppCompatActivity {

    //  convert currency page
    private EditText enterConvertAmount_ET;
    private ImageView switchBetweenAssets, cancelConvert;
    private TextView convertAssetButton, assetWillReceive_TV, available_balance_TV;
    private Spinner chooseAssetSpinner, selectToAssetSpinner;
    //    private String getSelectedAssetBalance = getString(R.string.convertForLocalAsset);
    private String selectedAsset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert_fund);


        //  convert currency page
        enterConvertAmount_ET = findViewById(R.id.enterConvertAmount_ET);
        cancelConvert = findViewById(R.id.cancleConvertButton);
        switchBetweenAssets = findViewById(R.id.switchBetweenAssets);
        convertAssetButton = findViewById(R.id.convertButton);
        available_balance_TV = findViewById(R.id.available_balance);
        assetWillReceive_TV = findViewById(R.id.assetWillReceive_TV);
        chooseAssetSpinner = findViewById(R.id.chooseAssetSpinner);
        chooseAssetSpinner.setSelection(0); // Set the default selection to the first item in the array
        selectToAssetSpinner = findViewById(R.id.selectToAssetSpinner);
        selectToAssetSpinner.setSelection(1); // Set the default selection to the first item in the array

        getOnBackPressedDispatcher().addCallback(this, callback);

        selectedAsset = getString(R.string.convertForUSDTAsset);

        enterConvertAmount_ET.addTextChangedListener(listenToConvertAmount());

        convertSpinner();
        new Handler().postDelayed(()-> enterConvertAmount_ET.requestFocus(), 500);

        switchBetweenAssets.setOnClickListener(v ->
        {
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
            Toast.makeText(ConvertFundActivity.this, getString(R.string.assetSwitch), Toast.LENGTH_SHORT).show();

        });

        convertAssetButton.setOnClickListener(v ->
        {
            Toast.makeText(this, getString(R.string.convertSuccessfully), Toast.LENGTH_SHORT).show();
        });

        cancelConvert.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


    }

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

    private TextWatcher listenToConvertAmount(){

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
                String assetConvert = selectedAsset + " " + s;
                assetWillReceive_TV.setText(assetConvert);

            }
        };

        return textWatcher;
    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            enterConvertAmount_ET.removeTextChangedListener(listenToConvertAmount());
            finish();
        }
    };


}





