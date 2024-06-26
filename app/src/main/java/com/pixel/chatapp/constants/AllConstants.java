package com.pixel.chatapp.constants;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.common.data.SingleRefDataBufferIterator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public interface AllConstants {

//    FirebaseUser user = null;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

//    Retrofit retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:8080/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();

    Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    int type_text = 0;
    int type_voice_note = 1;
    int type_photo = 2;
    int type_document = 3;
    int type_audio = 4;
    int type_video = 5;
    int type_call = 6;
    int type_game = 7;
    int type_pin = 8;
    int type_empty = 10;

    String MYUSERNAME = "MYUSERNAME";
    String USERNAME = "USERNAME";
    String DEVICEFIRSTLOGIN = "DEVICEFIRSTLOGIN";
    String FIRSTTIME = "FIRSTTIME";

    String PROFILE_USERNAME = "USERNAME";
    String PROFILE_DISNAME = "DISPLAY-NAME";
    String PROFILE_HINT = "HINT";
    String PROFILE_NUMBER = "NUMBER";
    String RESET_LOGIN = "RESETLOGINDEATILS";

    String SCROLLPOSITION = "lastScrollPosition";
    String OFFLINECHAT = "offlineChat";
    String PHOTO_OTHERUID = "PHOTO_UID";
    String VOICENOTE_UID = "VOICENOTE_UID";
    String CONTACTNAME = "CONTACT_NAME";
    String FCMTOKEN = "FCM_TOKEN";
    Handler handler = new Handler(Looper.getMainLooper());

    String PICKED_IMAGE_URI_PATH = "ImageUriPath";

    String MIC_ICON = "\uD83C\uDFA4  ";
    String MUSIC_ICON = "\uD83C\uDFB5 ";

    String DELETE_ICON = "\uD83D\uDDD1\uFE0F ";
//    String PHOTO_ICON = "\uD83D\uDCF7  ";
    String PHOTO_ICON = "\uD83C\uDF03 ";
    String VIDEO_ICON ="\uD83C\uDFA5 ";
    String PIN_ICON = "\uD83D\uDCCC ";
    String EDIT_ICON = "✏\uFE0F  ";
    String CALL_ICON = "☎️ ";
    String GAME_ICON = "🎮  ";
    String DOCUMENT_ICON = "\uD83D\uDCDC ";

    String URI_PREF = "UriPrefs";
    String OLD_URI_LIST = "oldPhotoEditedUri";

    ExecutorService executors = Executors.newSingleThreadExecutor();

//    String FIREBASE_PHOTO_LOCATION = "media/photos/" + user.getUid() + "/" + System.currentTimeMillis();

    String JOIN = "TopstarChatJoinPaths";

    String ALL_PHOTOS = "/Media/Photos/";
    String ALL_VIDEOs= "/Media/Videos/";

    String ALL_DOCUMENTS = "/Media/Documents/";
    String ALL_AUDIO = "/Media/Audio/";
    String ALL_VOICENOTE = "/Media/Voice_Note/";

    String ALL_THUMBNAIL = "/Media/Thumbnail/";


    String VERIFICATION_CODE = "code";
    String IMAGE_PATH = "Media/Profile_Image/profile";
    String GROUP_IMAGE = "/Media/Profile/profile.jpg";
    String GROUP_IMAGE_MESSAGE =  "/Media/Images/";
    String VOICE_NOTE_PATH = "/Media/VoiceNote/";
    int STORAGE_REQUEST_CODE = 1000;
//    int USERNAME_CODE = 1;
    int CONTACTS_REQUEST_CODE = 2000;
    int RECORDING_REQUEST_CODE = 200;
    int CAMERA_REQUEST_CODE = 10;
    int CALL_CAMERA_REQUEST_CODE = 11;
    int CALL_RECORDING_REQUEST_CODE = 12;
    int BIOMETRIC_REQUEST_CODE = 13;
    int NOTIFICATION_REQUEST_CODE = 14;

    String CHANNEL_ID = "1000";
    int NOTIFICATION_ID = 100;
    int REQUEST_PICK_PDF_FILE = 1;

    String[] ACCEPTED_MIME_TYPES = {
            "application/pdf",   // PDF
            "image/jpeg",        // JPEG
            "image/png",         // PNG
            "application/cdr",   // CDR (CorelDRAW)
            "application/msword", // .doc
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "image/vnd.adobe.photoshop", // Photoshop (PSD)
            "audio/*",           // All audio formats
            "video/*",           // All video formats
            "application/vnd.android.package-archive" // APK
    };



    String NOTIFICATION_URL = "https://fcm.googleapis.com/fcm/send";
    String SERVER_KEY = "AAAApFehDUk:APA91bGNxMHslZsMLBQhfkIstegapDflh0czl7p_uLQD7dVnHqgq6hlWpKHjH1Eajr7vtRU0_2pjLzr_gL6ageC3twrkejOB1PzDpJdYPwPFCfvzG5CyIJ8CBjyE_yqjpJAhN9ZAMR6q";
    String UID = "FoRpUQwjvRZJYfEjVhm6iCbxTUg1";

}
