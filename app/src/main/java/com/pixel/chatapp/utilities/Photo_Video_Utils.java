package com.pixel.chatapp.utilities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pixel.chatapp.R;
import com.pixel.chatapp.interface_listeners.SuccessAndFailureListener;
import com.pixel.chatapp.dataModel.MessageModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Photo_Video_Utils {

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
            // Image is not cached yet, save and share it
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), drawableResId, null);
            if(bitmapDrawable == null) {
                Toast.makeText(context, context.getString(R.string.failToSharePhoto), Toast.LENGTH_SHORT).show();
                return;
            }
            //  /storage/emulated/0/Android/data/com.pixel.chatapp/files/Media/Thumbnail/logo_image.png
            File savedImage = saveBitmapToThumbnailFolder(context, bitmapDrawable.getBitmap(), "topstar_logo_image.png");
            shareImageUsingContentUri(context, null, savedImage, shareTitle);
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
        // Default to generic MIME type for documents if MIME type cannot be determined
        shareIntent.setType(Objects.requireNonNullElse(mimeType, "application/octet-stream"));

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


    public static void saveMediaToGallery(Context context, String photoUriString, SuccessAndFailureListener successAndFailureListener)
    {
        Uri mediaUri = Uri.parse(photoUriString);  // Convert the string to a Uri

        if (mediaUri == null) {
            successAndFailureListener.onFailure(context.getString(R.string.notFound));
            return;
        }

        if(FileUtils.isPhotoFile(mediaUri, context))    // Use Glide to image extract the bitmap to avoid 90 degree rotation
        {
            Glide.with(context)
                    .asBitmap()
                    .load(mediaUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            // Save the bitmap to the gallery
                            saveImageBitmapToGallery(context, resource, new SuccessAndFailureListener() {
                                @Override
                                public void onSuccess() {
                                    successAndFailureListener.onSuccess();
                                }

                                @Override
                                public void onFailure(String error) {
                                    successAndFailureListener.onFailure(error);
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Handle if the image load is cancelled
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            successAndFailureListener.onFailure("glide error");
                        }
                    });

        } else if (FileUtils.isVideoFile(mediaUri, context))    // Handle video saving
        {
            //  content://media/external/video/media/1000040476
            saveVideoToGallery(context, mediaUri, new SuccessAndFailureListener(){

                @Override
                public void onSuccess() {
                    successAndFailureListener.onSuccess();
                }

                @Override
                public void onFailure(String error) {
                    successAndFailureListener.onFailure(error);
                }
            });

        } else {
            successAndFailureListener.onFailure("unsupported file");
        }

    }

    private static void saveImageBitmapToGallery(Context context, Bitmap bitmap, SuccessAndFailureListener successAndFailureListener)
    {
        if (bitmap == null) {
            successAndFailureListener.onFailure(context.getString(R.string.notFound));
            return;
        }

        String fileName = context.getString(R.string.app_name) + System.currentTimeMillis() + ".jpg";
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)     // For Android 10 and above
        {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                if (imageUri == null) {
                    successAndFailureListener.onFailure(context.getString(R.string.notFound));
                    return;
                }
                fos = resolver.openOutputStream(imageUri);
                if (fos == null) return;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                successAndFailureListener.onSuccess();

            } catch (IOException e) {
                successAndFailureListener.onFailure(e.getMessage());
                e.printStackTrace();
            }

        } else      // For Android versions below 10
        {
            File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(imagesDir, fileName);
            try {
                fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                MediaScannerConnection.scanFile( context, new String[]{imageFile.getAbsolutePath()},
                        null, (path, uri) -> successAndFailureListener.onSuccess() );

            } catch (IOException e) {
                successAndFailureListener.onFailure(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

    }

    private static void saveVideoToGallery(Context context, Uri videoUri, SuccessAndFailureListener successAndFailureListener)
    {
        String fileName = context.getString(R.string.app_name) + System.currentTimeMillis() + ".mp4"; // Adjust extension as needed
        ContentResolver resolver = context.getContentResolver();
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = resolver.openInputStream(videoUri);
            if (inputStream == null) {
                successAndFailureListener.onFailure(context.getString(R.string.errorOccur));
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)     // For Android 10 and above
            {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);

                Uri videoContentUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (videoContentUri == null) {
                    successAndFailureListener.onFailure(context.getString(R.string.errorOccur));
                    return;
                }

                outputStream = resolver.openOutputStream(videoContentUri);
                if (outputStream == null) {
                    successAndFailureListener.onFailure(context.getString(R.string.errorOccur));
                    return;
                }

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                successAndFailureListener.onSuccess();

            } else  // For Android versions below 10
            {
                File videosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                File videoFile = new File(videosDir, fileName);

                outputStream = new FileOutputStream(videoFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                MediaScannerConnection.scanFile( context, new String[]{videoFile.getAbsolutePath()},
                        null, (path, uri) -> successAndFailureListener.onSuccess() );
            }

        } catch (IOException e) {
            successAndFailureListener.onFailure(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}







