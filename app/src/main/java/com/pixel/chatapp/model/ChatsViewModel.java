package com.pixel.chatapp.model;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixel.chatapp.chats.MessageModel;
import com.pixel.chatapp.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatsViewModel extends ViewModel {

    private DatabaseReference refMessages;
    private FirebaseUser user;
    private MutableLiveData<List<MessageModel>> chatsLiveData;

    public LiveData<List<MessageModel>> getItemsLiveData(String userName, List<String> otherNameList, String otherUID) {


        // Initialize Firebase references
        user = FirebaseAuth.getInstance().getCurrentUser();
        refMessages = FirebaseDatabase.getInstance().getReference("Messages");

//        if (chatsLiveData == null) {
            chatsLiveData = new MutableLiveData<>();

//        }
        for (String name :
                otherNameList) {
            loadItems(userName, name, otherUID); // Load initial data
            System.out.println("What is the name2 " + name);

        }
        return chatsLiveData;
    }

    private void loadItems(String userName, String otherName, String otherUID) {
        // Simulate loading data from a data source (e.g., a network request or a database)


        List<MessageModel> allMsgList = new ArrayList<>();

        refMessages.child(userName).child(otherName).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

//            if(MainActivity.readDatabase == 0) {   // run only once
//            List<MessageModel> allMsgList = new ArrayList<>();

                allMsgList.clear();

                for (DataSnapshot snapshotOld : snapshot.getChildren()){

                    if(snapshotOld.child("from").exists()){
                        MessageModel messageModelOldMsg = snapshotOld.getValue(MessageModel.class);
                        messageModelOldMsg.setIdKey(snapshotOld.getKey());  // set msg keys to the adaptor
                        allMsgList.add(messageModelOldMsg);

//                        chatsLiveData.setValue(allMsgList);

                        // set old message to read status
//                        if(msgListNotRead.size() < 1){
//                            messageModelOldMsg.setMsgStatus(700016);    // change later to loop through only 2000 msg
//                        } else {
//                            // loop through only 2000 message for efficiency, and set readStatus
//                            int changeOnly = allMsgList.size()-msgListNotRead.size();
//                            int startCount = allMsgList.size() > 2000 ? allMsgList.size() - 2000: 0;
//
//                            for (int i = startCount; i < changeOnly; i++) {
//                                MessageModel msgStatus = allMsgList.get(i);
//                                msgStatus.setMsgStatus(700016);
//                            }
//                        }

                    } else {
                        refMessages.child(userName).child(otherName).child(snapshotOld.getKey()).removeValue();
                    }
                }

                chatsLiveData.setValue(allMsgList);
//                System.out.println("What is the nameside " + otherName + allMsgList.size());

                // scroll to previous position UI of user
//                try{
//                    scrollToPreviousPosition(otherName, (int) scrollPositionMap.get(otherName));
//                } catch (Exception e){
//                    scrollToPreviousPosition(otherName, adapter.getItemCount() - 1);
//                }

//            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

    }
}
