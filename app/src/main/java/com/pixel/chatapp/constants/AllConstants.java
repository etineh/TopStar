package com.pixel.chatapp.constants;

import com.google.android.gms.common.data.SingleRefDataBufferIterator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface AllConstants {

//    FirebaseUser user = null;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    String MYUSERNAME = "MYUSERNAME";
    String USERNAME = "USERNAME";
    String SCROLLPOSITION = "lastScrollPosition";
    String OFFLINECHAT = "offlineChat";
    String PHOTO_OTHERUID = "PHOTO_UID";
    String VOICENOTE_UID = "VOICENOTE_UID";

    String PICKED_IMAGE_URI_PATH = "ImageUriPath";

    String MIC_ICON = "\uD83C\uDFA4  ";
    String DELETE_ICON = "\uD83D\uDDD1\uFE0F  ";
//    String PHOTO_ICON = "\uD83D\uDCF7  ";
    String PHOTO_ICON = "\uD83C\uDF03";
    String VIDEO_ICON ="\uD83C\uDFA5  ";
    String PIN_ICON = "\uD83D\uDCCC  ";
    String EDIT_ICON = "✏\uFE0F  ";

    String DOCUMENT_ICON = "\uD83D\uDCDC";
    String URI_PREF = "UriPrefs";
    String OLD_URI_LIST = "oldPhotoEditedUri";

    ExecutorService executors = Executors.newSingleThreadExecutor();

//    String FIREBASE_PHOTO_LOCATION = "media/photos/" + user.getUid() + "/" + System.currentTimeMillis();

    String JOIN = "winnerChatJoinPaths";

    String ALL_PHOTOS = "/Media/Photos/";
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
