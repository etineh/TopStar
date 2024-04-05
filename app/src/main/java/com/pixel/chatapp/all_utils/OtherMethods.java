package com.pixel.chatapp.all_utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class OtherMethods {

    public static void animateVisibility(ConstraintLayout containerAnim, LinearLayout linearLayout) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(containerAnim, "alpha", 0f, 1f);
        animator.setDuration(500); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if(containerAnim != null){
                    containerAnim.setVisibility(View.VISIBLE);
                } else {
                    linearLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    public static void animateFadeOut(final ConstraintLayout containerAnim) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(containerAnim, "alpha", 1f, 0f);
        animator.setDuration(500); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                containerAnim.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    public static void fadeInSeekBar(ConstraintLayout seekbarContainer) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(seekbarContainer, "alpha", 0f, 1f);
        animator.setDuration(500); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                seekbarContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    public static void fadeOutSeekBar(final ConstraintLayout seekbarContainer) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(seekbarContainer, "alpha", 1f, 0f);
        animator.setDuration(2000); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                seekbarContainer.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    public static void fadeInRecyclerview(RecyclerView recyclerView) {
        if(recyclerView != null){
            // Animate the alpha property for fading effect
            ObjectAnimator animator = ObjectAnimator.ofFloat(recyclerView, "alpha", 0f, 1f);
            animator.setDuration(500); // Adjust duration as needed
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
            animator.start();
        }
    }

    public static void fadeInFastRecyclerview(RecyclerView recyclerView) {
        if(recyclerView != null){
            // Animate the alpha property for fading effect
            ObjectAnimator animator = ObjectAnimator.ofFloat(recyclerView, "alpha", 0f, 1f);
            animator.setDuration(100); // Adjust duration as needed
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
            animator.start();
        }
    }

    public static void fadeOutRecyclerview(final RecyclerView recyclerView) {
        if(recyclerView != null){
            // Animate the alpha property for fading effect
            ObjectAnimator animator = ObjectAnimator.ofFloat(recyclerView, "alpha", 1f, 0f);
            animator.setDuration(2000); // Adjust duration as needed
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    recyclerView.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
    }


}
