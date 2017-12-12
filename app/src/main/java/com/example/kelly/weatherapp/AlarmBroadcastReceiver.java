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

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPref;
    String beginningTime;
    String endTime;
    Date beginningDate;
    Date endDate;

    @Override
    public void onReceive(Context c, Intent i) {
        Log.d("Intent_broadcast", i.getAction());

        /*sharedPref = c.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);

        beginningTime = sharedPref.getString("notifications_beginning_time", "12:00AM");
        endTime = sharedPref.getString("notifications_end_time", "12:00PM");

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mmaa");
        try {
            beginningDate = dateFormat.parse(beginningTime);
        } catch (ParseException e){}
        try {
            endDate = dateFormat.parse(endTime);
        } catch (ParseException e){}

        Date now = Calendar.getInstance().getTime();

        // Only show notification if its between the times the user set
        if (now.before(beginningDate) || now.after(endDate)) {*/
            Log.d("AHHHHHHHHH", "");
            Intent notif = new Intent(c, NotificationService.class);
            c.startService(notif);
       // }
    }
}
