package com.pixel.chatapp.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static boolean isNotToday(long timestamp) {
        // Convert the timestamp to an Instant
        Instant instant = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            instant = Instant.ofEpochMilli(timestamp);
        }

        // Get the current date
        LocalDate currentDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDate = LocalDate.now();
        }

        // Get the date part of the timestamp
        LocalDate timestampDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            timestampDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        }

        // Check if the dates are different
        return !currentDate.equals(timestampDate);
    }


    public static int compareDays(long lastTime) {
        // Convert the timestamp to a Date object
        Date d = new Date(lastTime);

        // Get the current date
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);

        // Create Calendar instances for comparison
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        Calendar lastTimeCalendar = Calendar.getInstance();
        lastTimeCalendar.setTime(d);

        // Check if the message was sent today
        if (currentCalendar.get(Calendar.YEAR) == lastTimeCalendar.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) == lastTimeCalendar.get(Calendar.DAY_OF_YEAR)) {
            return 0;
        }
        // Check if the message was sent yesterday
        else if (currentCalendar.get(Calendar.YEAR) == lastTimeCalendar.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.DAY_OF_YEAR) - lastTimeCalendar.get(Calendar.DAY_OF_YEAR) == 1) {
            return 1;
        }
        // If the message was sent before yesterday
        else {
            return 2;
        }
    }


}
