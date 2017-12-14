package com.example.kelly.weatherapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPref;
    String beginningTime;
    String endTime;
    Date beginningDate;
    Date endDate;
    int beginning;
    int end;

    @Override
    public void onReceive(Context c, Intent i) {
        sharedPref = c.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);

        // Only send notification if user wants it after alarms
        if (sharedPref.getBoolean("notifications_alarms", false)) {

            beginningTime = sharedPref.getString("notifications_beginning_time", "12:00AM");
            endTime = sharedPref.getString("notifications_end_time", "12:00PM");

            // Format the times the user wants alarm notifications between so we can compare with current time
            beginningTime = beginningTime.replace(":", "");
            if (beginningTime.contains("PM")) {
                beginningTime = beginningTime.replace("PM", "");
                beginning = Integer.parseInt(beginningTime.trim());
                if (beginning < 1200) {
                    beginning = beginning + 1200;
                }
            }
            else if (beginningTime.contains("AM")) {
                beginningTime = beginningTime.replace("AM", "");
                beginning = Integer.parseInt(beginningTime.trim());
                if (beginning > 1159) {
                    beginning = beginning - 1200;
                }
            }

            endTime = endTime.replace(":", "");
            if (endTime.contains("PM")) {
                endTime = endTime.replace("PM", "");
                end = Integer.parseInt(endTime.trim());
                if (end < 1200) {
                    end = end + 1200;
                }
            }
            else if (endTime.contains("AM")) {
                endTime = endTime.replace("AM", "");
                end = Integer.parseInt(endTime.trim());
                if (end > 1159) {
                    end = end - 1200;
                }
            }

            // Figure out the current time
            Date date = new Date();   // given date
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date
            int now = (calendar.get(Calendar.HOUR_OF_DAY) * 100) + calendar.get(Calendar.MINUTE);

            // Only show notification if it is currently between the times the user set
            if (now >= beginning && now <= end) {
                Intent notif = new Intent(c, NotificationService.class);
                c.startService(notif);
            }
        }
    }
}
