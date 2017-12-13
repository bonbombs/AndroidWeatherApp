package com.example.kelly.weatherapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by hollyn on 12/11/17.
 */

public class TemperatureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //updateValuesFromBundle(savedInstanceState);
        setContentView(R.layout.activity_temp);
    }

}
