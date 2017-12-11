package com.example.kelly.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PreferencesActivity extends AppCompatActivity {

    SharedPreferences sharedPref;
    static final int CHOOSE_NOTIFY_INTERVAL = 1;

    // User settings values
    int timeInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
    }

    public void onClickWhenToNotify(View v) {
        Intent i = new Intent(this, TimeListActivity.class);
        startActivityForResult(i, CHOOSE_NOTIFY_INTERVAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_NOTIFY_INTERVAL) {
            if (resultCode == RESULT_OK) {
                timeInterval = (int) data.getSerializableExtra("timeInterval");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO: Save everything to SharedPreferences
    }
}
