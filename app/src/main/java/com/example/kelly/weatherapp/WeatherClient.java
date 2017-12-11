package com.example.kelly.weatherapp;

import android.util.Log;

import com.google.android.gms.awareness.state.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by Kelly on 12/4/2017.
 */

public class WeatherClient {

    enum RequestMode {
        CURRENT,
        HOURLY,
        PLACE
    }

    public static final int CURRENT_FORECAST = 0;
    public static final int HOURLY_FORECAST = 1;

    public final long TIME_BEFORE_REPOLL = 1000L * 60;

    private static String API_KEY = "";

    private static WeatherClient mInstance;
    private Request mRequest;
    private String mCurrentCity;
    private String mCurrentLocation;
    private OkHttpClient mClient;
    private WeatherConfig mCurrentConfig;;

    private WeatherData cachedCurrentWeather;
    private List<WeatherData> cachedHourlyWeather;
    private Dictionary<Integer, OnCurrentWeatherDataReceivedListener> onCurrentWeatherDataReceivedListeners;
    private Dictionary<Integer, OnHourlyWeatherDataReceivedListener> onHourlyWeatherDataReceivedListeners;

    private WeatherClient() {
        mClient = new OkHttpClient();
        mCurrentCity = "4930956";
        mCurrentConfig = new WeatherConfig(mCurrentCity);
        mRequest = UpdateRequest();
        mInstance = this;
        onCurrentWeatherDataReceivedListeners = new Hashtable<>();
        onHourlyWeatherDataReceivedListeners = new Hashtable<>();
        //TODO: NOT SECURE
        if (API_KEY.isEmpty())
            API_KEY = "2be70096614a5f7120ab4f91929341ef";
    }

    private Request UpdateRequest() {
        HttpUrl.Builder urlBuilder = null;
        switch (mCurrentConfig.getRequestMode()) {
            case HOURLY:
                urlBuilder = HttpUrl.parse("http://api.openweathermap.org/data/2.5/forecast").newBuilder();
                break;
            case CURRENT:
                urlBuilder = HttpUrl.parse("http://api.openweathermap.org/data/2.5/weather").newBuilder();
                break;
        }
        Log.d(TAG, "UpdateRequest: " + mCurrentConfig);
        urlBuilder.addQueryParameter("lat", String.valueOf(mCurrentConfig.getLatitude()));
        urlBuilder.addQueryParameter("lon", String.valueOf(mCurrentConfig.getLongitude()));
        switch (mCurrentConfig.getUnit()) {
            case Weather.CELSIUS:
                urlBuilder.addQueryParameter("units", "metric");
                break;
            case Weather.FAHRENHEIT:
                urlBuilder.addQueryParameter("units", "imperial");
        }
        urlBuilder.addQueryParameter("APPID", API_KEY);
        Log.d(TAG, "UpdateRequest: " + API_KEY);
        String url = urlBuilder.build().toString();
        return new Request.Builder().url(url).build();
    }

