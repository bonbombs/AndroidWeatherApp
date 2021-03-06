package com.example.kelly.weatherapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

import static android.content.ContentValues.TAG;

public class NotificationService extends Service {

    private final int PERMISSIONS_REQUEST_GETWEATHER = 0;
    private final int NOTIFICATION_SERVICE_LISTENER_ID = 5;
    private SharedPreferences sharedPref;
    private WeatherClient mClient;
    private FusedLocationProviderClient mFusedLocationClient;
    boolean mUseGPS;
    private WeatherData mCurrentWeatherData;
    private float temp;
    private List<Condition> conditions;
    private List<String> clothingRecommendations;
    private String clothes;
    private android.app.Notification notify;
    private int icon;


    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mUseGPS = sharedPref.getBoolean(getString(R.string.preference_use_gps), false);
        WeatherClient.resetTimers();

        if (mUseGPS) {
            mClient = WeatherClient.GetInstance();
            // When we receive updated data on current weather from WeatherClient
            mClient.addOnCurrentWeatherDataReceivedListener(NOTIFICATION_SERVICE_LISTENER_ID, new WeatherClient.OnCurrentWeatherDataReceivedListener() {
                @Override
                public void onDataLoaded(WeatherData data) {
                    mCurrentWeatherData = data;
                    if (data != null) {
                        updateWeatherData();
                    }
                }
                @Override
                public void onDataError(Throwable t) {
                    t.printStackTrace();
                }
            });

            UpdateLocationAndWeather();
        }
        else {
            mClient.addOnCurrentWeatherDataReceivedListener(NOTIFICATION_SERVICE_LISTENER_ID, new WeatherClient.OnCurrentWeatherDataReceivedListener() {
                @Override
                public void onDataLoaded(WeatherData data) {
                    mCurrentWeatherData = data;
                    if (data != null) {
                        updateWeatherData();
                    }
                }
                @Override
                public void onDataError(Throwable t) {
                    t.printStackTrace();
                }
            });
            String placeId = sharedPref.getString(getString(R.string.preference_location), "");
            if (!placeId.isEmpty()) {
                Places.getGeoDataClient(this, null).getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
                            PlaceBufferResponse places = task.getResult();
                            final Place myPlace = places.get(0);
                            Log.i(TAG, "Place found: " + myPlace.getName());
                            sharedPref.edit().putString(getString(R.string.preference_location), myPlace.getId()).apply();
                            if (!mUseGPS) {
                                LatLng loc = myPlace.getLatLng();
                                WeatherClient.GetConfig().setLocation(loc.longitude, loc.latitude);
                                UpdateWeather();
                            }
                            else {
                                UpdateLocationAndWeather();
                            }
                            places.release();
                        } else {
                            Log.e(TAG, "Place not found.");
                            if (mUseGPS) {
                                UpdateWeather();
                            }
                            else {
                                UpdateLocationAndWeather();
                            }
                        }
                    }
                });
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void UpdateWeather() {
        mCurrentWeatherData = WeatherClient.GetCurrentWeather();
    }

    /**
     * Grabs location from GPS and updates the weather
     */
    private void UpdateLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d("Location_found", "Location Found");
                // Got last known location. In some rare situations this can be null.
                if (location != null && mUseGPS) {
                    Log.e("WL", "onSuccess: " + location.getLatitude() + " " + location.getLongitude());
                    WeatherClient.GetConfig().setLocation(location.getLongitude(), location.getLatitude());
                    mCurrentWeatherData = WeatherClient.GetCurrentWeather();
                }
            }
        });
    }

    private void checkPermissions() {
        int internetCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int locationCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (internetCheck == PackageManager.PERMISSION_GRANTED && locationCheck == PackageManager.PERMISSION_GRANTED) {
            UpdateLocationAndWeather();
        }
    }

    private void updateWeatherData() {
        temp = mCurrentWeatherData.getTemperature(Weather.FAHRENHEIT);
        conditions = mCurrentWeatherData.getConditionsData();
        clothingRecommendations = RecommendationService.GetRecommendedWardrobe();
        sendNotification();
    }

    private void sendNotification() {
        clothes = "";

        Log.d("NOTIFICATION", "");
        NotificationManager notif = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification brings user to app on click
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Change the icon based on the condition, sun by default
        switch (conditions.get(0).ConditionName) {
            case "sunny":
                icon = R.drawable.sun;
                break;
            default:
                icon = R.drawable.sun;
                break;
        }

        // Make list of clothing recommendations one string
        for (String item : clothingRecommendations) {
            clothes = clothes + item + ", ";
        }

        // User wants both current weather and clothing recommendations in notification
        if (sharedPref.getBoolean("notifications_current_weather", true) &&
                sharedPref.getBoolean("notifications_clothing_recommendations", false)) {

            notify = new android.app.Notification.Builder(
                    this.getApplicationContext())
                    .setContentTitle("Weather App")
                    .setContentText("Check the weather!")
                    .setStyle(new Notification.BigTextStyle()
                            .bigText("Current Temp: " + temp + " F" + "\n" +
                                    "Condition: " + conditions.get(0).ConditionName + "\n" +
                                    "Clothing: " + clothes))
                    .setSmallIcon(icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        }
        // User wants only clothing recommendations in notification
        else if (sharedPref.getBoolean("notifications_clothing_recommendations", false)) {
            notify = new android.app.Notification.Builder(
                    this.getApplicationContext())
                    .setContentTitle("Weather App")
                    .setContentText("Check the weather!")
                    .setStyle(new Notification.BigTextStyle()
                            .bigText("Clothing: " + clothes))
                    .setSmallIcon(icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        }
        // User wants only current weather in notification
        else if (sharedPref.getBoolean("notifications_current_weather", true)) {
            notify = new android.app.Notification.Builder(
                    this.getApplicationContext())
                    .setContentTitle("Weather App")
                    .setContentText("Check the weather!")
                    .setStyle(new Notification.BigTextStyle()
                            .bigText("Current Temp: " + temp + " F" + "\n" +
                                    "Condition: " + conditions.get(0).ConditionName))
                    .setSmallIcon(icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        }
        // User doesn't want anything in notification, for some reason???
        else {
            notify = new android.app.Notification.Builder(
                    this.getApplicationContext())
                    .setContentTitle("Weather App")
                    .setContentText("Check the weather!")
                    .setSmallIcon(icon)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        }

        notify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);

        WeatherClient.removeListeners(NOTIFICATION_SERVICE_LISTENER_ID);
    }
}
