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

@Database(entities = {UserOnChatUI_Model.class, MessageModel.class}, version = 1, exportSchema = false)
public abstract class TopStarDatabase extends RoomDatabase {

    private static TopStarDatabase instance;

    public abstract UserChatDao userChatDao();

    //method
    public static synchronized TopStarDatabase getInstance(Context context){

        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext()
                            , TopStarDatabase.class, "topstar_database")
                    .fallbackToDestructiveMigration()
//                    .addMigrations(MIGRATION_16_17) // Add your migration here
                    .build();
        }

        return instance;

    }

    // Define the migration from version 1 to version 2
    static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add the new column "otherUid" to the old table
            database.execSQL("ALTER TABLE usersOnChatUI ADD COLUMN otherUid TEXT");

            // Create a new table with the desired schema including the new "type" column
            database.execSQL("CREATE TABLE usersOnChatUI_new (" +
                    "timeSent INTEGER NOT NULL, " +
                    "message TEXT, " +
//                    "type INTEGER NOT NULL DEFAULT 0, " +  // New column with a default value
                    "msgStatus INTEGER NOT NULL, " +
                    "emojiOnly TEXT, " +
                    "otherUid TEXT, " +
                    "idKey TEXT, " +
                    "otherContactName TEXT, " +
//                    "fromUid TEXT, " +
                    "imageUrl TEXT, " +
                    "otherUserName TEXT, " +
                    "\"from\" TEXT, " +
                    "otherDisplayName TEXT, " +
                    "PRIMARY KEY(otherUid))"
            );

            // Copy the data from the old table to the new table
            database.execSQL("INSERT INTO usersOnChatUI_new (timeSent, message, msgStatus, emojiOnly, otherUid, idKey, " +
                    "otherContactName, imageUrl, otherUserName, \"from\", otherDisplayName) " +
                    "SELECT timeSent, message, msgStatus, emojiOnly, otherUid, idKey, " +
                    "otherContactName, imageUrl, otherUserName, \"from\", otherDisplayName " +
                    "FROM usersOnChatUI");

            // Remove the old table
            database.execSQL("DROP TABLE usersOnChatUI");

            // Rename the new table to the original table name
            database.execSQL("ALTER TABLE usersOnChatUI_new RENAME TO usersOnChatUI");
        }
    };

    //final int _cursorIndexOfOtherUid = CursorUtil.getColumnIndexOrThrow(_cursor, "otherUid");
    //      final int _cursorIndexOfFromUid = CursorUtil.getColumnIndexOrThrow(_cursor, "fromUid");
    //      final int _cursorIndexOfFrom = CursorUtil.getColumnIndexOrThrow(_cursor, "from");
    //      final int _cursorIndexOfEmojiOnly = CursorUtil.getColumnIndexOrThrow(_cursor, "emojiOnly");
    //      final int _cursorIndexOfIdKey = CursorUtil.getColumnIndexOrThrow(_cursor, "idKey");
    //      final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
    //      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
    //      final int _cursorIndexOfMsgStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "msgStatus");
    //      final int _cursorIndexOfTimeSent = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSent");
    //      final int _cursorIndexOfOtherUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "otherUserName");
    //      final int _cursorIndexOfOtherDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "otherDisplayName");
    //      final int _cursorIndexOfOtherContactName = CursorUtil.getColumnIndexOrThrow(_cursor, "otherContactName");
    //      final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");

// Found
    //TableInfo{name='usersOnChatUI', columns={
    // timeSent=Column{name='timeSent', type='INTEGER', affinity='3', notNull=true, primaryKeyPosition=0, defaultValue='null'}, =====
    // message=Column{name='message', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},     =====
    // type=Column{name='type', type='INTEGER', affinity='3', notNull=true, primaryKeyPosition=0, defaultValue='0'},            =====
    // msgStatus=Column{name='msgStatus', type='INTEGER', affinity='3', notNull=true, primaryKeyPosition=0, defaultValue='null'},
    // emojiOnly=Column{name='emojiOnly', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    // otherUid=Column{name='otherUid', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=1, defaultValue='null'},
    // idKey=Column{name='idKey', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},

    // otherContactName=Column{name='otherContactName', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    // fromUid=Column{name='fromUid', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    // imageUrl=Column{name='imageUrl', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    // otherUserName=Column{name='otherUserName', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    // from=Column{name='from', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    // otherDisplayName=Column{name='otherDisplayName', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'}},
    // foreignKeys=[], indices=[]}

    // expected

    //  Expected:
    //  TableInfo{name='usersOnChatUI', columns={
    //  timeSent=Column{name='timeSent', type='INTEGER', affinity='3', notNull=true, primaryKeyPosition=0, defaultValue='null'}, ====
    //  message=Column{name='message', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},     ====
    //  type=Column{name='type', type='INTEGER', affinity='3', notNull=true, primaryKeyPosition=0, defaultValue='null'},         ====
    //  msgStatus=Column{name='msgStatus', type='INTEGER', affinity='3', notNull=true, primaryKeyPosition=0, defaultValue='null'},  ===
    //  emojiOnly=Column{name='emojiOnly', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},    ===
    //  otherUid=Column{name='otherUid', type='TEXT', affinity='2', notNull=true, primaryKeyPosition=1, defaultValue='null'},
    //  idKey=Column{name='idKey', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},

    //  otherContactName=Column{name='otherContactName', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    //  fromUid=Column{name='fromUid', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    //  imageUrl=Column{name='imageUrl', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    //  otherUserName=Column{name='otherUserName', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    //  from=Column{name='from', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'},
    //  otherDisplayName=Column{name='otherDisplayName', type='TEXT', affinity='2', notNull=false, primaryKeyPosition=0, defaultValue='null'}},
    //  foreignKeys=[], indices=[]}
}
