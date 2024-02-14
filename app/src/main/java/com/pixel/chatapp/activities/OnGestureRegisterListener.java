package com.pixel.chatapp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

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
//    public abstract void animateSwipeRight(View view);
//    public abstract void animateSwipeLeft(final View view);
    public abstract void onClick(View view);
    public abstract boolean onLongClick(View view);

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        // Constants for animation
        private static final int MOVE_THRESHOLD = -100;
        private static final int ANIMATION_DURATION = 300;

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
//                            onSwipeRight(view);
                            animateSwipeRight(view);

                        } else {
//                            onSwipeLeft(view);
                            animateSwipeLeft(view);

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

        // Animation for swiping right
        private void animateSwipeRight(final View view) {
            ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", -MOVE_THRESHOLD);
            translationX.setDuration(ANIMATION_DURATION);
            translationX.setInterpolator(new DecelerateInterpolator());
            translationX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onSwipeRight(view);
                    resetTranslation(view);
                }
            });
            translationX.start();
        }

        // Animation for swiping left
        private void animateSwipeLeft(final View view) {
            ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", MOVE_THRESHOLD);
            translationX.setDuration(ANIMATION_DURATION);
            translationX.setInterpolator(new DecelerateInterpolator());
            translationX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onSwipeLeft(view);
                    resetTranslation(view);
                }
            });
            translationX.start();
        }


    }


    // Reset translation after animation
    private void resetTranslation(View view) {
        view.setTranslationX(0);
    }
}

// usage code

// swipe method for reply
//        OnGestureRegisterListener onGestureRegisterListener = new OnGestureRegisterListener(mContext) {
//            public void onSwipeRight(View view) {
//                if(modelList.get(chatPosition).getMsgStatus() == 700033){
//                    Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    holder.constraintChatTop.setVisibility(View.GONE);  // close option menu
//                    // call method in MainActivity and set up the details
//                    fragmentListener.onEditOrReplyMessage(modelUser, "reply",
//                            "replying...", R.drawable.reply, modelUser.getFrom(), 1);
//
//                }
//            }
//            public void onSwipeLeft(View view) {
//                if(modelList.get(chatPosition).getMsgStatus() == 700033){
//                    Toast.makeText(mContext, "Check your network connection", Toast.LENGTH_SHORT).show();
//                }
//                holder.constraintChatTop.setVisibility(View.GONE);  // close option menu
//                // call method in MainActivity and set up the details
//                fragmentListener.onEditOrReplyMessage(modelUser, "reply",
//                        "replying...", R.drawable.reply, modelUser.getFrom(), 1);
//            }
//
//
//            public void onClick(View view) {
//
//            }
//            public boolean onLongClick(View view) {
//                // Do something
//                return true;
//            }
//        };
//
//        holder.imageViewOptions.setOnTouchListener(onGestureRegisterListener);   // swipe position
//        holder.textViewShowMsg.setOnTouchListener(onGestureRegisterListener);   // swipe position
//        holder.constraintMsgContainer.setOnTouchListener(onGestureRegisterListener);   // swipe position
//        holder.linearLayoutReplyBox.setOnTouchListener(onGestureRegisterListener);