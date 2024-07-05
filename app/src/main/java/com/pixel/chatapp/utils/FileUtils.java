package com.pixel.chatapp.utils;

import static com.pixel.chatapp.utils.FolderUtils.getAudioFolder;
import static com.pixel.chatapp.utils.FolderUtils.getDocumentFolder;
import static com.pixel.chatapp.utils.FolderUtils.getThumbnailFolder;
import static com.pixel.chatapp.utils.FolderUtils.getVideoFolder;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.iceteck.silicompressorr.SiliCompressor;
import com.pixel.chatapp.R;
import com.pixel.chatapp.model.MessageModel;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileUtils {

    public static String getFileSize(Uri imageUri, Context context){

        try {   // get the total image length
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                int fileSizeBytes = inputStream.available();
                int fileSizeKB = fileSizeBytes / 1024; // Size in kilobytes
                int fileSizeMB = fileSizeKB / 1024; // Size in megabytes

                String sizeString = fileSizeKB < 1000.0 ? Math.round(fileSizeKB) + " kB" : Math.round(fileSizeMB) + " MB";

                inputStream.close();

                return sizeString;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error Occur FileUtil 62: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("Error Occur FileUtil 62: " + imageUri + " show error: " + e.getMessage());
        }
        return context.getString(R.string.app_name); // Return an empty string or another appropriate default value in case of an error

    }

//    e.g from 200kb to 200_000
    public static int convertFileSizeToInt(String photoSize){
        String[] parts = photoSize.split(" ");
        int sizeValue = Integer.parseInt(parts[0]); // Extract the numeric part of the string
        String unit = parts[1]; // Extract the unit (e.g., "kB" or "MB")

        int fileSizeBytes;
        if (unit.equals("kB")) {
            fileSizeBytes = sizeValue * 1024; // Convert kilobytes to bytes
        } else if (unit.equals("MB")) {
            fileSizeBytes = sizeValue * 1024 * 1024; // Convert megabytes to bytes
        } else {
            // Handle unsupported units or invalid input
            throw new IllegalArgumentException("Unsupported unit: " + unit);
        }

        return  fileSizeBytes;
    }
    public static String getEstimateVideoSize(Uri imageUri, Context context){

        try {   // get the total image length
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                int fileSizeBytes = inputStream.available();
                int fileSizeKB = fileSizeBytes / (1024); // Size in kilobytes ~ 5 is the estimate
                int fileSizeMB = fileSizeKB / (1024); // Size in megabytes

                String sizeString = fileSizeKB < 10000.0 ? Math.round((float) fileSizeKB /10) + " kB" : Math.round((float) fileSizeMB /10) + " MB";

                inputStream.close();

                return sizeString;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error Occur FileUtil 105: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("Error Occur FileUtil 106: " + imageUri + " show error: " + e.getMessage());
        }
        return context.getString(R.string.app_name); // Return an empty string or another appropriate default value in case of an error

    }

    public static String getEstimatePhotoSize(Uri imageUri, Context context){

        try {   // get the total image length
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                int fileSizeBytes = inputStream.available();
                int fileSizeKB = fileSizeBytes / (1024); // Size in kilobytes ~ 5 is the estimate
                int fileSizeMB = fileSizeKB / (1024); // Size in megabytes
                String sizeString = fileSizeKB < 4000.0 ? Math.round((float) fileSizeKB /4) + " kB" : Math.round((float) fileSizeMB /4) + " MB";

                inputStream.close();

                return sizeString;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error Occur FileUtil 129: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("Error Occur FileUtil 129: " + imageUri + " show error: " + e.getMessage());
        }
        return context.getString(R.string.app_name); // Return an empty string or another appropriate default value in case of an error

    }
    public static boolean isFileLessThan150Kb(Uri imageUri, Context context){

        try {   // get the total image length
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                int fileSizeBytes = inputStream.available();
                int fileSizeKB = fileSizeBytes / 1024; // Size in kilobytes

                inputStream.close();

                return fileSizeKB < 150.0;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // Return an empty string or another appropriate default value in case of an error

    }

    public static void replaceSnapPhotoWithCompressFile(MessageModel modelChats, byte[] compressedBytes){
        if(modelChats.getEmojiOnly() != null){  // it was snap with app camera, so replaced with compressed photo
            File file = new File(modelChats.getEmojiOnly());
            if(file.exists()){
                // Write the compressed byte array to the file
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(compressedBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap convertUriToBitmap(Context context, Uri uri) {
        Bitmap originalBitmap = null; // Initialize to null to handle the case of failure

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error, e.g., log it or show a message to the user
        }

        return originalBitmap; // Return the Bitmap, which might be null if an error occurred
    }

    public interface CompressionCallback {
        void onCompressionComplete(String compressedVideoPath);
    }

    public static void compressVideo(Uri uriFile, Context context, CompressionCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {
            String videoPath;
            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());

            File compressedFile = new File(getVideoFolder(context), context.getString(R.string.app_name) + "_" + name + ".mp4");

            String uriPathToAppStorage = Uri.fromFile(compressedFile).toString();

            //compress video
            try {
                videoPath = SiliCompressor.with(context).compressVideo(uriFile, uriPathToAppStorage);
                // Notify the callback with the result
                callback.onCompressionComplete(videoPath);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

        });
    }


    //  copy the file from phone storage ("content/com.provider" or "content://com.android") to app storage ("/storage/emulated/0").
    //  This is to enable user share the file to other app
    public static String saveFileFromContentUriToAppStorage(Uri uriFile, Context context) {
        // Get the content resolver
        ContentResolver contentResolver = context.getContentResolver();

        // Open an input stream for the content URI
        try {
            InputStream inputStream = contentResolver.openInputStream(uriFile);

            String uuid = String.valueOf(System.currentTimeMillis()).substring(8);
            // Create a file in your app's internal storage directory
            File file;
            if(isAudioFile(uriFile, context)){
                file = new File(getAudioFolder(context), context.getString(R.string.app_name) + "_" + uuid + getFileName(uriFile, context) );
            } else if(isVideoFile(uriFile, context)) {
                file = new File(getVideoFolder(context), context.getString(R.string.app_name) + "_" + uuid + getFileName(uriFile, context) );
            }else {
                file = new File(getDocumentFolder(context), context.getString(R.string.app_name) + "_" + uuid + getFileName(uriFile, context) );
//                file = new File(getPhotoFolder(), getString(R.string.app_name) + "_" + getFileName(uriFile) );
            }

            // Copy the content from the input stream to the file
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close streams
            outputStream.close();
            inputStream.close();

            return file.getPath();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle errors
        }

        return null;
    }

    // Method to compress video by re-encoding with lower bit rate
//    public static File compressVideo(Uri videoUri, Context context) {
//        try {
//            // Create a MediaExtractor to extract video data from the URI
//            MediaExtractor extractor = new MediaExtractor();
//            extractor.setDataSource(context, videoUri, null);
//
//            // Find and select the video track
//            int trackIndex = -1;
//            for (int i = 0; i < extractor.getTrackCount(); i++) {
//                MediaFormat format = extractor.getTrackFormat(i);
//                String mime = format.getString(MediaFormat.KEY_MIME);
//                if (mime.startsWith("video/")) {
//                    trackIndex = i;
//                    break;
//                }
//            }
//            if (trackIndex < 0) {
//                throw new RuntimeException("No video track found");
//            }
//            extractor.selectTrack(trackIndex);
//
//            // Create a MediaCodec encoder for video compression
//            MediaCodec encoder = MediaCodec.createEncoderByType("video/avc");
//            MediaFormat inputFormat = MediaFormat.createVideoFormat("video/avc", 1280, 720); // Adjust resolution as needed
//            inputFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1024 * 1024); // Adjust bit rate for compression
//            encoder.configure(inputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            encoder.start();
//
//            // Create a temporary file to store the compressed video
//            String uuid = String.valueOf(System.currentTimeMillis()).substring(8);
//            File compressedFile = new File(getVideoFolder(context), context.getString(R.string.app_name) + "_" + uuid + getFileName(videoUri, context) );
//
//            FileOutputStream outputStream = new FileOutputStream(compressedFile);
//
//            // Encode and compress the video frame by frame
//            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
//            ByteBuffer[] inputBuffers = encoder.getInputBuffers();
//            ByteBuffer[] outputBuffers = encoder.getOutputBuffers();
//            boolean inputDone = false;
//            boolean outputDone = false;
//            while (!outputDone) {
//                if (!inputDone) {
//                    int inputBufferIndex = encoder.dequeueInputBuffer(-1);
//                    if (inputBufferIndex >= 0) {
//                        ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
//                        int sampleSize = extractor.readSampleData(inputBuffer, 0);
//                        if (sampleSize < 0) {
//                            encoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                            inputDone = true;
//                        } else {
//                            long presentationTimeUs = extractor.getSampleTime();
//                            encoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs, 0);
//                            extractor.advance();
//                        }
//                    }
//                }
//
//                int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, -1);
//                if (outputBufferIndex >= 0) {
//                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
//                    outputStream.write(outputBuffer.array(), bufferInfo.offset, bufferInfo.size);
//                    encoder.releaseOutputBuffer(outputBufferIndex, false);
//                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                        outputDone = true;
//                    }
//                }
//            }
//
//            // Release resources
//            encoder.stop();
//            encoder.release();
//            extractor.release();
//            outputStream.close();
//
//            return compressedFile;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    public static String getFileName(Uri uri, Context context) {
        String displayName = context.getString(R.string.app_name);
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        displayName = cursor.getString(index);
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            displayName = uri.getLastPathSegment();
        }
        return displayName;
    }

    public static boolean isPdfFile(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);
        return mimeType != null && mimeType.equals("application/pdf");
    }

    public static boolean isMsWordFile(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || mimeType.equals("application/msword");
    }

    public static boolean isPhotoFile(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);
        // Check if mimeType is null or empty, and return false in such cases
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        // Check if mimeType represents an image type
        return mimeType.equals("image/jpeg") || mimeType.equals("image/png") || mimeType.startsWith("image/");
    }


    public static boolean isAudioFile(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.equals("audio/mpeg") || mimeType.equals("audio/mp3") || mimeType.equals("audio/*");
    }

    public static boolean isVideoFile(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.equals("video/mp4") || mimeType.equals("video/*");
    }

    public static boolean isCdrFile(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.equals("application/cdr") || mimeType.equals("image/cdr");
    }

    public static boolean isPhotoshopFile(Uri uri, Context context) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.equals("image/vnd.adobe.photoshop");
    }

    public static Uri reduceImageSize(Bitmap bitmapImage, Uri originalImageUri, int maxSize, Context context) {
        try {
            // Get the original bitmap from the Uri
            Bitmap originalBitmap = bitmapImage == null ?
                    MediaStore.Images.Media.getBitmap(context.getContentResolver(), originalImageUri) : bitmapImage;

            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            float ratio = (float) width / (float) height;
            // reduce the length and width
            if (ratio > 1) {
                width = maxSize;
                height = (int) (width / ratio);
            } else {
                height = maxSize;
                width = (int) (height * ratio);
            }

            // Resize the original bitmap
            Bitmap reduceSize = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

            // Calculate the cropping dimensions based on the smaller dimension
            int lowDimension = Math.min(width, height);

            // Calculate the center coordinates for cropping
            int centerX = reduceSize.getWidth() / 2;
            int centerY = reduceSize.getHeight() / 2;

            // Crop the reduced size image
            Bitmap croppedBitmap = Bitmap.createBitmap(reduceSize, centerX - (lowDimension / 2),
                    centerY - (lowDimension / 2), lowDimension, lowDimension);

            // save the photo to phone app memory
            File saveImageToPhoneUri = new File(getThumbnailFolder(context), context.getString(R.string.app_name) + System.currentTimeMillis() + "_.jpg");
            OutputStream outputStream;
            try {
                outputStream = new FileOutputStream(saveImageToPhoneUri);
                // save the image to the phone
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return Uri.fromFile(saveImageToPhoneUri);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap createPhotoThumbnail(Uri uri, Context context) {
        Bitmap thumbnail = null;
        try {
            // Load the full-size image
            Bitmap fullSizeBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

            // Create a thumbnail of the image
            thumbnail = ThumbnailUtils.extractThumbnail(fullSizeBitmap, 400, 400);

            // Recycle the full-size bitmap to free up memory
            fullSizeBitmap.recycle();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnail;
    }


    public static Uri getThumbnailUri(Bitmap thumbnail, Context context) {
        // save the low photo or thumbnail to phone app memory
        File saveImageToPhoneUri = new File(getThumbnailFolder(context),
                context.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".jpg");

        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(saveImageToPhoneUri);
            // save the image to the phone
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(saveImageToPhoneUri);

    }

    public static Bitmap createVideoThumbnail(Context context, Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            // Set data source to the video URI
            retriever.setDataSource(context, videoUri);

            // Retrieve the thumbnail at the first frame
            Bitmap thumbnail = retriever.getFrameAtTime(0);
            return thumbnail;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            // Release resources
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String downloadThumbnailFile(String fileUrl, Context context) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(fileUrl)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                // Save the response body (file) to the specified path
                File createThumbnailPath = new File(getThumbnailFolder(context),
                        context.getString(R.string.app_name) + "_" + System.currentTimeMillis() + ".jpg");
                 try (OutputStream outputStream = new FileOutputStream(createThumbnailPath)) {
                     outputStream.write(response.body().bytes());

                     return Uri.fromFile( createThumbnailPath ).toString();
                 }

            } else {
                // Handle unsuccessful response
                return null;
            }
        }
    }

    // file:// to /storage
    public String getFilePathFromUri(Context context, Uri fileUri) {
        String filePath = null;
        if (fileUri.getScheme().equals("content")) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } else if (fileUri.getScheme().equals("file")) {
            filePath = fileUri.getPath();
        }
        return filePath;
    }


    public static Uri convertFileUriToContentUri(Context context, Uri fileUri) {
        String filePath = fileUri.getPath();
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media._ID };
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = { filePath };
        String sortOrder = null;

        Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(projection[0]);
            long imageId = cursor.getLong(columnIndex);
            cursor.close();
            return ContentUris.withAppendedId(contentUri, imageId);
        }

        // If cursor is null or no matching records found, return null
        return null;
    }

    // Function to format duration in milliseconds to "mm:ss" format
    public static String formatDuration(int durationInMillis) {
        int seconds = durationInMillis / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    // Function to format duration to "mm:ss" to milliseconds format
    public static int parseDuration(String durationString) {
        try {
            String[] parts = durationString.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);

            return (minutes * 60 + seconds) * 1000;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace(); // Handle parsing errors if needed
            return 0; // Default value if parsing fails
        }
    }


    public static String getVideoDuration(Uri videoUri, Context context){
        MediaPlayer mediaPlayer = new MediaPlayer();
        String videoDuration;
        try {
            mediaPlayer.setDataSource(context, videoUri);   //  content://media/external/video/media/1000142346
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            videoDuration = FileUtils.formatDuration(duration);

            return videoDuration;

        } catch (IOException e) {
//            Toast.makeText(context, "An error " + e.getMessage(), Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

    }

    public static String getMediaDuration(File file) {
        String mediaDuration = "0";

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getAbsolutePath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null) {
                int duration = Integer.parseInt(durationStr);
                mediaDuration = FileUtils.formatDuration(duration);

                return mediaDuration;
            }
            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaDuration;
    }

    public static void openDocumentFromUrl(Context mContext, String documentUri) throws URISyntaxException {
        System.out.println("what is doc ur " + documentUri);
        if(documentUri != null){
            if(documentUri.startsWith("file:/") || documentUri.startsWith("/storage")) {
                File docFile;
                if(documentUri.startsWith("/storage")) {
                    docFile = new File(documentUri);
                } else {    // starts with file/
                    docFile = new File(new URI(documentUri));
                }

                Uri docContentUri = FileProvider.getUriForFile(mContext, "com.pixel.chatapp.fileprovider", docFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String mimeType = mContext.getContentResolver().getType(docContentUri); // get the type -> pdf, docx jpeg etc
                intent.setDataAndType(docContentUri, mimeType);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
//                    mContext.startActivity(Intent.createChooser(intent, "Open Document with"));
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Handle no PDF viewer installed case
                    Toast.makeText(mContext, "No document viewer installed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.notSentYet), Toast.LENGTH_SHORT).show();
            }
        } else Toast.makeText(mContext, mContext.getString(R.string.corrupt), Toast.LENGTH_SHORT).show();

    }

    public static String getFileCreationDate(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                long lastModified = file.lastModified();

                return formatFileCreationDate( new Date(lastModified) );
            }
        }
        return null;
    }

    public static String formatFileCreationDate(Date creationDate) {
        if (creationDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return dateFormat.format(creationDate);
        } else {
            return "Unknown";
        }
    }

}
