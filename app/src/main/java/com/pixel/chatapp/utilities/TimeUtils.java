package com.pixel.chatapp.utilities;

import android.content.Context;

import com.pixel.chatapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    // Helper method to format date 9:30 PM
    public static String getFormattedTime(long timestamp) {
        Date date = new Date(timestamp);
        DateFormat formatter = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return formatter.format(date).toLowerCase();
    }

    // Helper method to format date in "MMM dd, yyyy" format - Jan 15, 1995
    public static String getShortFormattedDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getFullFormattedDate(Date date) {      //January 7, 2022
        DateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    // Helper method to calculate the difference in days
    public static int calculateDayDifference(Date currentDate, Date messageDate) {
        long differenceInMillis = currentDate.getTime() - messageDate.getTime();
        return (int) (differenceInMillis / (1000 * 60 * 60 * 24)); // Days difference
    }

    // Calculate the difference in months between two dates
    public static int calculateMonthDifference(Date currentDate, Date lastMessageDate) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        Calendar lastMessageCalendar = Calendar.getInstance();
        lastMessageCalendar.setTime(lastMessageDate);

        int yearDifference = currentCalendar.get(Calendar.YEAR) - lastMessageCalendar.get(Calendar.YEAR);
        int monthDifference = currentCalendar.get(Calendar.MONTH) - lastMessageCalendar.get(Calendar.MONTH);

        return yearDifference * 12 + monthDifference;  // Convert year difference to months and add month difference
    }

    // Helper method to get last seen message based on day difference
    public static String getUserLastSeenOnChat(long lastSeenTimestamp, Context context) {

        if(lastSeenTimestamp == -1) return context.getString(R.string.app_name);

        if (lastSeenTimestamp == 1) return context.getString(R.string.online);  // online

        if(lastSeenTimestamp == 2) return context.getString(R.string.unavailable);  // premium user

        Date lastSeenDate = new Date(lastSeenTimestamp);
        String lastDateString = getFullFormattedDate(lastSeenDate);
        String formattedTime = getFormattedTime(lastSeenTimestamp);
        int dayDifference = calculateDayDifference(new Date(), lastSeenDate);

        if (dayDifference == 0) return String.format(context.getString(R.string.seen_today), formattedTime.toLowerCase());
        if (dayDifference == 1) return String.format(context.getString(R.string.seen_yesterday), formattedTime.toLowerCase());
        if (dayDifference < 7) return String.format(context.getString(R.string.seen_days_ago), dayDifference, formattedTime.toLowerCase());
        if (dayDifference < 14) return context.getString(R.string.seen_last_week);
        if (dayDifference < 21) return context.getString(R.string.seen_last_2_weeks);
        if (dayDifference < 28) return context.getString(R.string.seen_last_3_weeks);
        if (dayDifference < 60) return context.getString(R.string.seen_a_month_ago);

        return String.format(context.getString(R.string.seen_on_date), lastDateString);

    }



    // Check if two dates are in the same year
    public static boolean isSameYear(Date current, Date past) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar pastCalendar = Calendar.getInstance();
        currentCalendar.setTime(current);
        pastCalendar.setTime(past);

        return currentCalendar.get(Calendar.YEAR) == pastCalendar.get(Calendar.YEAR);
    }

    // Check if two dates are in the same month
    public static boolean isSameMonth(Date current, Date past) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar pastCalendar = Calendar.getInstance();
        currentCalendar.setTime(current);
        pastCalendar.setTime(past);

        return currentCalendar.get(Calendar.MONTH) == pastCalendar.get(Calendar.MONTH);
    }


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
        if (currentDate != null) return !currentDate.equals(timestampDate);

        return false;
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
