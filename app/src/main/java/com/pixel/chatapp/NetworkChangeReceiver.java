package com.pixel.chatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private FragmentListener fragmentListener;

    public NetworkChangeReceiver(FragmentListener listener) {
        this.fragmentListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            // Create a single-threaded executor (can be replaced with other executor types if needed)
            ExecutorService executor = Executors.newSingleThreadExecutor();

            // Execute the background task using the executor
            executor.execute(() -> {
                boolean isConnected = isNetworkConnected(context);

                // Update the UI with the result on the main (UI) thread
                new Handler(Looper.getMainLooper()).post(() -> {

                    // Notify the FragmentListener about the network status change
                    if (fragmentListener != null) {
                        fragmentListener.onNetworkStatusChanged(isConnected);
                    }
                });

                // Shutdown the executor when it's no longer needed
                executor.shutdown();
            });
        }
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                // Check internet reachability by pinging a known server (e.g., google.com)
                HttpURLConnection urlc = (HttpURLConnection) (new URL("https://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500); // Timeout in milliseconds
                urlc.connect();

                // Return true if the response code is 200 (HTTP OK)
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                // Error while checking internet access
                e.printStackTrace();
            }
        }

        return false; // No network connection or no internet access
    }

}
