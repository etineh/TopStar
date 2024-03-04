# For type
0 is text, 1 is voice note, 2 is photo, 3 is document, 4 is audio (mp3), 5 is video

# Sending photo
Photo is not downloaded to app storage, it retrieve uri from the phone media directly
except a user is sharing from another app, then it will be saved to app storage.
Thumbnail photo is generated and saved to app storage only when it's successfully sent.
    -   vnDuration is used for both voice note and video duration
# Video uri type picked from gallery inside app    
    -   content://media/external/video/media/1000142346
# Video uri type picked from gallery outside app    

# Photo uri type picked from gallery inside app
    -   content://media/external/images/media/1000147354

# Sending Video
Video is downloaded or copy to app storage on MessageAdapter when it is successfully sent.
    - from content://media/external/video/media/1000142346 to 
        /storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Videos/Topper_06675az_recorder_20230607_104341.mp4
    
    -- from camera recording and gallery
    content://media/external/video/media/1000149782

    -- from forwarding (file already in app storage)
    file:///storage/emulated/0/Android/data/com.pixel.chatapp/files/Movies/20240229_102925.mp4

// saving the original quality image to firebase    - convert storage to file:// with the link below
        Uri uriOnPhone = modelChats.getPhotoUriOriginal().startsWith("/storage/") ? Uri.fromFile(new File(modelChats.getPhotoUriOriginal()))
                : Uri.parse(modelChats.getPhotoUriOriginal());  // change from /storage to file://
