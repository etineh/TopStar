package com.pixel.chatapp.activities;

import static com.pixel.chatapp.home.MainActivity.sharing;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.pixel.chatapp.SendImageActivity;
import com.pixel.chatapp.home.MainActivity;

public class AppLifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private static int resumed;
    private static int paused;
    public static boolean isAppActive = false;
    public static Class<? extends Activity> currentActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // delay for 3 sec to get the initial state of the isAppActive before activity loads
        new Handler().postDelayed(() -> isAppActive = true, 3000);
        if (activity.getClass() != RedirectHome.class && activity.getClass() != SendImageActivity.class && activity.getClass() != MainActivity.class) {
            if(!sharing) currentActivity = activity.getClass();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // Handle activity started
//        currentActivity = activity.getClass();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        resumed++;
//        currentActivity = activity.getClass();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // don't change if activity is Home Direct or send
        if (activity.getClass() != RedirectHome.class && activity.getClass() != SendImageActivity.class) {
            if(!sharing) {  // this will prevent Main Activity from replacing the previous activity till user finish sharing the photo
                currentActivity = activity.getClass();
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // Handle activity stopped
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Handle activity save instance state
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Handle activity destroyed
    }

    public static boolean isAppInForeground() {
        return resumed > paused;
    }

    public static boolean isActivityInForeground(Class<? extends Activity> MainActivityClass, Class<? extends Activity> redirectClass, Context context) {
//        System.out.println( currentActivity.equals(activityClass) + " what is act " + currentActivity);
        return currentActivity.equals(MainActivityClass) || currentActivity.equals(redirectClass);
    }


}
