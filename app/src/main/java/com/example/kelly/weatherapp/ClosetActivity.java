package com.example.kelly.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * Created by hollyn on 12/11/17.
 */

public class ClosetActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {

    private Switch mWinterCoatButton;
    private Switch mRainJacketButton;
    private Switch mScarfButton;
    private Switch mBeanieButton;
    private Switch mGlovesButton;
    private Switch mUmbrellaButton;
    private Switch mHatButton;
    private Switch mSunglassesButton;
    private Switch mTshirtButton;
    private Switch mShortsButton;
    private Switch mRainBootsButton;
    private Switch mSnowBootsButton;
    private Switch mSandalsButton;
    private Switch mSneakersButton;

    private Button doneButton;

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

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);

        // Get Firebase database reference using google-services.json file
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mWinterCoatButton = findViewById(R.id.button_wintercoat);
        mRainJacketButton = findViewById(R.id.button_rainjacket);
        mScarfButton = findViewById(R.id.button_scarf);
        mBeanieButton = findViewById(R.id.button_beanie);
        mGlovesButton = findViewById(R.id.button_gloves);
        mUmbrellaButton = findViewById(R.id.button_umbrella);
        mHatButton = findViewById(R.id.button_hat);
        mSunglassesButton = findViewById(R.id.button_sunglasses);
        mTshirtButton = findViewById(R.id.button_tshirt);
        mShortsButton = findViewById(R.id.button_shorts);
        mRainBootsButton = findViewById(R.id.button_rainboots);
        mSnowBootsButton = findViewById(R.id.button_snowboots);
        mSandalsButton = findViewById(R.id.button_sandals);
        mSneakersButton = findViewById(R.id.button_sneakers);
        doneButton = findViewById(R.id.button_done);

        mWinterCoatButton.setOnCheckedChangeListener(this);
        mRainJacketButton.setOnCheckedChangeListener(this);
        mScarfButton.setOnCheckedChangeListener(this);
        mBeanieButton.setOnCheckedChangeListener(this);
        mGlovesButton.setOnCheckedChangeListener(this);
        mUmbrellaButton.setOnCheckedChangeListener(this);
        mHatButton.setOnCheckedChangeListener(this);
        mSunglassesButton.setOnCheckedChangeListener(this);
        mTshirtButton.setOnCheckedChangeListener(this);
        mShortsButton.setOnCheckedChangeListener(this);
        mRainBootsButton.setOnCheckedChangeListener(this);
        mSnowBootsButton.setOnCheckedChangeListener(this);
        mSandalsButton.setOnCheckedChangeListener(this);
        mSneakersButton.setOnCheckedChangeListener(this);

        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UserClothing userClothingPref = new UserClothing(isWinterCoat, isRainJacket, isScarf,
                        isBeanie, isGloves, isUmbrella, isHat, isSunglasses, isTshirt, isShorts,
                        isRainBoots, isSnowBoots, isSandals, isSneakers);

                String userId = UUID.randomUUID().toString();

                mDatabase.child("clothing").child(userId).setValue(userClothingPref);

                // Intent to go to temperature preferences activity
                Intent intent = new Intent(getApplicationContext(), TemperatureActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.button_wintercoat:
                if (isChecked) { isWinterCoat = true; }
                else { isWinterCoat = false; }
                break;
            case R.id.button_rainjacket:
                if (isChecked) { isRainJacket = true; }
                else { isRainJacket = false; }
                break;
            case R.id.button_scarf:
                if (isChecked) { isScarf = true; }
                else { isScarf = false; }
                break;
            case R.id.button_beanie:
                if (isChecked) { isBeanie = true; }
                else { isBeanie = false; }
                break;
            case R.id.button_gloves:
                if (isChecked) { isGloves = true; }
                else { isGloves = false; }
                break;
            case R.id.button_umbrella:
                if (isChecked) { isUmbrella = true; }
                else { isUmbrella = false; }
                break;
            case R.id.button_hat:
                if (isChecked) { isHat = true; }
                else { isHat = false; }
                break;
            case R.id.button_sunglasses:
                if (isChecked) { isSunglasses = true; }
                else { isSunglasses = false; }
                break;
            case R.id.button_tshirt:
                if (isChecked) { isTshirt = true; }
                else { isTshirt = false; }
                break;
            case R.id.button_shorts:
                if (isChecked) { isShorts = true; }
                else { isShorts = false; }
                break;
            case R.id.button_rainboots:
                if (isChecked) { isRainBoots = true; }
                else { isRainBoots = false; }
                break;
            case R.id.button_snowboots:
                if (isChecked) { isSnowBoots = true; }
                else { isSnowBoots = false; }
                break;
            case R.id.button_sandals:
                if (isChecked) { isSandals = true; }
                else { isSandals = false; }
                break;
            case R.id.button_sneakers:
                if (isChecked) { isSneakers = true; }
                else { isSneakers = false; }
                break;
        }

    }


}
