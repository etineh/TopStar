package com.pixel.chatapp.all_utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.pixel.chatapp.R;
import com.pixel.chatapp.model.MessageModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SharePhotoUtil {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void downloadImageFromInternetAndShare(Context context, String imageUrl, String shareTitle) {

        executor.execute(() -> {
            try {
                FutureTarget<Bitmap> futureTarget = Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .submit();
                Bitmap bitmap = futureTarget.get();

                if (bitmap != null) {
                    // Save the downloaded image to the thumbnail directory
                    File cachedImage = saveBitmapToThumbnailFolder(context, bitmap, "download_photo_fromTopStar.png");
                    if (cachedImage != null) {
                        // Share the cached image
                        shareImageUsingContentUri(context, null, cachedImage, shareTitle);
                    } else {
                        showToastOnUiThread(context, context.getString(R.string.sharePhotoFails));
                    }
                } else {
                    showToastOnUiThread(context, context.getString(R.string.downloadFails));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

//        try {
//            FutureTarget<Bitmap> futureTarget = Glide.with(context)
//                    .asBitmap()
//                    .load(imageUrl)
//                    .submit();
//
//            Bitmap bitmap = futureTarget.get();
//            if (bitmap != null) {
//                // Save the downloaded image to the thumbnail directory
//                File cachedImage = saveBitmapToThumbnailFolder(context, bitmap);
//                if (cachedImage != null) {
//                    // Share the cached image
//                    shareImageUsingContentUri(context, null, cachedImage, shareTitle);
//                } else {
//                    Toast.makeText(context, context.getString(R.string.sharePhotoFails), Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(context, context.getString(R.string.downloadFails), Toast.LENGTH_SHORT).show();
//            }
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
    }

    private static void showToastOnUiThread(Context context, String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }


    public static void shareDrawableImage(Context context, int drawableResId, String shareTitle) {
        // Check if the image is already cached/saved
        File cachedImage = getCachedImage(context);
        if (cachedImage != null) {
            // Image is cached, share it
            shareImageUsingContentUri(context, null, cachedImage, shareTitle);
        } else {
            // Image is not cached, save and share it
            Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(drawableResId)).getBitmap();
            //  /storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Thumbnail/logo_image.png
            File savedImage = saveBitmapToThumbnailFolder(context, bitmap, "topstar_logo_image.png");
            if (savedImage != null) {
                shareImageUsingContentUri(context, null, savedImage, shareTitle);
            } else {
                Toast.makeText(context, "Failed to share image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static File getCachedImage(Context context) {
        File thumbnailFolder = FolderUtils.getThumbnailFolder(context);
        File cachedImage = new File(thumbnailFolder, "topstar_logo_image.png");
        if (cachedImage.exists()) {
            return cachedImage;
        }
        return null;
    }


    private static File saveBitmapToThumbnailFolder(Context context, Bitmap bitmap, String fileName) {
        File thumbnailFolder = FolderUtils.getThumbnailFolder(context);
        try {
            File file = new File(thumbnailFolder, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void shareImageUsingContentUri(Context context, MessageModel model, File fileImage, String shareTitle) {

        // fileImage -->  /storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Thumbnail/logo_image.png

        Uri photoOrDocUri = fileImage == null ? uniqueUriForSharingPhotoOrDoc(model, context)
                : FileProvider.getUriForFile(context, "com.pixel.chatapp.fileprovider", fileImage);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        String mimeType = context.getContentResolver().getType(photoOrDocUri);
        if (mimeType != null) {
            shareIntent.setType(mimeType);
        } else {
            // Default to generic MIME type for documents if MIME type cannot be determined
            shareIntent.setType("application/octet-stream");
        }

        shareIntent.putExtra(Intent.EXTRA_STREAM, photoOrDocUri);

        if(shareTitle != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareTitle);
        } else {
            if(model != null && model.getMessage() != null)
                shareIntent.putExtra(Intent.EXTRA_TEXT, model.getMessage());
        }

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.app_name)));

    }

    public static Uri uniqueUriForSharingPhotoOrDoc(MessageModel model, Context context){
        // send app logo in case any error getting the photo uri path
        Uri photoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.logo);

        if(model.getPhotoUriOriginal() != null){
            if(model.getPhotoUriOriginal().startsWith("file:/")) {
                try {
                    File file = new File(new URI( model.getPhotoUriOriginal() ));
                    photoUri = FileProvider.getUriForFile(context, "com.pixel.chatapp.fileprovider", file);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

            } else if(model.getPhotoUriOriginal().startsWith("/storage")) {
                File file = new File( model.getPhotoUriOriginal() );
                photoUri = FileProvider.getUriForFile(context, "com.pixel.chatapp.fileprovider", file);

            }else if (model.getPhotoUriOriginal().startsWith("content:/"))
            {
                photoUri = Uri.parse(model.getPhotoUriOriginal());
            }
        } else if (model.getVoiceNote() != null) {
            if(model.getVoiceNote().startsWith("file:/")) {
                try {
                    File file = new File(new URI( model.getVoiceNote() ));
                    photoUri = FileProvider.getUriForFile(context, "com.pixel.chatapp.fileprovider", file);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

            }
            else if(model.getVoiceNote().startsWith("/storage")) {
                File file = new File( model.getVoiceNote() );
                photoUri = FileProvider.getUriForFile(context, "com.pixel.chatapp.fileprovider", file);
            }else if (model.getVoiceNote().startsWith("content:/")) {
                photoUri = Uri.parse(model.getVoiceNote());
            }

        }
        return photoUri;

        //  content://media/external/images/media/1000143399  -- from device
        //  file:///storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Photos/WinnerChat_1707594880207.jpg -- from app storage
    }


}
