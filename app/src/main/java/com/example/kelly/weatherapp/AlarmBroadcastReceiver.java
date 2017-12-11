package com.example.kelly.weatherapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context c, Intent i) {
        // TODO: Only send notification if user sets preferences to do so
        NotificationManager notif = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        // TODO: Get weather information instead
        Notification notify = new Notification.Builder
                (c.getApplicationContext()).setContentTitle("Notification").setContentText("Check the weather!").
                setContentTitle("Hello").setSmallIcon(R.drawable.sun).build();

        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
    }
}
