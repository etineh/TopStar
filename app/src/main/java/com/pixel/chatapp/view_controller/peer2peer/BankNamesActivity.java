package com.pixel.chatapp.view_controller.peer2peer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixel.chatapp.R;

public class BankNamesActivity extends AppCompatActivity {

    CheckBox checkBoxAll;
    TextView selectedNo_TV;
    RecyclerView recyclerView;
    ImageView done_IV, cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_names);

        cancel = findViewById(R.id.arrowBackS);
        selectedNo_TV = findViewById(R.id.selectedNo_TV);
        recyclerView = findViewById(R.id.recyclerView);
        done_IV = findViewById(R.id.done_IV);
        checkBoxAll = findViewById(R.id.checkBoxAll);
//        checkBoxAll = findViewById(R.id.checkBoxAll);


        cancel.setOnClickListener(v -> onBackPressed());


    }




}