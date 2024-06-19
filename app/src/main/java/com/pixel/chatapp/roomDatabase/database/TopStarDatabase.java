package com.pixel.chatapp.roomDatabase.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.pixel.chatapp.model.MessageModel;
import com.pixel.chatapp.model.UserOnChatUI_Model;
import com.pixel.chatapp.roomDatabase.dao.UserChatDao;

@Database(entities = {UserOnChatUI_Model.class, MessageModel.class}, version = 6, exportSchema = false)
public abstract class TopStarDatabase extends RoomDatabase {

    private static TopStarDatabase instance;

    public abstract UserChatDao userChatDao();

    //method
    public static synchronized TopStarDatabase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext()
                            , TopStarDatabase.class, "topstar_database")
//                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_5_6) // Add your migration here
                    .build();
        }

        return instance;

    }

    // Define the migration from version 1 to version 2
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE usersOnChatUI ADD COLUMN numberOfNewChat INTEGER NOT NULL DEFAULT 0");

        }
    };

//    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            // Log current schema
//            logTableSchema(database, "chats");
//
//            // Create the new table with the correct schema
//            database.execSQL("CREATE TABLE IF NOT EXISTS `chats_new` (" +
//                    "`idKey` TEXT NOT NULL PRIMARY KEY, " +
//                    "`myUid` TEXT, " +
//                    "`fromUid` TEXT, " +
//                    "`message` TEXT, " +
//                    "`emojiOnly` TEXT, " +
//                    "`from` TEXT, " +
//                    "`replyFrom` TEXT, " +
//                    "`edit` TEXT, " +
//                    "`replyMsg` TEXT, " +
//                    "`timeSent` INTEGER NOT NULL, " +
//                    "`newChatNumberID` TEXT, " +
//                    "`msgStatus` INTEGER NOT NULL, " +
//                    "`type` INTEGER NOT NULL, " +
//                    "`imageSize` TEXT, " +
//                    "`replyID` TEXT, " +
//                    "`isChatPin` INTEGER NOT NULL, " +
//                    "`isChatForward` INTEGER NOT NULL, " +
//                    "`emoji` TEXT, " +
//                    "`id` TEXT, " +
//                    "`voiceNote` TEXT, " +
//                    "`vnDuration` TEXT, " +
//                    "`photoUriPath` TEXT, " +
//                    "`photoUriOriginal` TEXT)");
//
//            // Log new schema
//            logTableSchema(database, "chats_new");
//
//            // Copy data from the old table to the new table
//            // Copy the data from the old table to the new table
//            database.execSQL("INSERT INTO `chats_new` (`idKey`, `myUid`, `fromUid`, `message`, `emojiOnly`, `from`, `replyFrom`, `edit`, `replyMsg`, `timeSent`, `newChatNumberID`, `msgStatus`, `type`, `imageSize`, `replyID`, `isChatPin`, `isChatForward`, `emoji`, `id`, `voiceNote`, `vnDuration`, `photoUriPath`, `photoUriOriginal`) " +
//                    "SELECT `idKey`, `myUid`, `fromUid`, `message`, `emojiOnly`, `from`, `replyFrom`, `edit`, `replyMsg`, `timeSent`, `newChatNumberID`, `msgStatus`, `type`, `imageSize`, `replyID`, `isChatPin`, `isChatForward`, `emoji`, `id`, `voiceNote`, `vnDuration`, `photoUriPath`, `photoUriOriginal` " +
//                    "FROM `chats`");
//
//            // Remove the old table
//            database.execSQL("DROP TABLE `chats`");
//
//            // Rename the new table to the original name
//            database.execSQL("ALTER TABLE `chats_new` RENAME TO `chats`");
//        }
//    };

    private static void logTableSchema(SupportSQLiteDatabase database, String tableName) {
        Cursor cursor = database.query("PRAGMA table_info(" + tableName + ")");
        while (cursor.moveToNext()) {
            Log.d("MIGRATION", "Table: " + tableName + " Column: " + cursor.getString(1) + " Type: " + cursor.getString(2) + " NotNull: " + cursor.getInt(3));
        }
        cursor.close();
    }


    //                  + "\"from\" TEXT, " // Escaped reserved keyword

}
