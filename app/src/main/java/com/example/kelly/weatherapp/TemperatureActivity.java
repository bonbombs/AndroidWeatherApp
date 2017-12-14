package com.example.kelly.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.awareness.state.Weather;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.UUID;

/**
 * Created by hollyn on 12/11/17.
 */

public class TemperatureActivity extends AppCompatActivity {

    private SeekBar hotSeekBar;
    private SeekBar coldSeekBar;
    private TextView hotProgress;
    private TextView coldProgress;
    private int mUnit;

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
        mSharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);

        mUnit = mSharedPreferences.getBoolean(getString(R.string.preference_unit), true) ? Weather.CELSIUS : Weather.FAHRENHEIT;

        hotSeekBar = (SeekBar)findViewById(R.id.seekBarHot); // make seekbar object
        hotProgress = (TextView)findViewById(R.id.hotBarProgress);
        doneButton = findViewById(R.id.button_done);
        coldSeekBar = (SeekBar)findViewById(R.id.seekBarCold); // make seekbar object
        coldProgress = (TextView)findViewById(R.id.coldBarProgress);


        String userId = mSharedPreferences.getString("uuid", "");
        if (userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
            mSharedPreferences.edit().putString("uuid", userId).apply();
            isHot = 40;
            isCold = 10;
            if (mUnit == Weather.CELSIUS) {
                float valHot = (isHot.intValue() - 32f) / 1.8f;
                float valCold = (isCold.intValue() - 32f) / 1.8f;
                hotProgress.setText(String.format("%.0f°C", valHot));
                coldProgress.setText(String.format("%.0f°C", valCold));
            }
            else {
                hotProgress.setText(String.format("%d°F", isHot));
                coldProgress.setText(String.format("%d°F", isCold));
            }
            hotSeekBar.setProgress(isHot - 40);
            coldSeekBar.setProgress(isCold - 10);
            hotSeekBar.setMax(50);
            coldSeekBar.setMax(50);
        }
        else {
            mDatabase.child("temperature").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();
                    UserTemperature data = dataSnapshot.getValue(UserTemperature.class);
                    while (items.hasNext()) {
                        DataSnapshot item = items.next();
                        Log.e("AAAAAAAAAAAA", "onDataChange: " + item.getKey() + " " + item.getValue());
                    }
                    if (data == null) return;
                    if (data.isCold == null) isCold = 10;
                    else isCold = data.isCold;
                    if (data.isHot == null) isHot = 40;
                    else isHot = data.isHot;
                    Log.e("AAAAAAAAAAAAAAAAA", "onDataChange: " + isHot + " " + isCold );
                    if (mUnit == Weather.CELSIUS) {
                        float valHot = (isHot.intValue() - 32f)  / 1.8f;
                        float valCold = (isCold.intValue() - 32f) / 1.8f;
                        hotProgress.setText(String.format("%.0f°C", valHot));
                        coldProgress.setText(String.format("%.0f°C", valCold));
                    }
                    else {
                        hotProgress.setText(String.format("%d°F", isHot));
                        coldProgress.setText(String.format("%d°F", isCold));
                    }
                    hotSeekBar.setProgress(isHot - 40);
                    coldSeekBar.setProgress(isCold - 10);
                    hotSeekBar.setMax(50);
                    coldSeekBar.setMax(50);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

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
                Integer finalProgress = progress + 40; // Calculates temperature user chose with offset
                // Sets the TextView of the temp the user chose
                if (mUnit == Weather.CELSIUS) {
                    float val = (progress + 40 - 32f) / 1.8f;
                    hotProgress.setText(String.format("%.0f°C", val));
                }
                else {
                    hotProgress.setText(String.format("%d°F", finalProgress));
                }
                seekBar.setMax(50);

                isHot = finalProgress; // Sets the variable which will be pushed to firebase
            }
        });

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
                Integer finalProgress = progress + 10; // Calculates temperature user chose with offset
                // Sets the TextView of the temp the user chose
                if (mUnit == Weather.CELSIUS) {
                    float val = (progress + 10 - 32f) / 1.8f;
                    coldProgress.setText(String.format("%.0f°C", val));
                }
                else {
                    coldProgress.setText(String.format("%d°F", finalProgress));
                }
                seekBar.setMax(50);

                isCold = finalProgress; // Sets the variable which will be pushed to firebase
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

                finish();
            }
        });
    }

}
