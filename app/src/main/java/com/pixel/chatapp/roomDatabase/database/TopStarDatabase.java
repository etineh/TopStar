package com.pixel.chatapp.roomDatabase.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.dao.UserChatDao;

@Database(entities = {UserOnChatUI_Model.class, MessageModel.class}, version = 3, exportSchema = false)
public abstract class TopStarDatabase extends RoomDatabase {

    private static TopStarDatabase instance;

    public abstract UserChatDao userChatDao();

    //method
    public static synchronized TopStarDatabase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext()
                            , TopStarDatabase.class, "topstar_database")
//                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_2_3) // Add your migration here
                    .build();
        }

        return instance;

    }

    // Define the migration from version 1 to version 2
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE usersOnChatUI ADD COLUMN myUid TEXT NOT NULL DEFAULT 'ucdySn50eeRx1iquBilJtYL86e92'");

        }
    };

}
