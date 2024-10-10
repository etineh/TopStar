package com.pixel.chatapp.utilities;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixel.chatapp.dataModel.ContactModel;
import com.pixel.chatapp.dataModel.UserOnChatUI_Model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class LocalFileUtils {

    private static void saveUserListToLocalFile(List<UserOnChatUI_Model> userModelList, Context context)
    {
        Gson gson = new Gson();
        String json = gson.toJson(userModelList);

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("userList.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes());

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

    public static List<UserOnChatUI_Model> readUserListFromFile(Context context)
    {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("userList.json");
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
