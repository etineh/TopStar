package com.pixel.chatapp.side_bar_menu.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.pixel.chatapp.R;
import com.pixel.chatapp.home.MainActivity;
import com.sp.shuftipro_sdk.listener.ShuftiVerifyListener;
import com.sp.shuftipro_sdk.models.Shuftipro;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class KycActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyc);


        JSONObject AuthKeys = new JSONObject();

        try{
            AuthKeys.put("auth_type","access_token");
            AuthKeys.put("access_token","sp-accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject Config=new JSONObject();

        try{
            Config.put("base_url",  "api.shuftipro.com");
            Config.put("consent_age",  16);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject requestObject = new JSONObject();

        try{
            requestObject.put("reference", "Unique-Reference");
            requestObject.put("country", "");
            requestObject.put("language", "");
            requestObject.put("email", "");
            requestObject.put("callback_url", "");
            requestObject.put("verification_mode", "image_only");
            requestObject.put("show_results", "1");
            requestObject.put("allow_retry", "0");
            requestObject.put("show_ocr_form", "0");

            //Creating Face object
            JSONObject faceObject = new JSONObject();
            faceObject.put("proof", "");

            requestObject.put("face", faceObject);

//Creating Document object
            JSONObject documentObject = new JSONObject();

            ArrayList<String> doc_supported_types = new ArrayList<String>();
            doc_supported_types.add("passport");
            doc_supported_types.add("id_card");
            doc_supported_types.add("driving_license");
            doc_supported_types.add("credit_or_debit_card");

            documentObject.put("proof", "");
            documentObject.put("additional_proof", "");

            JSONObject docNameObject = new JSONObject();
            docNameObject.put("first_name", "Johon");
            docNameObject.put("middle_name", "Johsan");
            docNameObject.put("last_name", "Livone");

            documentObject.put("name", docNameObject);
            documentObject.put("dob", "1980-11-12");
            documentObject.put("document_number", "19901112");
            documentObject.put("expiry_date", "1996-11-12");
            documentObject.put("issue_date", "1990-11-12");
            documentObject.put("backside_proof_required", "1");
            documentObject.put("supported_types",new JSONArray(doc_supported_types));

            requestObject.put("document", documentObject);


            Shuftipro shuftipro = Shuftipro.getInstance();
            shuftipro.shuftiproVerification(requestObject, AuthKeys, Config, KycActivity.this,
                    new ShuftiVerifyListener() {
                        @Override
                        public void verificationStatus(@NonNull Map<String, ?> responseSet) {

                            Log.e("Response",responseSet.toString());

                        }
                    });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




}











