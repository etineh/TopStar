package com.pixel.chatapp.view_controller;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.R;
import com.pixel.chatapp.adapters.ContactAdapter;
import com.pixel.chatapp.utilities.ContactUtils;
import com.pixel.chatapp.utilities.PhoneUtils;
import com.pixel.chatapp.utilities.SearchUtils;
import com.pixel.chatapp.utilities.Photo_Video_Utils;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.dataModel.ContactModel;

import java.util.ArrayList;
import java.util.List;

public class UsersContactActivity extends AppCompatActivity implements ContactAdapter.BackButtonClickListener, ContactUtils.RefreshContactListener {

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

    CardView searchContactContainer;
    EditText searchContact_ET;
    TextWatcher textWatcher;
    private SearchUtils searchUtils;

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

        // A field to store the listener reference
        FragmentListener fragmentListener = mainActivity.getMainActivityContext();

        if(ContactUtils.contactListFile.size() == 0){  // when local file contact is empty - no contact save yet
            adapter = new ContactAdapter(ContactUtils.contactList, UsersContactActivity.this);
            searchUtils = new SearchUtils(ContactUtils.contactList);
            System.out.println("what is contactList");
        } else {
            adapter = new ContactAdapter(ContactUtils.contactListFile, UsersContactActivity.this);
            searchUtils = new SearchUtils(ContactUtils.contactListFile);
            System.out.println("what is contactFile " + ContactUtils.contactListFile.size());
        }

        //  initialise interface
        adapter.setListener(fragmentListener);
        adapter.setBackButtonClickListener(UsersContactActivity.this); // "this" refers to the UserContactActivity
        ContactUtils.refreshContactListener = this;

        new Handler().postDelayed(()-> {
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
        },50);

        inviteFriends.setOnClickListener(v ->
        {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(20).withEndAction(() ->
                    Photo_Video_Utils.shareDrawableImage(this, R.drawable.logo_new_name, getString(R.string.appInvite) ));

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

        refresh_IV.setOnClickListener(v -> refreshContact());

        getOnBackPressedDispatcher().addCallback(callback);


    }


    // -----------  methods -----------

    private void refreshContact(){
        progressBarRefresh.setVisibility(View.VISIBLE);
        refresh_IV.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(()-> new Thread(()-> ContactUtils.readContacts(this) ).start(), 20);
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
                        searchUtils.setSearchQuery(query);
                        // Update RecyclerView as user types
                        try {
                            List<ContactModel> filteredContacts = searchUtils.searchContacts();
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


    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {


            if(searchContactContainer.getVisibility() == View.VISIBLE)
            {
                searchContactContainer.setVisibility(View.GONE);
                PhoneUtils.hideKeyboard(UsersContactActivity.this, searchContact_ET);
                try {
                    adapter.resetContactList();    // onBackPress
                } catch (Exception e){
                    System.out.println("what is error UserContactAct L350 " + e.getMessage());
                }
                searchContact_ET.setText(null);

            } else {
                onBackButtonClicked();
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
        if(textWatcher != null) searchContact_ET.removeTextChangedListener(textWatcher);
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






