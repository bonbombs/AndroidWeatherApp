package com.example.kelly.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        nextButton = findViewById(R.id.button_next);

        mWinterCoatButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mRainJacketButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mScarfButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mBeanieButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mGlovesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mUmbrellaButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mHatButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mSunglassesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mTshirtButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mShortsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mRainBootsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mSnowBootsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mSandalsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        mSneakersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Firebase code to add to user clothing and accessories preferences
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Intent to go to temperature preferences activity
                Intent intent = new Intent(getApplicationContext(), TemperatureActivity.class);
                startActivity(intent);
            }
        });
    }



}
