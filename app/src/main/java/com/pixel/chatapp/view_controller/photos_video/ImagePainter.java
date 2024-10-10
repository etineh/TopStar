package com.pixel.chatapp.view_controller.photos_video;

import static com.pixel.chatapp.view_controller.MainActivity.unusedPhotoShareRef;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.pixel.chatapp.R;
import com.pixel.chatapp.constants.K;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImagePainter {

    private static final float TOUCH_TOLERANCE = 4;

    private Bitmap bitmap;
    private static Canvas canvas;
    private static Paint paint;
    private static Path path;
    private float lastTouchX, lastTouchY;
    private static List<Bitmap> bitmapList;
    private boolean isFirstPaint = true;
    Context context;
    Gson gson = new Gson();
    public ImagePainter(Bitmap bitmap, Context context) {
        this.bitmap = bitmap;
        this.context = context;
        canvas = new Canvas(bitmap);
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK); // Default color is black
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10); // Default brush size
        path = new Path();
        bitmapList = new ArrayList<>();
    }

    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
    }

    private void touchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);

        if(!isFirstPaint){   // don't add bitmap to list and don't save image when user draw first time
            Uri uri = SendImageOrVideoActivity.savePaintPhotoUri(context, SendImageOrVideoActivity.paintedBitmap);
            Bitmap uriToBitmap;
            try {
                uriToBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                bitmapList.add(uriToBitmap);    // to enable displaying the the previous image/bitmap when user undo
                SendImageOrVideoActivity.tempUri.add(uri.toString()); // to enable deleting from app memory when done painting
                SendImageOrVideoActivity.allOldUriList.add(uri.toString());    // for sharePreference deleting in case goes offline
                SendImageOrVideoActivity.undoPaint_IV.setColorFilter(0);

                // save edited photo uri to sharePref via gson to enable app first launch onCreate to delete the photos in case photo was not deleted
                String json = gson.toJson(SendImageOrVideoActivity.allOldUriList);
                unusedPhotoShareRef.edit().putString(K.OLD_URI_LIST, json).apply();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        isFirstPaint = false;

        List<PointF> points = new ArrayList<>();
        points.add(new PointF(x, y));
        lastTouchX = x;
        lastTouchY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - lastTouchX);
        float dy = Math.abs(y - lastTouchY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            float cx = (x + lastTouchX) / 2;
            float cy = (y + lastTouchY) / 2;
            path.quadTo(lastTouchX, lastTouchY, cx, cy);
            lastTouchX = x;
            lastTouchY = y;
            canvas.drawPath(path, paint);
        }
    }

    private void touchUp() {
        path.lineTo(lastTouchX, lastTouchY);
        canvas.drawPath(path, paint);
        path.reset();
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setBrushSize(float size) {
        paint.setStrokeWidth(size);
    }

    public void undo(ImageView view, Bitmap currentBitmap) {
        if (!bitmapList.isEmpty()) {

            Bitmap bitmap = bitmapList.get(bitmapList.size() - 1);  // get the last edited photo
            bitmapList.remove(bitmapList.size() - 1);            // remove it from the bitmap list

            canvas.drawBitmap(bitmap, 0, 0, paint);       // display the last phone on the imageView

        } else {
            // change the undo button color
            int fadedOrangeColor = ContextCompat.getColor(context, R.color.transparent_orange);
            SendImageOrVideoActivity.undoPaint_IV.setColorFilter(fadedOrangeColor);
            // reset the image
            canvas.drawBitmap(currentBitmap, 0, 0, paint);
        }
        // Invalidate the view to trigger redraw
        view.invalidate();
    }

    public void reset(ImageView imageView, Bitmap currentBitmap) {
        canvas.drawBitmap(currentBitmap, 0, 0, paint);
        bitmapList.clear();
        imageView.invalidate();
    }

}


