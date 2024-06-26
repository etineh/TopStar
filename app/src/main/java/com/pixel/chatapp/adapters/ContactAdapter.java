package com.pixel.chatapp.adapters;

import static com.pixel.chatapp.home.MainActivity.myProfileShareRef;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pixel.chatapp.activities.LinearLayoutManagerWrapper;
import com.pixel.chatapp.constants.AllConstants;
import com.pixel.chatapp.home.fragments.ChatsFragment;
import com.pixel.chatapp.interface_listeners.FragmentListener;
import com.pixel.chatapp.R;
import com.pixel.chatapp.home.MainActivity;
import com.pixel.chatapp.model.ContactModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.UserViewHolder> {

    MainActivity mainActivity = new MainActivity();
    List<ContactModel> allContactList;
    List<ContactModel> copyContacts = new ArrayList<>();
    DatabaseReference refUsers, refClearSign;
    FirebaseUser user;
    ChatsFragment chatsFragment = new ChatsFragment();

    public static Context mContext;
    Handler handler = new Handler();

    public static Context getmContext() {
        return mContext;
    }

    public static void setmContext(Context mContext_) {
        mContext = mContext_;
    }

    //  constructor
    public ContactAdapter(List<ContactModel> allContactList, Context mContext) {
        this.allContactList = allContactList;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refUsers = FirebaseDatabase.getInstance().getReference("Users");
        refClearSign = FirebaseDatabase.getInstance().getReference("ClearSign");

    }


    public interface BackButtonClickListener {
        void onBackButtonClicked();
    }

    private BackButtonClickListener backButtonClickListener;

    public void setBackButtonClickListener(BackButtonClickListener listener) {
        this.backButtonClickListener = listener;
    }

    private FragmentListener listener;

    public void setListener(FragmentListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_card, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.textViewUser.setText(null);
        holder.textViewMsg.setText(null);
        holder.imageView.setImageResource(0);

        ContactModel contactModel = allContactList.get(position);

        //  get all other-user details -----------------------
        String otherUid = contactModel.getOtherUid();
        String otherUsername = contactModel.getOtherUserName();
        String otherDisplayname = contactModel.getOtherUserName();
        String myUserName = contactModel.getMyUserName();
        String imageUrl = contactModel.getImage();
        String hint = contactModel.getBio();
        String contactName = contactModel.getContactName();
        String number = contactModel.getNumber();

        if (imageUrl == null || imageUrl.equals("null")) {      // display user photo
            holder.imageView.setImageResource(R.drawable.person_round);
        }
        else Picasso.get().load(imageUrl).into(holder.imageView);

        holder.textViewUser.setText(contactName);                 // display all myUserName
        holder.textViewMsg.setText(hint);                    // display Hint of the user

        // add up user recyclerView that doesn't exist
        listener.sendRecyclerView(holder.recyclerChat, otherUid);

        // check if other user deleted me from his chat list, if yes, then clear all the user chat
//        MainActivity.checkClearChatsDB(otherUid);

        // what happen when the cardView is click
        holder.itemView.setOnClickListener(view -> {
            String myUsername = myProfileShareRef.getString(AllConstants.PROFILE_USERNAME, MainActivity.getMyUserName);
            String myDisplayName = myProfileShareRef.getString(AllConstants.PROFILE_DISNAME, null);

            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50)
                    .withEndAction(() -> {

                        if(otherUid != null)
                        {
                            listener.chatBodyVisibility(contactName, imageUrl, myUsername, otherUid, getmContext(), holder.recyclerChat);

                            listener.getLastSeenAndOnline(otherUid, mContext);

                            listener.msgBackgroundActivities(otherUid);

                            listener.callAllMethods(otherUid, getmContext(), new Activity());

                            // activate adapter for user if null
                            if(MainActivity.adapterMap.get(otherUid) == null){
                                // call getMessage() to add up new user adapter
                                listener.getMessage(myUsername, otherUid, getmContext(), false);
                            }
                            // check if network is okay and remove the network bar constraint
                            new Handler().postDelayed(() -> {
                                if(MainActivity.networkListener.equals("yes"))
                                    if(MainActivity.checkNetworkView != null) MainActivity.checkNetworkView.setVisibility(View.GONE);
                            }, 1000);

                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("sms:" + number));
                            intent.putExtra("sms_body", mContext.getString(R.string.appInvite));

                            mContext.startActivity(intent);
                            Toast.makeText(mContext, "Invite user " + number, Toast.LENGTH_SHORT).show();
                        }

                        // Reset the scale
                        view.setScaleX(1.0f);
                        view.setScaleY(1.0f);

                    }).start();

            if (backButtonClickListener != null) {  // close contact list
                backButtonClickListener.onBackButtonClicked();
            }

        });

    }
    
    //  ========    methods
    // Method to update dataset
