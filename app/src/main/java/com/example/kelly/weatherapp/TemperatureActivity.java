package com.example.kelly.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * Created by hollyn on 12/11/17.
 */

public class TemperatureActivity extends AppCompatActivity {

    private SeekBar hotSeekBar;
    private SeekBar coldSeekBar;
    private TextView hotProgress;
    private TextView coldProgress;

    private Integer isHot;
    private Integer isCold;

    private Button doneButton;
    private DatabaseReference mDatabase;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //updateValuesFromBundle(savedInstanceState);
        setContentView(R.layout.activity_temp);


        // Get Firebase database reference using google-services.json file
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mSharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        hotSeekBar = (SeekBar)findViewById(R.id.seekBarHot); // make seekbar object
        hotProgress = (TextView)findViewById(R.id.hotBarProgress);
        hotProgress.setText("" + 40);
        isHot = 40; // Set to lowest temperature possible

        doneButton = findViewById(R.id.button_done);

        hotSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                hotProgress = (TextView)findViewById(R.id.hotBarProgress);
                Integer finalProgress = progress + 40;
                isHot = finalProgress;
                hotProgress.setText("" + finalProgress  + "°F");
                seekBar.setMax(100);
            }
        });

        coldSeekBar = (SeekBar)findViewById(R.id.seekBarCold); // make seekbar object
        coldProgress = (TextView)findViewById(R.id.coldBarProgress);
        coldProgress.setText("" + 10);
        isCold = 10; // Set to lowest temperature possible
        coldSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                Integer finalProgress = progress + 10;
                coldProgress.setText("" + finalProgress + "°F");
                seekBar.setMax(100);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UserTemperature userTempPref = new UserTemperature(isHot, isCold);

                String userId = mSharedPreferences.getString("uuid", "");
                if (userId.isEmpty()) {
                    userId = UUID.randomUUID().toString();
                    mSharedPreferences.edit().putString("uuid", userId).apply();
                }

                mDatabase.child("temperature").child(userId).setValue(userTempPref);

                // Intent to go to temperature preferences activity
                Intent intent = new Intent(getApplicationContext(), TemperatureActivity.class);
                startActivity(intent);
            }
        });
    }

}
