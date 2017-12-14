package com.example.kelly.weatherapp;

/**
 * Created by hollyn on 12/11/17.
 */

public class UserTemperature {

    public Integer isHot;
    public Integer isCold;

    public UserTemperature() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        isHot = Integer.MAX_VALUE;
        isCold = Integer.MIN_VALUE;
    }

    public UserTemperature(Integer hotTemp, Integer coldTemp) {
        this.isHot = hotTemp;
        this.isCold = coldTemp;
    }
}
