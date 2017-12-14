package com.example.kelly.weatherapp;

import com.google.android.gms.awareness.state.Weather;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

/**
 * Created by Kelly on 12/4/2017.
 *
 * Class containing information about a particular weather condition
 */

public class Condition {

    public int State;
    public String ConditionName;
    public String ConditionDesc;
    public int mIconId = 0;

    public Condition(JSONObject json) throws IOException {
        int code = 0;
        String iconPath = "";
        try {
            ConditionName = json.getString("main");
            ConditionDesc = json.getString("description");
            code = json.getInt("id");
            iconPath = json.getString("icon");
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        // Based off of https://openweathermap.org/weather-conditions
        if (code >= 200 && code < 300) {
            State = Weather.CONDITION_STORMY;
        } else if (code >= 300 && code < 600) {
            State = Weather.CONDITION_RAINY;
        } else if (code >= 600 && code < 700) {
            State = Weather.CONDITION_SNOWY;
        } else if (code == 701 || code == 711 || code == 741) {
            State = Weather.CONDITION_HAZY;
        } else if (code == 721) {
            State = Weather.CONDITION_FOGGY;
        } else if (code == 800) {
            State = Weather.CONDITION_CLEAR;
        } else if (code > 800 && code < 810) {
            State = Weather.CONDITION_CLOUDY;
        } else {
            State = Weather.CONDITION_UNKNOWN;
        }

        if (iconPath.contains("01")) {
            mIconId = R.drawable.icons8_sun_50;
        }
        else if (iconPath.contains("02")) {
            mIconId = R.drawable.icons8_partly_cloudy_day_50;
        }
        else if (iconPath.contains("03") || iconPath.contains("04")) {
            mIconId = R.drawable.icons8_cloud_40;
        }
        else if (iconPath.contains("09")) {
            mIconId = R.drawable.icons8_rain_50;
        }
        else if (iconPath.contains("10")) {
            mIconId = R.drawable.icons8_rain_cloud_40;
        }
        else if (iconPath.contains("11")) {
            mIconId = R.drawable.icons8_storm_50;
        }
        else if (iconPath.contains("13")) {
            mIconId = R.drawable.icons8_snow_50;
        }
        else if (iconPath.contains("50")) {
            mIconId = R.drawable.icons8_haze_40;
        }
    }
}
