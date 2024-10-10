package com.pixel.chatapp.view_controller.side_bar_menu.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.pixel.chatapp.R;
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

        ImageView arrowBackS = findViewById(R.id.arrowBackS);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        arrowBackS.setOnClickListener(v -> onBackPressed());

        Gson gson = new Gson();

        JSONObject AuthKeys = new JSONObject();

//        try{
//            AuthKeys.put("auth_type","access_token");
//            AuthKeys.put("access_token","sp-accessToken");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        try{
            AuthKeys.put("auth_type","basic_auth");
            AuthKeys.put("client_id","82eeb940d3c995e3582d80721ecd042bced8fb54741bd21bab611fc5d565217e");
            AuthKeys.put("secret_key","iR7XCWIqQfQ7PtNyA1xqRR7rbpwXkRgZ");
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
            requestObject.put("reference", "Unique-Reference1");
            requestObject.put("country", "");
            requestObject.put("language", "");
            requestObject.put("email", user.getEmail());
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
            docNameObject.put("first_name", "");
            docNameObject.put("middle_name", "");
            docNameObject.put("last_name", "");

            documentObject.put("name", docNameObject);
            documentObject.put("dob", "");
            documentObject.put("document_number", "");
            documentObject.put("expiry_date", "");
            documentObject.put("issue_date", "");
            documentObject.put("backside_proof_required", "1");
            documentObject.put("supported_types",new JSONArray(doc_supported_types));

            requestObject.put("document", documentObject);

//            SharedPreferences sharedPreferences = getSharedPreferences("KYC", Context.MODE_PRIVATE);

            Shuftipro shuftipro = Shuftipro.getInstance();
            shuftipro.shuftiproVerification(requestObject, AuthKeys, Config, KycActivity.this,
                    new ShuftiVerifyListener() {
                        @Override
                        public void verificationStatus(@NonNull Map<String, ?> responseSet) {

                            Log.e("Response", responseSet.get("verification_data").toString());

//                            sharedPreferences.edit().putString(user.getUid(), responseSet.get("verification_data").toString()).apply();

                            if(responseSet.get("event").equals("verification.accepted")){
                                // Verification accepted callback
                                Toast.makeText(KycActivity.this, "KYC verified!", Toast.LENGTH_SHORT).show();
                            }
                            else if(responseSet.get("event").equals("verification.declined")){
                                // Verification declined callback
                                Toast.makeText(KycActivity.this, "KYC failed", Toast.LENGTH_SHORT).show();
                            }

                            finish();

                            extractDetails(responseSet.toString());
                        }
                    });


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void extractDetails(String jsonStr){
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);

            // Extracting verification_data
            JSONObject verificationData = jsonObject.getJSONObject("verification_data");
            JSONObject document = verificationData.getJSONObject("document");
            JSONObject name = document.getJSONObject("name");

            String firstName = name.getString("first_name");
            String middleName = name.getString("middle_name");
            String lastName = name.getString("last_name");
            String dob = document.getString("dob");
            String country = document.getString("country");

            // Extracting info
            JSONObject info = jsonObject.getJSONObject("info");
            JSONObject geolocation = info.getJSONObject("geolocation");

            String countryName = geolocation.getString("country_name");
            String countryCode = geolocation.getString("country_code");
            String regionName = geolocation.getString("region_name");
            String city = geolocation.getString("city");
            String continentName = geolocation.getString("continent_name");
            String currency = geolocation.getString("currency");

            // Print the extracted information
            System.out.println("First Name: " + firstName);
            System.out.println("Middle Name: " + middleName);
            System.out.println("Last Name: " + lastName);
            System.out.println("Date of Birth: " + dob);
            System.out.println("Country: " + country);
            System.out.println("Country Name: " + countryName);
            System.out.println("Country Code: " + countryCode);
            System.out.println("Region Name: " + regionName);
            System.out.println("City: " + city);
            System.out.println("Continent Name: " + continentName);
            System.out.println("Currency: " + currency);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // on verify

