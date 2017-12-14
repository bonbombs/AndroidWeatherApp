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
        switch (v.getId()) {
            case R.id.never:
                sendResult(Activity.RESULT_OK, 0);
                break;
            case R.id.one_hour:
                sendResult(Activity.RESULT_OK, 1);
                break;
            case R.id.three_hours:
                sendResult(Activity.RESULT_OK, 3);
                break;
            case R.id.six_hours:
                sendResult(Activity.RESULT_OK, 6);
                break;
            case R.id.twelve_hours:
                sendResult(Activity.RESULT_OK, 12);
                break;
            case R.id.twentyfour_hours:
                sendResult(Activity.RESULT_OK, 24);
                break;
            default:
                sendResult(Activity.RESULT_OK, 0);
        }
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
