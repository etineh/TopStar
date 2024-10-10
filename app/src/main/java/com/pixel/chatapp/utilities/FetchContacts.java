package com.pixel.chatapp.utilities;

import static com.pixel.chatapp.view_controller.MainActivity.contactNameShareRef;
import static com.pixel.chatapp.view_controller.MainActivity.handlerInternet;
import static com.pixel.chatapp.view_controller.MainActivity.otherUserHintRef;
import static com.pixel.chatapp.view_controller.MainActivity.refUsers;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.K;
import com.pixel.chatapp.services.api.dao_interface.ContactApiDao;
import com.pixel.chatapp.services.api.model.incoming.UserSearchM;
import com.pixel.chatapp.view_controller.MainActivity;
import com.pixel.chatapp.dataModel.ContactModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchContacts {

    public static List<ContactModel> contactList = new ArrayList<>();

    public static List<ContactModel> contactListFile = new ArrayList<>();


    public static void readContacts(Context context) {

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getNetworkCountryIso().toUpperCase();

        // Use a Map to store unique contacts (key: phone number, value: ContactModel)
        Map<String, ContactModel> contactMap = new HashMap<>();

        // Query contacts
        Cursor cursor = context.getContentResolver().query(
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

                    String sanitizedPhoneNumber = "+" + phoneNumber.replaceAll("[^\\d]", ""); // Remove non-numeric characters

                    // Check if the phone number already exists in the map
                    if (!contactMap.containsKey(sanitizedPhoneNumber)) {
                        // Create a new ContactModel and add it to the map
                        name = sanitizedPhoneNumber.equals(MainActivity.user.getPhoneNumber()) ? context.getString(R.string.you) : name;
                        ContactModel contact = new ContactModel(null, null, null, null, null,
                                context.getString(R.string.invite_now), null, name, phoneNumber);
                        contactMap.put(sanitizedPhoneNumber, contact);

                    }

                } else {
                    // Handle the case when the column indices are not found
                    Toast.makeText(context, context.getString(R.string.contactNotFound), Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());

            cursor.close(); // Close the cursor when done

            // Convert the Map values to a List
            contactList = new ArrayList<>(contactMap.values());     // number and names

            // Sort the list of ContactModel objects alphabetically by name
            Collections.sort(contactList, (contact1, contact2) -> contact1.getContactName().compareToIgnoreCase(contact2.getContactName()));

            List<String> numberList = new ArrayList<>(contactMap.keySet());  // only the numbers

            readContactFromAPI(numberList, context);

        } else {
            // Handle the case when the cursor is empty
            handlerInternet.post(()->{
                Toast.makeText(context, context.getString(R.string.contactNotFound), Toast.LENGTH_SHORT).show();
            });

        }

    }

    private static void readContactFromAPI(List<String> numberList, Context context)
    {
        ContactApiDao contactApiDao = K.retrofit.create(ContactApiDao.class);

        contactApiDao.contacts(numberList).enqueue(new Callback<List<UserSearchM>>() {
            @Override
            public void onResponse(Call<List<UserSearchM>> call, Response<List<UserSearchM>> response) {

                if(response.isSuccessful()){
                    List<UserSearchM> number = response.body();

                    for (int i = 0; i < number.size(); i++)
                    {
                        UserSearchM userSearchM = number.get(i);
//                        numberList.remove(userSearchM.getNumber());

                        for (int j = 0; j < contactList.size(); j++)
                        {
                            ContactModel contactModel = contactList.get(j);

                            if(contactModel.getNumber().equals(userSearchM.getNumber()))
                            {
                                contactList.remove(j);

                                getContactDetailsFromDB(userSearchM.getUid(), contactModel, number.size(), context);

                            }

                        }

                    }

                }

            }

            @Override
            public void onFailure(Call<List<UserSearchM>> call, Throwable throwable) {
                if (refreshContactListener != null) refreshContactListener.onFailure();
                System.out.println("what is err MA: FetchContact L170 " + throwable.getMessage());

            }
        });
    }

    private static final AtomicInteger counter = new AtomicInteger(0);

    private static void getContactDetailsFromDB(String contactUid, ContactModel contactModel, int contactSize, Context context)
    {
        refUsers.child(contactUid).child("general").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String imageLink = snapshot.child("image").exists() && !snapshot.child("image").getValue().toString().equals("null")
                        ? snapshot.child("image").getValue().toString() : null;

                String displayName = snapshot.child("displayName").exists() ?
                        snapshot.child("displayName").getValue().toString() : null;

                String otherUsername = snapshot.child("userName").exists() ?
                        snapshot.child("userName").getValue().toString() : null;

                String hint = snapshot.child("hint").exists() && !snapshot.child("userName").getValue().toString().isEmpty()
                        ? snapshot.child("hint").getValue().toString() : context.getString(R.string.hint2);


                contactModel.setBio(hint);
                contactModel.setOtherUserName(otherUsername);
                contactModel.setOtherDisplayName(displayName);
                contactModel.setImage(imageLink);
                contactModel.setOtherUid(contactUid);

                contactList.add(0, contactModel);

                // save contact name to each user uid
                contactNameShareRef.edit().putString(contactUid, contactModel.getContactName()).apply();
                // save each user hint
                otherUserHintRef.edit().putString(contactUid, hint).apply();

                // Increment the counter
                int count = counter.incrementAndGet();

                // Check if 20 iterations have been completed
                if (count == contactSize)
                {
                    // Sort the sublist alphabetically
                    List<ContactModel> sortedSubList = contactList.subList(0, contactSize);
                    Collections.sort(sortedSubList, (c1, c2) -> c1.getContactName().compareToIgnoreCase(c2.getContactName()));

                    // Save contacts to local file
                    K.executors.execute(()-> saveContactToLocalFile(context));
                    counter.set(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (refreshContactListener != null) refreshContactListener.onFailure();
            }
        });
    }

    private static void saveContactToLocalFile(Context context)
    {
        Gson gson = new Gson();
        String json = gson.toJson(contactList);

        contactListFile.clear();
        contactListFile = contactList;

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("contacts.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes());

            if (refreshContactListener != null) refreshContactListener.onSuccess();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static RefreshContactListener refreshContactListener;

    public interface RefreshContactListener {
        void onSuccess();
        void onFailure();
    }

    public static List<ContactModel> readContactFromFile(Context context)
    {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("contacts.json");
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String json = stringBuilder.toString();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ContactModel>>(){}.getType();

//            MainActivity.contactListFile = gson.fromJson(json, listType);
            return gson.fromJson(json, listType);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;

    }

}
