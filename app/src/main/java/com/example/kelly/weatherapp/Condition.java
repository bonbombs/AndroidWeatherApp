package com.example.kelly.weatherapp;

import com.google.android.gms.awareness.state.Weather;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Created by Kelly on 12/4/2017.
 */

public class Condition {

    public int State;
    public String ConditionName;
    public String ConditionDesc;

    public Condition(JSONObject json) throws IOException {
        int code = 0;
        try {
            ConditionName = json.getString("main");
            ConditionDesc = json.getString("description");
            code = json.getInt("id");
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
    }
}
