package com.example.kelly.weatherapp;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class NotificationService extends Service {

    private final int PERMISSIONS_REQUEST_GETWEATHER = 0;
    private final int NOTIFICATION_SERVICE_LISTENER_ID = 1;
    private SharedPreferences sharedPref;
    private WeatherClient mClient;
    private FusedLocationProviderClient mFusedLocationClient;
    boolean mUseGPS;
    private WeatherData mCurrentWeatherData;
    private float temp;


    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Build notification based on user preferences
        sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);

        mUseGPS = sharedPref.getBoolean("preference_use_gps", false);

        mClient = WeatherClient.GetInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (mUseGPS) {
            UpdateLocationAndWeather();
        }

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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        sendNotification();
    }

    private void sendNotification() {
        NotificationManager notif = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        // TODO: Get weather/clothing recommendation information instead
        android.app.Notification notify = new android.app.Notification.Builder(
                this.getApplicationContext()).setContentTitle("Weather App")
                .setContentText("Current Temp: " + temp + " F")
                .setSmallIcon(R.drawable.sun).build();

        notify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify);
    }
}
