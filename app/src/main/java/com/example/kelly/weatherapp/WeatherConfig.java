package com.example.kelly.weatherapp;

import static com.google.android.gms.awareness.state.Weather.CELSIUS;
import static com.google.android.gms.awareness.state.Weather.FAHRENHEIT;

/**
 * Created by Chiff on 12/4/2017.
 */

public class WeatherConfig {

    public boolean useGPS;
    public boolean isDirty;        // Has this object been modified recently
    private WeatherClient.RequestMode mRequestMode;
    private double mLatitude = 0;
    private double mLongitude = 0;
    private String mCityID = "";
    private int mUnit = CELSIUS;


    public WeatherConfig(String cityID) {
        this.useGPS = true;
        this.mCityID = cityID;
        this.mLatitude = 0;
        this.mLongitude = 0;
        this.mRequestMode = WeatherClient.RequestMode.CURRENT;
        this.mUnit = CELSIUS;
        this.isDirty = true;
    }

    public WeatherConfig(double lon, double lat) {
        this.useGPS = true;
        this.mLatitude = lat;
        this.mLongitude = lon;
        this.mCityID = "";
        this.mRequestMode = WeatherClient.RequestMode.CURRENT;
        this.mUnit = CELSIUS;
        this.isDirty = true;
    }

    public double getLongitude() { return this.mLongitude; }
    public double getLatitude() { return this.mLatitude; }
    public WeatherClient.RequestMode getRequestMode() { return mRequestMode; }
    public int getUnit() { return this.mUnit; }
    public void setLocation(double lon, double lat) {
        if (lon != this.mLongitude || lat != this.mLatitude)
            this.isDirty = true;
        this.mLongitude = lon;
        this.mLatitude = lat;
    }
    public void setRequestMode(WeatherClient.RequestMode newMode) { this.mRequestMode = newMode; }
    public void setUnit(int newUnit) {
        if (this.mUnit != newUnit)
            this.isDirty = true;
        switch (newUnit) {
            case CELSIUS:
                this.mUnit = newUnit;
                break;
            case FAHRENHEIT:
                this.mUnit = newUnit;
                break;
        }
    }
}
