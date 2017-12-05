package com.example.kelly.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_GETWEATHER = 0;

    private FusedLocationProviderClient mFusedLocationClient;
    private TextView mTempNowView;
    private TextView mTempLocationView;
    private TextView mConditionView;
    private TextView mHumidityView;
    private TextView mWindView;
    private SwipeRefreshLayout mSwipeContainer;

    private WeatherClient mClient;

    private WeatherData mCurrentWeatherData;
    private Location mCachedLocation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
                case R.id.navigation_settings:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClient = WeatherClient.GetInstance();
        mTempNowView = findViewById(R.id.temperatureNowView);
        mTempLocationView = findViewById(R.id.Location);
        mConditionView = findViewById(R.id.currentCondition);
        mHumidityView = findViewById(R.id.humidityView);
        mWindView = findViewById(R.id.windView);
        mSwipeContainer = findViewById(R.id.swipeContainer);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewData();
            }
        });

        checkPermissions();

        mClient.addOnWeatherDataReceivedListener(new WeatherClient.OnWeatherDataReceivedListener() {
            @Override
            public void onDataLoaded(WeatherData data) {
                mCurrentWeatherData = data;
                if (data != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateUI();
                        }
                    });
                }
            }
            @Override
            public void onDataError(Throwable t) {
                t.printStackTrace();
            }
        });

        fetchNewData();
    }

    private void UpdateUI() {
        if (mCurrentWeatherData != null) {
            mTempNowView.setText(String.format("%.0f", mCurrentWeatherData.getTemperature(Weather.CELSIUS)));
            mTempLocationView.setText(mCurrentWeatherData.getCityName());
            mWindView.setText(String.format("%.2f %s %.2f deg", mCurrentWeatherData.getWindSpeed(), getString(R.string.windSpeedUnitMetric), mCurrentWeatherData.getWindDeg()));
            mHumidityView.setText(String.format("%d%%", mCurrentWeatherData.getHumidity()));
            mConditionView.setText(mCurrentWeatherData.getConditionsData().get(0).ConditionDesc);
        }
        mSwipeContainer.setRefreshing(false);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState == null) return;
    }

    private void checkPermissions() {
        int internetCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int locationCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (internetCheck == PackageManager.PERMISSION_GRANTED && locationCheck == PackageManager.PERMISSION_GRANTED) {
            UpdateLocationAndWeather();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_GETWEATHER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GETWEATHER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    boolean locationGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean internetGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationGranted && internetGranted) {
                        fetchNewData();
                    } else if (internetGranted) {
                        // check if user has a specific location, otherwise, set to default (Boston)
                        UpdateWeather();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void fetchNewData() {
        UpdateLocationAndWeather();
    }

    private void UpdateLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.e("WL", "onSuccess: " + location.getLatitude() + " " + location.getLongitude());
                            mCachedLocation = location;
                            WeatherClient.UpdateConfig(new WeatherConfig(mCachedLocation.getLongitude(), mCachedLocation.getLatitude()));
                            WeatherClient.GetCurrentWeather();
                        }
                    }
                });
    }

    private void UpdateWeather() {
        if (mCachedLocation != null) {
            WeatherClient.UpdateConfig(new WeatherConfig(mCachedLocation.getLongitude(), mCachedLocation.getLatitude()));
            WeatherClient.GetCurrentWeather();
        }
    }
}