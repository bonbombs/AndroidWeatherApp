package com.example.kelly.weatherapp;

/**
 * Created by Chiff on 12/4/2017.
 */

public class WeatherConfig {

    private double mLatitude = 0;
    private double mLongitude = 0;
    private String mCityID = "";

    public WeatherConfig(String cityID) {
        this.mCityID = cityID;
        this.mLatitude = 0;
        this.mLongitude = 0;
    }

    public WeatherConfig(double lon, double lat) {
        this.mLatitude = lat;
        this.mLongitude = lon;
        this.mCityID = "";
    }

    public double getLongitude() { return this.mLongitude; }
    public double getLatitude() { return this.mLatitude; }
    public String getCityID() { return this.mCityID; }
}
