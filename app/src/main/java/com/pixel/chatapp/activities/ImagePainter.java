package com.pixel.chatapp.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.pixel.chatapp.SendImageActivity;

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
    private static List<Path> paths;
    private static List<Bitmap> bitmapList;

    private List<List<PointF>> pointsList;
    Context context;

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
        paths = new ArrayList<>();
        pointsList = new ArrayList<>();
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
        paths.add(new Path(path));

        Uri uri = SendImageActivity.savePaintPhotoUri(context, SendImageActivity.paintedBitmap);
        Bitmap uriToBitmap;
        try {
            uriToBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            bitmapList.add(uriToBitmap);
            SendImageActivity.tempUri.add(uri);
            System.out.println("what is: " + path + " and " + bitmap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<PointF> points = new ArrayList<>();
        points.add(new PointF(x, y));
        pointsList.add(points);
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
            pointsList.get(pointsList.size() - 1).add(new PointF(x, y));
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

    public void undo(ImageView view, Bitmap bitmap) {
        if (!paths.isEmpty()) {
            // Redraw the bitmap
//            canvas.drawBitmap(bitmap, 0, 0, paint);
            // Remove the last drawn path
            bitmapList.remove(bitmapList.size() - 1);
////            pointsList.remove(pointsList.size() - 1);
            bitmap = bitmapList.get(bitmapList.size() - 1);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            System.out.println("what is b: " + bitmap);
            // Redraw the remaining paths
//            for (Bitmap previousBitmap : bitmapList) {
////                canvas.drawPath(p, paint);
//                view.setImageBitmap(bitmap);
//            }
            // Invalidate the view to trigger redraw
            view.invalidate();
        }
    }

    public void reset(ImageView imageView, Bitmap currentBitmap) {
        canvas.drawBitmap(currentBitmap, 0, 0, paint);
        imageView.invalidate();
    }
}


