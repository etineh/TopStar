package com.pixel.chatapp.home;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnGestureRegisterListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private View view;

    public OnGestureRegisterListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.view = view;
        return gestureDetector.onTouchEvent(event);
    }

    public abstract void onSwipeRight(View view);
    public abstract void onSwipeLeft(View view);
//    public abstract void onSwipeBottom(View view);
//    public abstract void onSwipeTop(View view);
    public abstract void onClick(View view);
    public abstract boolean onLongClick(View view);

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick(view);
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick(view);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(view);
                        } else {
                            onSwipeLeft(view);
                        }
                        result = true;
                    }
                }
//                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                    if (diffY > 0) {
//                        onSwipeBottom(view);
//                    } else {
//                        onSwipeTop(view);
//                    }
//                    result = true;
//                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

    }
}

// usage code

// swipe method for reply
//        OnGestureRegisterListener onGestureRegisterListener = new OnGestureRegisterListener(mContext) {
//            public void onSwipeRight(View view) {
//                if(modelList.get(pos).getMsgStatus() == 700033){
//                    Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    editOrReplyIV.setImageResource(R.drawable.reply);   // set reply icon
//                    editAndReply("reply", modelList.get(pos).getIdKey(), editTextMsg, holder, pos, modelList.get(pos).getFrom());
//                }
//            }
//            public void onSwipeLeft(View view) {
//                if(modelList.get(pos).getMsgStatus() == 700033){
//                    Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    editOrReplyIV.setImageResource(R.drawable.reply);   // set reply icon
//                    editAndReply("reply", modelList.get(pos).getIdKey(), editTextMsg, holder, pos, modelList.get(pos).getFrom());
//                }
//            }
//            public void onClick(View view) {
// Do something
//            }
//            public boolean onLongClick(View view) {
//                // Do something
//                return true;
//            }
//        };
//
//        holder.imageViewOptions.setOnTouchListener(onGestureRegisterListener);   // swipe position
//        holder.textViewShowMsg.setOnTouchListener(onGestureRegisterListener);   // swipe position
//        holder.constrSlide.setOnTouchListener(onGestureRegisterListener);   // swipe position