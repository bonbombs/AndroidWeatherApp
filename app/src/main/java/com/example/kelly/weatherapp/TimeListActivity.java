package com.example.kelly.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TimeListActivity extends AppCompatActivity {

    public static final String EXTRA_NOTIFY_TIME = "notificationTimeInterval";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_list);
    }

    public void onClickTime(View v) {
        // TODO: Change time based on which button was clicked
        sendResult(Activity.RESULT_OK, 3);
    }

    private void sendResult(int resultCode, int time) {
        // Set the time the user chose
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NOTIFY_TIME, time);
        setResult(resultCode, intent);

        // Go back to previous activity
        onBackPressed();
    }
}
