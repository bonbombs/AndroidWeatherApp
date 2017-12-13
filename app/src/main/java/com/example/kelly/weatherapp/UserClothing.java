package com.example.kelly.weatherapp;


/**
 * Created by hollyn on 12/11/17.
 */

public class UserClothing {

    public Boolean isWinterCoat;
    public Boolean isRainJacket;
    public Boolean isScarf;
    public Boolean isBeanie;
    public Boolean isGloves;
    public Boolean isUmbrella;
    public Boolean isHat;
    public Boolean isSunglasses;
    public Boolean isTshirt;
    public Boolean isShorts;
    public Boolean isRainBoots;
    public Boolean isSnowBoots;
    public Boolean isSandals;
    public Boolean isSneakers;

    public UserClothing() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserClothing(Boolean wcExist, Boolean rjExist, Boolean scarfExist, Boolean beanieExist,
                        Boolean glovesExist, Boolean umbExist, Boolean hatExist, Boolean sunExist,
                        Boolean tsExist, Boolean shortsExist, Boolean rbExist, Boolean sbExist,
                        Boolean sandExist, Boolean sneakersExist) {
        this.isWinterCoat = wcExist;
        this.isRainJacket = rjExist;
        this.isScarf = scarfExist;
        this.isBeanie = beanieExist;
        this.isGloves = glovesExist;
        this.isUmbrella = umbExist;
        this.isHat = hatExist;
        this.isSunglasses = sunExist;
        this.isTshirt = tsExist;
        this.isShorts = shortsExist;
        this.isRainBoots = rbExist;
        this.isSnowBoots = sbExist;
        this.isSandals = sandExist;
        this.isSneakers = sneakersExist;
    }
}