//    public void updateDataSet(List<ContactModel> newList) {
//        allContactList.clear();
//        allContactList.addAll(newList);
//        notifyDataSetChanged();
//    }

    public void updateDataSet(final List<ContactModel> newList)
    {
        for (ContactModel newContact : newList)
        {
            for (int i = 0; i < allContactList.size(); i++)
            {
                ContactModel contact = allContactList.get(i);

                if(contact != null && contact.getContactName() != null){
                    if (contact.getContactName().equalsIgnoreCase(newContact.getContactName())) {
                        // Remove the matching contact from its current position
                        allContactList.remove(i);
                        // Add the matching contact to the top
                        allContactList.add(0, newContact);

                        break; // Move to the next newContact
                    }
                }
            }
        }
        handler.post(() -> notifyDataSetChanged());

    }

    public void resetContactList() {
        Collections.sort(allContactList, (contact1, contact2) -> {
            if (contact1 != null && contact2 != null && contact1.getBio() != null && contact2.getBio() != null) {
                String name1 = contact1.getBio().equalsIgnoreCase(getmContext().getString(R.string.invite_now)) ? "" : contact1.getContactName();
                String name2 = contact2.getBio().equalsIgnoreCase(getmContext().getString(R.string.invite_now)) ? "" : contact2.getContactName();

                // Compare based on bio
                int bioComparison = name2.compareToIgnoreCase(name1);
                if (bioComparison != 0) {
                    // Contacts with non-"Invite now" bio come first
                    return bioComparison;
                } else {
                    // If bios are the same, compare based on contact name
                    return contact1.getContactName().compareToIgnoreCase(contact2.getContactName());
                }
            }
            return 0;
        });

        synchronizeContactLists();

        handler.post(this::notifyDataSetChanged);

    }

    public void synchronizeContactLists() {
        // Iterate over MainActivity.contactListFile
        for (ContactModel contact : MainActivity.contactListFile) {
            // Check if the contact exists in allContactList
            if (!allContactList.contains(contact)) {
                // Contact exists in MainActivity.contactListFile but not in allContactList, so add it
                allContactList.add(contact);
            }
        }

        // Create a copy of allContactList to avoid ConcurrentModificationException
        List<ContactModel> allContactListCopy = new ArrayList<>(allContactList);

        // Iterate over allContactListCopy to remove contacts not present in MainActivity.contactListFile
        for (ContactModel contact : allContactListCopy) {
            // Check if the contact exists in MainActivity.contactListFile
            if (!MainActivity.contactListFile.contains(contact)) {
                // Contact exists in allContactList but not in MainActivity.contactListFile, so remove it
                allContactList.remove(contact);
            }
        }
    }


//    public void resetContactList()
//    {
//        allContactList.clear();
//        MainActivity.contactListFile.clear();
////
//        FetchContacts.readContactFromFile(getmContext());
//        allContactList = MainActivity.contactListFile;
//        handler.post(()-> notifyDataSetChanged());
//
//    }


    @Override
    public int getItemCount() {
        if(allContactList != null){

            return allContactList.size();
        }
        return 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{

        private final CircleImageView imageView;
        private final TextView textViewUser;
        private final TextView textViewMsg;
        RecyclerView recyclerChat;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageViewUsers);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);

            recyclerChat = itemView.findViewById(R.id.recyclerChat);
//            recyclerChat.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManagerWrapper(itemView.getContext(), LinearLayoutManager.VERTICAL, false);

            recyclerChat.setLayoutManager(mLayoutManager);
        }

    }
}







