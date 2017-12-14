package com.example.kelly.weatherapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.awareness.state.Weather;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Kelly on 12/10/2017.
 *
 * Serves as the recommend/alerter of events based on upcoming weather, user preferences, etc.
 */

public class RecommendationService {
    private final int RECOMMENDATION_SERVICE_LISTENERID = 100;

    private static RecommendationService mInstance = null;
    private DatabaseReference mDatabase;
    private Activity mContext;

    private final float mSeverityThreshold = 0.3f;
    private int MAX_3HOURS_LOOK_AHEAD = 2;                  // Openweather gives us forecasts every 3 hrs so we're measuring in iterations
    private Dictionary<Integer, Float> mSeverityMap;
    private WeatherData mCurrentWeather;
    private List<WeatherData> mHourlyWeather;
    private List<String> mRecommendationEvaluator;
    private Map<String, Integer> mClothingMap;
    private UserTemperature mTemperaturePreference;

    private List<String> DummyWardrobeRecommendations;
    private Dictionary<Integer, OnWardrobeUpdateDataReceivedListener> onWardrobeUpdateDataDataReceivedListeners;

    //TODO: Class or data structure for wardrobe
    //TODO: Class for temp prefs

    private RecommendationService() {
        onWardrobeUpdateDataDataReceivedListeners = new Hashtable<>();
        mRecommendationEvaluator = new ArrayList<>();

        ConstructWeatherListeners();
        ConstructSeverityMap();

        DummyWardrobeRecommendations = new ArrayList<>();
        DummyWardrobeRecommendations.add("Hat");
        DummyWardrobeRecommendations.add("Socks");
        DummyWardrobeRecommendations.add("Boots");
    }

    private void ConstructWeatherListeners() {
        WeatherClient.addOnCurrentWeatherDataReceivedListener(RECOMMENDATION_SERVICE_LISTENERID, new WeatherClient.OnCurrentWeatherDataReceivedListener() {
            @Override
            public void onDataLoaded(WeatherData data) {
                mCurrentWeather = data;
                UpdateRecommendations();
            }

            @Override
            public void onDataError(Throwable t) {

            }
        });

        WeatherClient.addOnHourlyWeatherDataReceivedListener(RECOMMENDATION_SERVICE_LISTENERID, new WeatherClient.OnHourlyWeatherDataReceivedListener() {
            @Override
            public void onDataLoaded(List<WeatherData> data) {
                mHourlyWeather = data;
                UpdateRecommendations();
            }

            @Override
            public void onDataError(Throwable t) {

            }
        });
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
        // Get Firebase database reference using google-services.json file
        mDatabase = FirebaseDatabase.getInstance().getReference();
        List<Condition> conditions = data.getConditionsData();
        float total = 0f;
        for (Condition c : conditions) {
            total += mSeverityMap.get(c.State);
        }
        return total;
    }

    public void DetermineRecommendations(Activity context) {
        // TODO
        mHourlyWeather = WeatherClient.GetHourlyWeather();
        mCurrentWeather = WeatherClient.GetCurrentWeather();

        mContext = context;
        helper_GetTemperaturePreferences();
    }

    private void UpdateRecommendations() {
        // TODO Temp Pref Section
        // (1) Check current temp to see if it's past thresholds

        // (2) Else if weather conditions in next N hours has drastically changed, alert
        //      Check current weather heuristic
        // TODO Wardrobe Rec Section
        // Always recommend a list, unless they never checked off anything
        if (mCurrentWeather != null && mHourlyWeather != null) {
            float currentSeverity = CalculateWeatherSeverity(mCurrentWeather);
            for (int i = 0; i < MAX_3HOURS_LOOK_AHEAD; i++) {
                WeatherData data = mHourlyWeather.get(i);
                float lookAhead = CalculateWeatherSeverity(data);
                if (lookAhead - currentSeverity > mSeverityThreshold) {
                    //TODO setup alert w/ details on this hour's forecast
                }
            }
        }

        // (3) Else if weather temps in next N hours is past threshold, alert
    }

