package com.example.kelly.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * Created by hollyn on 12/11/17.
 */

public class ClosetActivity extends AppCompatActivity {

    private Button mWinterCoatButton;
    private Button mRainJacketButton;
    private Button mScarfButton;
    private Button mBeanieButton;
    private Button mGlovesButton;
    private Button mUmbrellaButton;
    private Button mHatButton;
    private Button mSunglassesButton;
    private Button mTshirtButton;
    private Button mShortsButton;
    private Button mRainBootsButton;
    private Button mSnowBootsButton;
    private Button mSandalsButton;
    private Button mSneakersButton;
    private Button nextButton;

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
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);

        // Get Firebase database reference using google-services.json file
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mSharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

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
        nextButton = findViewById(R.id.button_next);

        mWinterCoatButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isWinterCoat = true;
            }
        });

        mRainJacketButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isRainJacket = true;
            }
        });

        mScarfButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isScarf = true;

            }
        });

        mBeanieButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isBeanie = true;
            }
        });

        mGlovesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isGloves = true;
            }
        });

        mUmbrellaButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isUmbrella = true;
            }
        });

        mHatButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isHat = true;

            }
        });

        mSunglassesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isSunglasses = true;
            }
        });

        mTshirtButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isTshirt = true;
            }
        });

        mShortsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isShorts = true;
            }
        });

        mRainBootsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isRainBoots = true;
            }
        });

        mSnowBootsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isSnowBoots = true;
            }
        });

        mSandalsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isSandals = true;

            }
        });

        mSneakersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isSneakers = true;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UserClothing userClothingPref = new UserClothing(isWinterCoat, isRainJacket, isScarf,
                        isBeanie, isGloves, isUmbrella, isHat, isSunglasses, isTshirt, isShorts,
                        isRainBoots, isSnowBoots, isSandals, isSneakers);

                String userId = mSharedPreferences.getString("uuid", "");
                if (userId.isEmpty()) {
                   userId = UUID.randomUUID().toString();
                   mSharedPreferences.edit().putString("uuid", userId).apply();
                }

                mDatabase.child("clothing").child(userId).setValue(userClothingPref);

                // Intent to go to temperature preferences activity
                Intent intent = new Intent(getApplicationContext(), TemperatureActivity.class);
                startActivity(intent);
            }
        });
    }



}
