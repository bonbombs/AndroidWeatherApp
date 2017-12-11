package com.example.kelly.weatherapp;

import com.google.android.gms.awareness.state.Weather;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Kelly on 12/10/2017.
 *
 * Serves as the recommend/alerter of events based on upcoming weather, user preferences, etc.
 */

public class RecommendationService {
    private static RecommendationService mInstance = null;

    private final float mSeverityThreshold = 0.3f;
    private int MAX_3HOURS_LOOK_AHEAD = 2;                  // Openweather gives us forecasts every 3 hrs so we're measuring in iterations
    private Dictionary<Integer, Float> mSeverityMap;

    //TODO: Class or data structure for wardrobe
    //TODO: Class for temp prefs

    private RecommendationService() {
        ConstructSeverityMap();
    }

    private void ConstructSeverityMap() {
        // Values subject to change
        // TODO: Add more values since API has more than what Google has provided
        mSeverityMap = new Hashtable<>();
        mSeverityMap.put(Weather.CONDITION_CLEAR, 0f);
        mSeverityMap.put(Weather.CONDITION_CLOUDY, 0.1f);
        mSeverityMap.put(Weather.CONDITION_FOGGY, 0.3f);
        mSeverityMap.put(Weather.CONDITION_HAZY, 0.3f);
        mSeverityMap.put(Weather.CONDITION_ICY, 0.75f);
        mSeverityMap.put(Weather.CONDITION_RAINY, 0.5f);
        mSeverityMap.put(Weather.CONDITION_SNOWY, 0.6f);
        mSeverityMap.put(Weather.CONDITION_STORMY, 0.75f);
        mSeverityMap.put(Weather.CONDITION_WINDY, 0.4f);
    }

    private float CalculateWeatherSeverity(WeatherData data) {
        List<Condition> conditions = data.getConditionsData();
        float total = 0f;
        for (Condition c : conditions) {
            total += mSeverityMap.get(c.State);
        }
        return total;
    }

    private void DetermineRecommendations() {
        // TODO
        List<WeatherData> hourlyWeather = WeatherClient.GetHourlyWeather();
        WeatherData currentWeather = WeatherClient.GetCurrentWeather();
        // TODO Wardrobe Rec Section
        // Always recommend a list, unless they never checked off anything
        // TODO Temp Pref Section
        // (1) Check current temp to see if it's past thresholds

        // (2) Else if weather conditions in next N hours has drastically changed, alert
        //      Check current weather heuristic
        float currentSeverity = CalculateWeatherSeverity(currentWeather);
        for (int i = 0; i < MAX_3HOURS_LOOK_AHEAD; i++) {
            WeatherData data = hourlyWeather.get(i);
            float lookAhead = CalculateWeatherSeverity(data);
            if (lookAhead- currentSeverity > mSeverityThreshold) {
                //TODO setup alert w/ details on this hour's forecast
            }
        }

        // (3) Else if weather temps in next N hours is past threshold, alert
    }


    public static List<String> GetRecommendedWardrobe() {
        // TODO Return value subject to change
        return new ArrayList<>();
    }

    public static String GetAlert() {
        return "";
    }

    public static void UpdateTemperaturePreferences() {
        //TODO
    }

    public static void UpdateWardrobePreferences() {
        //TODO
    }

    public static RecommendationService GetInstance() {
        if(mInstance == null)
        {
            mInstance = new RecommendationService();
        }
        return mInstance;
    }
}
