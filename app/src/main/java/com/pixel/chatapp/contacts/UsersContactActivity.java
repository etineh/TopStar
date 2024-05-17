package com.pixel.chatapp.contacts;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ContactAdapter;
import com.pixel.chatapp.all_utils.CountryNumCodeUtils;
import com.pixel.chatapp.all_utils.PhoneUtils;
import com.pixel.chatapp.all_utils.SharePhotoUtil;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.model.ContactModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersContactActivity extends AppCompatActivity implements ContactAdapter.BackButtonClickListener, FetchContacts.RefreshContactListener {

    RecyclerView recyclerView;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference refUsers;

    List<ContactModel> list;
//    List<String> names;
    ContactAdapter adapter;

    ImageView imageViewBack, addContact_IV, refresh_IV;
    ConstraintLayout inviteFriends;

    MainActivity mainActivity = new MainActivity();
    ProgressBar progressBar, progressBarRefresh;
    private FragmentListener fragmentListener; // A field to store the listener reference

    CardView searchContactContainer;
    EditText searchContact_ET;
    TextWatcher textWatcher;
    private ContactSearch contactSearch;

    boolean refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_contact);

        addContact_IV = findViewById(R.id.addContact_IV);
        refresh_IV = findViewById(R.id.refresh__IV);
        inviteFriends = findViewById(R.id.inviteFrdConst);
        imageViewBack = findViewById(R.id.imageViewBack);
        recyclerView = findViewById(R.id.recyclerViewContacts);
        progressBar = findViewById(R.id.progressBar7);
        searchContactContainer = findViewById(R.id.searchContactContainer);
        searchContact_ET = findViewById(R.id.searchContact_ET);
        ImageView searchContact_IV = findViewById(R.id.searchContact_IV);
        progressBarRefresh = findViewById(R.id.progressBarRefresh);
//        searchContact_IV = findViewById(R.id.searchContact_IV);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();

        searchContactListener();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        refUsers = FirebaseDatabase.getInstance().getReference("Users");

        imageViewBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        fragmentListener = mainActivity.getMainActivityContext();

        if(MainActivity.contactListFile.size() == 0){
            adapter = new ContactAdapter(MainActivity.contactList, UsersContactActivity.this);
            contactSearch = new ContactSearch(MainActivity.contactList);
            System.out.println("what is contactList");
        } else {
            adapter = new ContactAdapter(MainActivity.contactListFile, UsersContactActivity.this);
            contactSearch = new ContactSearch(MainActivity.contactListFile);
            System.out.println("what is contactFile " + MainActivity.contactListFile.size());
        }

        //  initialise interface
        adapter.setListener(fragmentListener);
        adapter.setBackButtonClickListener(UsersContactActivity.this); // "this" refers to the UserContactActivity
        FetchContacts.refreshContactListener = this;

        new Handler().postDelayed(()-> {
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        },50);
//        recyclerView.setAdapter(adapter);

//        getUsers();