//     {reference=Unique-Reference5, verification_data={document={name={first_name=GODSTIME  OGHENERUONA, middle_name=OGHENERUONA, last_name=ETINEMIDERHI}, dob=1996-03-15, issue_date=2024-04-08, document_number=628626055156286260, country=ng, selected_type=[id_card], supported_types=[passport, id_card, driving_license, credit_or_debit_card], face_match_confidence=72.0}}, proofs={document={proof=https://ns.shuftipro.com/api/pea/8a2493cb068fa4988dd63fc2cd06818b496ac4bb, additional_proof=https://ns.shuftipro.com/api/pea/c2776f1c959f820792a0777919be1e4604654013}, access_token=5e945b8518c764da083bd2bf8aee88044041bf747ce11d81697d1b0952c2abd1, face={proof=https://ns.shuftipro.com/api/pea/1167f420b91bfb5fcd5a011c20f3af008f30eb97}, verification_report=https://ns.shuftipro.com/api/pea/7aea188d98aef78ea298c7842c2fb1b7fd0025a8}, verification_result={face=1.0, document={document=1.0, document_visibility=1.0, document_must_not_be_expired=1.0, document_proof=1.0, selected_type=1.0, face_on_document_matched=1.0, name=1.0, dob=1.0, document_number=1.0, issue_date=1.0}}, event=verification.accepted, info={agent={is_desktop=true, is_phone=false, device_name=, browser_name= , platform_name= }, geolocation={host=, ip=197.210.55.88, rdns=197.210.55.88, asn=29465, isp=Mtn Nigeria Communication Limited, country_name=Nigeria, country_code=NG, region_name=Lagos, region_code=LA, city=Lagos, postal_code=100002, continent_name=Africa, continent_code=AF, latitude=6.4351301193237, longitude=3.4160599708557, metro_code=, timezone=Africa/Lagos, ip_type=ipv4, capital=Abuja, currency=NGN}}}

    // {reference=Unique-Reference, services_declined_codes={document=[SPDR75]},
// verification_data={document={name={first_name=Johon, middle_name=Johsan, last_name=Livone},
// dob=1980-11-12, expiry_date=1996-11-12, issue_date=1990-11-12,
// document_number=19901112, country=ng, selected_type=[id_card],
// supported_types=[passport, id_card, driving_license, credit_or_debit_card], face_match_confidence=73.0}},
// proofs={document={proof=https://ns.shuftipro.com/api/pea/17f2695da7a8a228c665f3506a2dbe45bacb6c92,
// additional_proof=https://ns.shuftipro.com/api/pea/eae1506124b1079c08ac2d273bff5f378ffd7ca5},
// access_token=33ffc5265414337b63e39bde68476799fd0bac38d063c4285c878d951618af87,
// face={proof=https://ns.shuftipro.com/api/pea/9fb094089b3697a050413a18df35e5faea944e99},
// verification_report=https://ns.shuftipro.com/api/pea/a68aeae59e1a82166e7e2b6cef52da0470bb4813},
// declined_codes=[SPDR75], verification_result={face=1.0,
// document={document=1.0, document_visibility=1.0, document_must_not_be_expired=1.0,
// document_proof=1.0, selected_type=1.0, face_on_document_matched=1.0, name=0.0}},
// event=verification.declined, info={agent={is_desktop=true, is_phone=false, device_name=, browser_name= , platform_name= },
// geolocation={host=, ip=197.210.84.109, rdns=197.210.84.109, asn=29465, isp=Mtn Nigeria Communication Limited,
// country_name=Nigeria, country_code=NG, region_name=Lagos, region_code=LA, city=Lagos, postal_code=100002,
// continent_name=Africa, continent_code=AF, latitude=6.4351301193237, longitude=3.4160599708557, metro_code=,
// timezone=Africa/Lagos, ip_type=ipv4, capital=Abuja, currency=NGN}},
// declined_reason=Name on the document does not match with the provided one}

//  {reference=Unique-Reference, country=, event=request.invalid,
//  error={service=, key=reference, message=The reference has already been taken}, email=null}
}











