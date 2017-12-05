package com.example.kelly.weatherapp;

import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public static final int CURRENT_FORECAST = 0;
    public static final int HOURLY_FORECAST = 1;

    private static String API_KEY = "2be70096614a5f7120ab4f91929341ef";

    private static WeatherClient mInstance;
    private Request mRequest;
    private String mCurrentCity = "4930956";    // Default is Boston
    private String mCurrentLocation;
    private OkHttpClient mClient;
    private WeatherConfig mCurrentConfig;

    private WeatherData cachedCurrentWeather;
    private List<OnWeatherDataReceivedListener> onWeatherDataReceivedListeners;

    private WeatherClient() {
        mClient = new OkHttpClient();
        mCurrentConfig = new WeatherConfig("4930956");
        mRequest = UpdateRequest();
        mInstance = this;
        onWeatherDataReceivedListeners = new ArrayList<>();
        //TODO: NOT SECURE
        if (API_KEY == null)
            API_KEY = "2be70096614a5f7120ab4f91929341ef";
    }

    private Request UpdateRequest() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://api.openweathermap.org/data/2.5/weather").newBuilder();
        Log.d(TAG, "UpdateRequest: " + mCurrentConfig);
        if (!mCurrentConfig.getCityID().isEmpty())
            urlBuilder.addQueryParameter("id", mCurrentConfig.getCityID());
        else {
            urlBuilder.addQueryParameter("lat", String.valueOf(mCurrentConfig.getLatitude()));
            urlBuilder.addQueryParameter("lon", String.valueOf(mCurrentConfig.getLongitude()));
        }
        urlBuilder.addQueryParameter("APPID", API_KEY);
        Log.d(TAG, "UpdateRequest: " + API_KEY);
        String url = urlBuilder.build().toString();
        return new Request.Builder().url(url).build();
    }

    private void CallRequest() {
        mRequest = UpdateRequest();
        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ParseResponse(response);
            }
        });
    }

    private void ParseResponse(Response res) throws IOException {
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
                    WeatherData.KELVIN
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
            EmitWeatherDataUpdateSuccess(currentWeather);
        } catch (JSONException e) {
            EmitWeatherDataUpdateError(e);
        }
    }

    private void EmitWeatherDataUpdateSuccess(WeatherData newData) {
        for (OnWeatherDataReceivedListener l : onWeatherDataReceivedListeners) {

            l.onDataLoaded(newData);
        }
    }

    private void EmitWeatherDataUpdateError(Throwable t) {
        for (OnWeatherDataReceivedListener l : onWeatherDataReceivedListeners) {
            l.onDataError(t);
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

    public static WeatherData GetCurrentWeather() {
        WeatherClient client = GetInstance();
//        if (client.cachedCurrentWeather != null) {
//            long timeBefore = client.cachedCurrentWeather.timeCalculated;
//            if (System.currentTimeMillis() - (60 * 60 * 1000) > timeBefore) {
//                client.CallRequest();
//            }
//        }
//        else {
//            client.CallRequest();
//        }
        client.CallRequest();
        return client.cachedCurrentWeather;
    }

    public static void addOnWeatherDataReceivedListener(OnWeatherDataReceivedListener listener) {
        GetInstance().onWeatherDataReceivedListeners.add(listener);
    }

    public interface OnWeatherDataReceivedListener {
        void onDataLoaded(WeatherData data);
        void onDataError(Throwable t);
    }
}