    private void helper_GetTemperaturePreferences() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        SharedPreferences prefs = mContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("uuid", "");
        if (!userId.isEmpty()) {
            mDatabase.child("temperature").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();
                    mTemperaturePreference = new UserTemperature();
                    while(items.hasNext()) {
                        DataSnapshot item = items.next();
                        Log.e("RecommendationService", "onDataChange: " + item.getKey() + " " + item.getValue());
                        if (item.getKey().equals("isHot")) {
                            mTemperaturePreference.isHot = item.getValue(Integer.class);
                        }
                        if (item.getKey().equals("isCold")) {
                            mTemperaturePreference.isCold = item.getValue(Integer.class);
                        }
                    }
                    helper_GetWardrobeRecommendation();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void helper_GetWardrobeRecommendation() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        SharedPreferences prefs = mContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("uuid", "");
        if (!userId.isEmpty()) {
            mDatabase.child("clothing").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mClothingMap = new HashMap<>();
                    Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();
                    while(items.hasNext()) {
                        DataSnapshot item = items.next();
                        if (item.getValue(Boolean.class)) {
                            //Log.e("RecommendationService", "onDataChange: " + item.getKey() + " " + item.getValue());
                            mClothingMap.put(item.getKey(), 0);
                            List<Integer> conditions = GetConditionsFromClothing(item.getKey());
                            for (Condition c : mCurrentWeather.getConditionsData()) {
                                if (conditions.contains(c.State)) {
                                    mClothingMap.put(item.getKey(), mClothingMap.get(item.getKey()) + 1);
                                }
                            }
                            List<String> clothes = GetClothingFromTemperature(mTemperaturePreference);
                            if (clothes.contains(item.getKey())) {
                                mClothingMap.put(item.getKey(), mClothingMap.get(item.getKey()) + 1);
                            }
                        }
                    }
                    mClothingMap = sortByValue(mClothingMap);
                    EmitOnWardrobeUpdateSuccess(mClothingMap.keySet(), mClothingMap);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    public List<String> GetClothingFromTemperature (UserTemperature tempPrefs) {
        List<String> result = new ArrayList<>();

        if (mCurrentWeather.getTemperature(Weather.FAHRENHEIT) > tempPrefs.isHot
                || mCurrentWeather.getTemperature(Weather.FAHRENHEIT) > 80) {
            result = Arrays.asList(new String[] {
                    "isHat",
                    "isSunglasses",
                    "isTshirt",
                    "isShorts",
                    "isSandals"
            });
        }
        else if (mCurrentWeather.getTemperature(Weather.FAHRENHEIT) < tempPrefs.isCold
                || mCurrentWeather.getTemperature(Weather.FAHRENHEIT) < 32) {
            result = Arrays.asList(new String[] {
                    "isWinterCoat",
                    "isScarf",
                    "isBeanie",
                    "isGloves"
            });
        }

        return result;
    }

    public List<Integer> GetConditionsFromClothing (String clothing) {
        List<Integer> result = new ArrayList<>();
        if (clothing.equals("isGloves")) {
            result.add(Weather.CONDITION_ICY);
            result.add(Weather.CONDITION_SNOWY);
        }
        else if (clothing.equals("isScarf")) {
            result.add(Weather.CONDITION_ICY);
            result.add(Weather.CONDITION_SNOWY);
            result.add(Weather.CONDITION_WINDY);
        }
        else if (clothing.equals("isWinterCoat")) {
            result.add(Weather.CONDITION_ICY);
            result.add(Weather.CONDITION_SNOWY);
            result.add(Weather.CONDITION_WINDY);
        }
        else if (clothing.equals("isRainJacket")) {
            result.add(Weather.CONDITION_WINDY);
            result.add(Weather.CONDITION_RAINY);
            result.add(Weather.CONDITION_STORMY);
        }
        else if (clothing.equals("isBeanie")) {
            result.add(Weather.CONDITION_ICY);
            result.add(Weather.CONDITION_SNOWY);
            result.add(Weather.CONDITION_WINDY);
        }
        else if (clothing.equals("isUmbrella")) {
            result.add(Weather.CONDITION_RAINY);
            result.add(Weather.CONDITION_STORMY);
        }
        else if (clothing.equals("isRainBoots")) {
            result.add(Weather.CONDITION_RAINY);
            result.add(Weather.CONDITION_STORMY);
        }
        else if (clothing.equals("isSnowBoots")) {
            result.add(Weather.CONDITION_ICY);
            result.add(Weather.CONDITION_SNOWY);
        }
        return result;
    }


    public static List<String> GetRecommendedWardrobe() {
        // TODO Return value subject to change
        return GetInstance().DummyWardrobeRecommendations;
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

    private void EmitOnWardrobeUpdateSuccess(Set<String> newData, Map<String, Integer> map) {
        Enumeration<OnWardrobeUpdateDataReceivedListener> listeners = onWardrobeUpdateDataDataReceivedListeners.elements();
        while(listeners.hasMoreElements()) {
            listeners.nextElement().onDataSuccess(newData, map);
        }
    }

    public static void addOnWardrobeUpdateDataReceivedListener(int id, OnWardrobeUpdateDataReceivedListener listener) {
        if (GetInstance().onWardrobeUpdateDataDataReceivedListeners.get(id) == null)
            GetInstance().onWardrobeUpdateDataDataReceivedListeners.put(id, listener);
    }

    public interface OnWardrobeUpdateDataReceivedListener {
        void onDataSuccess(Set<String> data, Map<String, Integer> map);
    }

    // https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
