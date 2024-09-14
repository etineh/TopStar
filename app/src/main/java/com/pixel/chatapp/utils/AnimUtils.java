package com.pixel.chatapp.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
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

    public static void fadeOutGone(final View view, final int duration) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
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

    public static void fadeOutInvisible(final View view, final int duration) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animator.setDuration(duration); // Adjust duration as needed
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
            }
        });
        animator.start();
    }

    public static void fadeInVisible(final View view, final int duration) {
        // Animate the alpha property for fading effect
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animator.setDuration(duration); // Adjust duration as needed
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

    public static void slideInFromBottom(final View view, final long duration) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0);
        animator.setDuration(duration);
        animator.start();
    }

    public static void slideInFromTop(final View view, final long duration) {
        // Start the view above the screen (negative value of its height)
        view.setTranslationY(-view.getHeight());
        view.setVisibility(View.VISIBLE);

        // Animate the translationY property to 0, so the view moves into the screen
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -view.getHeight(), 0);
        animator.setDuration(duration); // Adjust duration as needed
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

    public static void slideInFromLeft(final View view, final long duration) {
        // Start the view out of the screen (to the left)
        view.setTranslationX(-view.getWidth());
        view.setVisibility(View.VISIBLE);

        // Animate the translationX property to 0, so the view moves into the screen
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0);
        animator.setDuration(duration);
        animator.start();
    }

    public static void slideOutToLeft(final View view, final long duration) {
        // Animate the translationX property to the negative width of the view, so the view moves out of the screen to the left
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, -view.getWidth());
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

    public static void slideOutToBottom(final View view, final long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, view.getHeight());
        animator.setDuration(duration);
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

    public static void linearSlidingAnimation(final View headingTV, Long duration) {
        headingTV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                headingTV.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Set up the slide in animation (from outside left to original position)
                ObjectAnimator slideIn = ObjectAnimator.ofFloat(headingTV, "translationX", -headingTV.getWidth(), 0f);
                slideIn.setDuration(duration); // Duration in milliseconds

                // Set up the slide out animation (from original position to outside right)
                ObjectAnimator slideOut = ObjectAnimator.ofFloat(headingTV, "translationX", 0f, headingTV.getWidth());
                slideOut.setDuration(duration); // Duration in milliseconds

                // Set up the AnimatorSet to play the animations sequentially
                final AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playSequentially(slideIn, slideOut);

                // Make the animation loop indefinitely
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animatorSet.start();
                    }
                });

                // Start the animation
                animatorSet.start();
            }
        });
    }

}