//        new Thread( () -> {
//            readContacts();
//
//        }).start();

        inviteFriends.setOnClickListener(v ->
        {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                SharePhotoUtil.shareDrawableImage(this, R.drawable.logo, getString(R.string.appInvite) );
            });

            new Handler().postDelayed(() -> {
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            }, 1000);
        });

        addContact_IV.setOnClickListener(v ->
        {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
            {
                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);
            });

            new Handler().postDelayed(() -> {
                v.setScaleX(1.0f);
                v.setScaleY(1.0f);
            }, 1000);
        });

        searchContact_IV.setOnClickListener(v ->
        {
           searchContactContainer.setVisibility(View.VISIBLE);
           searchContact_IV.requestFocus();
           PhoneUtils.showKeyboard(this, searchContact_ET);
        });

        refresh_IV.setOnClickListener(v ->
        {
           refreshContact();
        });

        getOnBackPressedDispatcher().addCallback(callback);


    }


    // -----------  methods -----------

    private void refreshContact(){
        progressBarRefresh.setVisibility(View.VISIBLE);
        refresh_IV.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(()-> new Thread(()-> FetchContacts.readContacts(this) ).start(), 20);
    }

    private void searchContactListener()
    {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update RecyclerView as user types
                if (count > 0){
                    new Thread(()-> {
                        String query = s.toString();
                        contactSearch.setSearchQuery(query);
                        // Update RecyclerView as user types
                        try {
                            List<ContactModel> filteredContacts = contactSearch.searchContacts();
                            adapter.updateDataSet(filteredContacts);
                        } catch (Exception e){
                            System.out.println("what is error UserContactAct L200 " + e.getMessage());
                        }

                        runOnUiThread(()-> recyclerView.scrollToPosition(0));
                    }).start();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        };

        searchContact_ET.addTextChangedListener(textWatcher);
    }


    private void readContacts() {

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getNetworkCountryIso().toUpperCase();

        // Use a Map to store unique contacts (key: phone number, value: ContactModel)
        Map<String, ContactModel> contactMap = new HashMap<>();

        // Query contacts
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            do {
                // Check if the column indices are valid
                if (nameIndex != -1 && phoneNumberIndex != -1) {

                    String name = cursor.getString(nameIndex);
                    String phoneNumber = cursor.getString(phoneNumberIndex).replaceAll("\\s+", "");
                    if(phoneNumber.startsWith("0")){
                        // remove the 0 and add the country code
                        phoneNumber = CountryNumCodeUtils.getCountryDialingCode(countryCode, phoneNumberUtil) + phoneNumber.substring(1);
                    }

                    // Check if the phone number already exists in the map
                    if (!contactMap.containsKey(phoneNumber)) {
                        // Create a new ContactModel and add it to the map
                        ContactModel contact = new ContactModel(null, null, name, null, phoneNumber, null, name, phoneNumber);
                        contactMap.put(phoneNumber, contact);
                    }

                } else {
                    // Handle the case when the column indices are not found
                    Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());

            cursor.close(); // Close the cursor when done

            // Convert the Map values to a List
            List<ContactModel> list_ = new ArrayList<>(contactMap.values());

            // Sort the list of ContactModel objects alphabetically by name
            Collections.sort(list_, (contact1, contact2) -> contact1.getContactName().compareToIgnoreCase(contact2.getContactName()));

            // Set up RecyclerView
            runOnUiThread(()->{

                adapter = new ContactAdapter(list_, UsersContactActivity.this);
                adapter.setListener(fragmentListener);
                adapter.setBackButtonClickListener(UsersContactActivity.this); // "this" refers to the UserContactActivity
                recyclerView.setAdapter(adapter);
            });


        } else {
            // Handle the case when the cursor is empty
            Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show();
        }
    }

    public void getUsers(){

        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                // get my username
                String myUserName = snapshot.child(user.getUid()).child("general").child("userName").getValue().toString();

                for (DataSnapshot userDetails : snapshot.getChildren()) {

                    // to get the uid all my contact user child
                    String key = userDetails.getKey();

                    if (!key.equals(user.getUid())){// if the key id is not mine, then it should fetch out other user id
                        ContactModel contactModel = userDetails.child("general").getValue(ContactModel.class);
                        contactModel.setOtherUid(key);
                        contactModel.setMyUserName(myUserName);
                        list.add(contactModel);
                    }

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {


            if(searchContactContainer.getVisibility() == View.VISIBLE)
            {
                searchContactContainer.setVisibility(View.GONE);
                PhoneUtils.hideKeyboard(UsersContactActivity.this, searchContact_ET);
                try {
                    adapter.resetContactList();    // onbackPress
                } catch (Exception e){
                    System.out.println("what is error UserContactAct L350 " + e.getMessage());
                }
                searchContact_ET.setText(null);

            } else {
                searchContact_ET.removeTextChangedListener(textWatcher);
                onBackButtonClicked();
                finish();
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if(refresh) refreshContact();
    }

    @Override
    protected void onPause() {
        super.onPause();
        refresh = true;
    }

    //  ----------- interface   ------------
    @Override
    public void onBackButtonClicked() {
        // Close the activity
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(mainActivityIntent);
        finish();
    }

    @Override
    public void onSuccess() {

        try {
            adapter.resetContactList();  // onSuccess InterfaceListener
        } catch (Exception e){
            System.out.println("what is UserContactAct L370 " + e.getMessage());
        }

        runOnUiThread(()-> {
            refresh_IV.setVisibility(View.VISIBLE);
            progressBarRefresh.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.contactUpdated), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onFailure() {
        progressBarRefresh.setVisibility(View.GONE);
        refresh_IV.setVisibility(View.VISIBLE);
        Toast.makeText(this, getString(R.string.isNetwork), Toast.LENGTH_SHORT).show();
    }


}






