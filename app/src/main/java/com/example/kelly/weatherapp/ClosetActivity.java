package com.example.kelly.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by hollyn on 12/11/17.
 */

public class ClosetActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {

    private SwitchCompat mWinterCoatButton;
    private SwitchCompat mRainJacketButton;
    private SwitchCompat mScarfButton;
    private SwitchCompat mBeanieButton;
    private SwitchCompat mGlovesButton;
    private SwitchCompat mUmbrellaButton;
    private SwitchCompat mHatButton;
    private SwitchCompat mSunglassesButton;
    private SwitchCompat mTshirtButton;
    private SwitchCompat mShortsButton;
    private SwitchCompat mRainBootsButton;
    private SwitchCompat mSnowBootsButton;
    private SwitchCompat mSandalsButton;
    private SwitchCompat mSneakersButton;

    private Button doneButton;

    public Boolean isWinterCoat = false;
    public Boolean isRainJacket = false;
    public Boolean isScarf = false;
    public Boolean isBeanie = false;
    public Boolean isGloves = false;
    public Boolean isUmbrella = false;
    public Boolean isHat = false;
    public Boolean isSunglasses = false;
    public Boolean isTshirt = false;
    public Boolean isShorts = false;
    public Boolean isRainBoots = false;
    public Boolean isSnowBoots = false;
    public Boolean isSandals = false;
    public Boolean isSneakers = false;

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
        doneButton = findViewById(R.id.button_done);

        String userId = mSharedPreferences.getString("uuid", "");
        if (userId.isEmpty()) {
            userId = UUID.randomUUID().toString();
            mSharedPreferences.edit().putString("uuid", userId).apply();
        }
        else {
            mDatabase.child("clothing").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();
                    UserClothing data = dataSnapshot.getValue(UserClothing.class);
                    if (data == null) return;
                    if (data.isWinterCoat == null) data.isWinterCoat = false;
                    else if (data.isRainJacket == null) data.isRainJacket = false;
                    else if (data.isScarf == null) data.isScarf = false;
                    else if (data.isBeanie == null) data.isBeanie = false;
                    else if (data.isGloves == null) data.isGloves = false;
                    else if (data.isUmbrella == null) data.isUmbrella = false;
                    else if (data.isHat == null) data.isHat = false;
                    else if (data.isSunglasses == null) data.isSunglasses = false;
                    else if (data.isTshirt == null) data.isTshirt = false;
                    else if (data.isShorts) data.isShorts = false;
                    else if (data.isRainBoots) data.isRainBoots = false;
                    else if (data.isSnowBoots) data.isSnowBoots = false;
                    else if (data.isSandals) data.isSandals = false;
                    else if (data.isSneakers) data.isSneakers = false;

                    setChecked(data);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

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

                String userId = mSharedPreferences.getString("uuid", "");
                if (userId.isEmpty()) {
                   userId = UUID.randomUUID().toString();
                   mSharedPreferences.edit().putString("uuid", userId).apply();
                }

                mDatabase.child("clothing").child(userId).setValue(userClothingPref);

                finish();
            }
        });
    }

    private void setChecked(UserClothing data) {
        mWinterCoatButton.setChecked(data.isWinterCoat);
        mRainJacketButton.setChecked(data.isRainJacket);
        mScarfButton.setChecked(data.isScarf);
        mBeanieButton.setChecked(data.isBeanie);
        mGlovesButton.setChecked(data.isGloves);
        mUmbrellaButton.setChecked(data.isUmbrella);
        mHatButton.setChecked(data.isHat);
        mSunglassesButton.setChecked(data.isSunglasses);
        mTshirtButton.setChecked(data.isTshirt);
        mShortsButton.setChecked(data.isShorts);
        mRainBootsButton.setChecked(data.isRainBoots);
        mSnowBootsButton.setChecked(data.isSnowBoots);
        mSandalsButton.setChecked(data.isSandals);
        mSneakersButton.setChecked(data.isSneakers);
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
