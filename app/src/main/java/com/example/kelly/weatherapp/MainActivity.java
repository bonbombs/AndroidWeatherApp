package com.example.kelly.weatherapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.provider.forecastio.ForecastIOProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView mTempNowView;

    private float mCurrentTemp;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        mTempNowView = findViewById(R.id.temperatureNowView);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        WeatherConfig config = new WeatherConfig();
        try {
            WeatherClient client = (new WeatherClient.ClientBuilder()).attach(this)
                    .provider(new ForecastIOProviderType())
                    .httpClient(com.survivingwithandroid.weather.lib.StandardHttpClient.class)
                    .config(config)
                    .build();
            //TODO: Change it so that we can request via custom cityId and/or GPD lat/lon
            client.getCurrentCondition(new WeatherRequest("20988507"), new WeatherClient.WeatherEventListener() {
                @Override
                public void onWeatherError(WeatherLibException wle) {
                    Log.d("WL", "Weather Error - parsing data");
                }

                @Override
                public void onConnectionError(Throwable t) {
                    Log.d("WL", "Connection Error");
                }

                @Override
                public void onWeatherRetrieved(CurrentWeather currentWeather) {
                    mCurrentTemp = currentWeather.weather.temperature.getTemp();
                    mTempNowView.setText(String.valueOf(mCurrentTemp));
                    Log.d("WL", "City [" + currentWeather.weather.location.getCity() + "] Current Temp [" + mCurrentTemp + "]");
                }
            });

        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