    private void CallRequest() {
        mRequest = UpdateRequest();
        switch (mCurrentConfig.getRequestMode()) {
            case HOURLY:
                mClient.newCall(mRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int Unit = mCurrentConfig.getUnit();
                        ParseHourlyWeatherResponse(response, Unit);
                    }
                });
                break;
            case CURRENT:
                mClient.newCall(mRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int Unit = mCurrentConfig.getUnit();
                        ParseCurrentWeatherResponse(response, Unit);
                    }
                });
                break;
        }
    }

    private void ParseHourlyWeatherResponse(Response res, int unit) throws IOException {
        // Refer to https://openweathermap.org/current#current_JSON
        try {
            ArrayList<WeatherData> hourlyWeather = new ArrayList<>();
            String responseData = res.body().string();
            JSONObject json = new JSONObject(responseData);
            Log.d("WL", json.toString());
            JSONObject city = json.getJSONObject("city");
            JSONArray list = json.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject listItem = list.getJSONObject(i);
                JSONArray weatherInfo = listItem.getJSONArray("weather");
                JSONObject tempInfo = listItem.getJSONObject("main");
                JSONObject windInfo = listItem.getJSONObject("wind");
                JSONObject cloudInfo = listItem.getJSONObject("clouds");

                WeatherData currentWeather = new WeatherData();
                currentWeather.setCity(
                        city.getString("id"),
                        city.getString("name")
                );
                currentWeather.setTemp(
                        (float) tempInfo.getDouble("temp"),
                        (float) tempInfo.getDouble("temp_max"),
                        (float) tempInfo.getDouble("temp_min"),
                        unit
                );
                for (int j = 0; j < weatherInfo.length(); j++) {
                    JSONObject weather = weatherInfo.getJSONObject(j);
                    Log.d(TAG, "ParseResponse: " + weather.toString());
                    currentWeather.addCondition(weather);
                }
                currentWeather.setHumidity(
                        tempInfo.getInt("humidity")
                );
                currentWeather.setPressure(
                        (float) tempInfo.getDouble("pressure")
                );
                currentWeather.setCloud(
                        cloudInfo.getInt("all")
                );
                currentWeather.setWind(
                        (float) windInfo.getDouble("speed"),
                        (float) windInfo.getDouble("deg")
                );
                if (json.has("rain")) {
                    JSONObject rainInfo = listItem.getJSONObject("rain");
                    currentWeather.setRain(
                            (float) rainInfo.getDouble("3h")
                    );
                }
                if (json.has("snow")) {
                    JSONObject snowInfo = listItem.getJSONObject("snow");
                    currentWeather.setSnow(
                            (float) snowInfo.getDouble("3h")
                    );
                }
                currentWeather.timeCalculated = listItem.getLong("dt");
                hourlyWeather.add(currentWeather);
            }
            cachedHourlyWeather = hourlyWeather;
            mCurrentConfig.isDirty = false;
            EmitHourlyWeatherDataUpdateSuccess(hourlyWeather);
        }
        catch (JSONException e) {
            EmitHourlyWeatherDataUpdateError(e);
        }
    }

    private void ParseCurrentWeatherResponse(Response res, int unit) throws IOException {
        // Refer to https://openweathermap.org/current#current_JSON
        try {
            String responseData = res.body().string();
            JSONObject json = new JSONObject(responseData);
            Log.d("WL", json.toString());
            JSONObject coordInfo = json.getJSONObject("coord");
            JSONArray weatherInfo = json.getJSONArray("weather");
            JSONObject tempInfo = json.getJSONObject("main");
            JSONObject windInfo = json.getJSONObject("wind");
            JSONObject cloudInfo = json.getJSONObject("clouds");

            WeatherData currentWeather = new WeatherData();
            currentWeather.setCity(
                    json.getString("id"),
                    json.getString("name")
            );
            currentWeather.setTemp(
                    (float) tempInfo.getDouble("temp"),
                    (float) tempInfo.getDouble("temp_max"),
                    (float) tempInfo.getDouble("temp_min"),
                    unit
            );
            for (int i = 0; i < weatherInfo.length(); i++) {
                JSONObject weather = weatherInfo.getJSONObject(i);
                Log.d(TAG, "ParseResponse: " + weather.toString());
                currentWeather.addCondition(weather);
            }
            currentWeather.setHumidity(
                    tempInfo.getInt("humidity")
            );
            currentWeather.setPressure(
                    (float) tempInfo.getDouble("pressure")
            );
            currentWeather.setCloud(
                    cloudInfo.getInt("all")
            );
            currentWeather.setWind(
                    (float) windInfo.getDouble("speed"),
                    (float) windInfo.getDouble("deg")
            );
            if (json.has("rain")) {
                JSONObject rainInfo = json.getJSONObject("rain");
                currentWeather.setRain(
                        (float) rainInfo.getDouble("3h")
                );
            }
            if (json.has("snow")) {
                JSONObject snowInfo = json.getJSONObject("snow");
                currentWeather.setSnow(
                        (float) snowInfo.getDouble("3h")
                );
            }
            currentWeather.timeCalculated = json.getLong("dt");
            cachedCurrentWeather = currentWeather;
            mCurrentConfig.isDirty = false;
            EmitCurrentWeatherDataUpdateSuccess(currentWeather);
        } catch (JSONException e) {
            EmitCurrentWeatherDataUpdateError(e);
        }
    }

    private void EmitHourlyWeatherDataUpdateSuccess(List<WeatherData> newData) {
        Enumeration<OnHourlyWeatherDataReceivedListener> listeners = onHourlyWeatherDataReceivedListeners.elements();
        while(listeners.hasMoreElements()) {
            listeners.nextElement().onDataLoaded(newData);
        }
    }

    private void EmitHourlyWeatherDataUpdateError(Throwable t) {
        Enumeration<OnHourlyWeatherDataReceivedListener> listeners = onHourlyWeatherDataReceivedListeners.elements();
        while(listeners.hasMoreElements()) {
            listeners.nextElement().onDataError(t);
        }
    }

    private void EmitCurrentWeatherDataUpdateSuccess(WeatherData newData) {
        Enumeration<OnCurrentWeatherDataReceivedListener> listeners = onCurrentWeatherDataReceivedListeners.elements();
        while(listeners.hasMoreElements()) {
            listeners.nextElement().onDataLoaded(newData);
        }
    }

    private void EmitCurrentWeatherDataUpdateError(Throwable t) {
        Enumeration<OnCurrentWeatherDataReceivedListener> listeners = onCurrentWeatherDataReceivedListeners.elements();
        while(listeners.hasMoreElements()) {
            listeners.nextElement().onDataError(t);
        }
    }

    public static void UpdateConfig(WeatherConfig newConfig) {
        WeatherClient client = GetInstance();
        client.mCurrentConfig = newConfig;
    }

    public static WeatherClient GetInstance() {
        if (mInstance == null)
            mInstance = new WeatherClient();
        return mInstance;
    }

    public static WeatherConfig GetConfig() {
        return GetInstance().mCurrentConfig;
    }

    public static WeatherData GetCurrentWeather() {
        WeatherClient client = GetInstance();
        client.mCurrentConfig.setRequestMode(RequestMode.CURRENT);
        /*  Ideally, we don't want to continuously poll for new data so figure out a way to use cached
            data until a time threshold
        */
        if (client.cachedCurrentWeather != null && !client.mCurrentConfig.isDirty) {
            long timeBefore = client.cachedCurrentWeather.timeCalculated;
            if (System.currentTimeMillis() - GetInstance().TIME_BEFORE_REPOLL > timeBefore) {
                client.CallRequest();
            }
        }
        else {
            client.CallRequest();
        }
        return client.cachedCurrentWeather;
    }

    public static List<WeatherData> GetHourlyWeather() {
        WeatherClient client = GetInstance();
        client.mCurrentConfig.setRequestMode(RequestMode.HOURLY);
        if (client.cachedHourlyWeather != null && !client.mCurrentConfig.isDirty && client.cachedCurrentWeather != null) {
            long timeBefore = client.cachedCurrentWeather.timeCalculated;
            if (System.currentTimeMillis() - GetInstance().TIME_BEFORE_REPOLL > timeBefore) {
                client.CallRequest();
            }
        }
        else {
            client.CallRequest();
        }
        return client.cachedHourlyWeather;
    }

    public static void addOnCurrentWeatherDataReceivedListener(int id, OnCurrentWeatherDataReceivedListener listener) {
        if (GetInstance().onCurrentWeatherDataReceivedListeners.get(id) == null)
            GetInstance().onCurrentWeatherDataReceivedListeners.put(id, listener);
    }

    public static void addOnHourlyWeatherDataReceivedListener(int id, OnHourlyWeatherDataReceivedListener listener) {
        if (GetInstance().onHourlyWeatherDataReceivedListeners.get(id) == null)
            GetInstance().onHourlyWeatherDataReceivedListeners.put(id, listener);
    }

    public interface OnCurrentWeatherDataReceivedListener {
        void onDataLoaded(WeatherData data);
        void onDataError(Throwable t);
    }

    public interface OnHourlyWeatherDataReceivedListener {
        void onDataLoaded(List<WeatherData> data);
        void onDataError(Throwable t);
    }
}
