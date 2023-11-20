package com.pixel.chatapp.roomDatabase.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.dao.UserChatDao;

@Database(entities = {UserOnChatUI_Model.class, MessageModel.class}, version = 1, exportSchema = false)
public abstract class WinnerDatabase  extends RoomDatabase {

    private static WinnerDatabase instance;

    public abstract UserChatDao userChatDao();

    //method
    public static synchronized WinnerDatabase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext()
                            ,WinnerDatabase.class, "winnerChat_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;

    }


}
