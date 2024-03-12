package com.pixel.chatapp.all_utils;

import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

public class AnimUtils {

    public static TranslateAnimation makeTransition(){
        // Define the animation
        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -0.2f); // Move up by 20% of the view's height

        animation.setDuration(1000); // Duration in milliseconds
        animation.setInterpolator(new OvershootInterpolator()); // Apply an overshoot interpolator for a spring-like effect

        return animation;
    }


}
