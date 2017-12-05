package com.example.kelly.weatherapp;

import com.google.android.gms.awareness.state.Weather;

import org.json.JSONObject;

import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kelly on 12/4/2017.
 */

public class WeatherData implements Weather {

    public static final int KELVIN = 3;
    private final float KELVIN_CONVERSION = 273.15f;

    private String mCityID = "";
    private String mCityName = "";
    private double mLongitude = 0.0;
    private double mLatitude = 0.0;
    private float mTemp = 0.0f;
    private int mTempUnit = KELVIN;
    private float mPressure = 0.0f;
    private int mHumidity = 0;
    private float mTempMin = 0.0f;
    private float mTempMax = 0.0f;
    private float mWindSpeed = 0.0f;
    private float mWindDeg = 0.0f;
    private int mCloudsAll = 0;
    private float mRainVol3 = 0.0f;
    private float mSnowVol3 = 0.0f;

    private List<Integer> mConditionIDs;
    private List<Condition> mConditions;

    public long timeCalculated;

    public WeatherData () {
        mConditionIDs = new ArrayList<>();
        mConditions = new ArrayList<>();
    }

    public void setCoords(double lon, double lat) {
        this.mLatitude = lat;
        this.mLongitude = lon;
    }

    public void setCity(String cityID, String cityName) {
        this.mCityID = cityID;
        this.mCityName = cityName;
    }

    public void setTemp(float temp, float tempMax, float tempMin, int unit) {
        this.mTemp = temp;
        this.mTempMax = tempMax;
        this.mTempMin = tempMin;
        switch (unit) {
            case CELSIUS:
                mTempUnit = CELSIUS;
                break;
            case FAHRENHEIT:
                mTempUnit = FAHRENHEIT;
                break;
            default:
                mTempUnit = KELVIN;
        }
    }

    public void setHumidity(int value) {
        this.mHumidity = value;
    }

    public void setPressure(float value) {
        this.mPressure = value;
    }

    public void setWind(float speed, float deg) {
        this.mWindSpeed = speed;
        this.mWindDeg = deg;
    }

    public void setCloud(int clouds) {
        this.mCloudsAll = clouds;
    }

    public void setRain(float rainVol) {
        this.mRainVol3 = rainVol;
    }

    public void setSnow(float snowVol) {
        this.mSnowVol3 = snowVol;
    }

    public void addCondition(JSONObject condition) {
        try {
            Condition c = new Condition(condition);
            mConditions.add(c);
            mConditionIDs.add(c.State);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public String getCityID() {
        return mCityID;
    }

    public String getCityName() {
        return mCityName;
    }

    public float getWindSpeed() {
        return mWindSpeed;
    }

    public float getWindDeg() {
        return mWindDeg;
    }

    @Override
    public float getTemperature(int i) {
        switch (i) {
            case CELSIUS:
                if (mTempUnit == CELSIUS) return mTemp;
                else if (mTempUnit == FAHRENHEIT) return (mTempUnit - 32f) * (5/9);
                else if (mTempUnit == KELVIN) return mTemp - KELVIN_CONVERSION;
                break;
            case FAHRENHEIT:
                if (mTempUnit == CELSIUS) return mTemp * (9/5) + 32;
                else if (mTempUnit == FAHRENHEIT) return mTempUnit;
                else if (mTempUnit == KELVIN) return mTempUnit * (9/5) - 459.67f;
                break;
            case KELVIN:
                if (mTempUnit == CELSIUS) return mTemp + KELVIN_CONVERSION;
                else if (mTempUnit == FAHRENHEIT) return (mTempUnit + 457.67f) * (5/9);
                else if (mTempUnit == KELVIN) return mTempUnit;
                break;
        }
        return -1;
    }

    @Override
    public float getFeelsLikeTemperature(int i) {
        return getTemperature(i);
    }

    @Override
    public float getDewPoint(int i) {

        return 0;
    }

    @Override
    public int getHumidity() {
        return mHumidity;
    }

    @Override
    public int[] getConditions() {
        return new int[0];
    }

    public List<Condition> getConditionsData() {
        return mConditions;
    }
}