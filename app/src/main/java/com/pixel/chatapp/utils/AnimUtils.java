package com.pixel.chatapp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AnimUtils {

    public static void animateVisibility(ConstraintLayout containerAnim, View view) {
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
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    public static void animateView(View view) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(700); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    public static void fadeOut_500(final View view) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animator.setDuration(500); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    public static void fadeIn_300(final View view) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(300); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    public static void slideInFromBottom200s(final View view) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0);
        animator.setDuration(200);
        animator.start();
    }

    public static void slideInFromTop100s(final View view) {
        // Start the view above the screen (negative value of its height)
        view.setTranslationY(-view.getHeight());
        view.setVisibility(View.VISIBLE);

        // Animate the translationY property to 0, so the view moves into the screen
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -view.getHeight(), 0);
        animator.setDuration(100); // Adjust duration as needed
        animator.start();
    }

    public static void slideInFromRight(final View view, final long duration) {
        // Start the view out of the screen (to the right)
        view.setTranslationX(view.getWidth());
        view.setVisibility(View.VISIBLE);

        // Animate the translationX property to 0, so the view moves into the screen
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", view.getWidth(), 0);
        animator.setDuration(duration);
        animator.start();
    }

    public static void slideOutToRight(final View view, final long duration) {
        // Animate the translationX property to the width of the view, so the view moves out of the screen to the right
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, view.getWidth());
        animator.setDuration(duration); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    public static void slideOutToBottom(final View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, view.getHeight());
        animator.setDuration(100);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
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
