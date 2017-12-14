package com.example.kelly.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private final int MAIN_ACTIVITY_LISTENER_ID = 0;
    private final int PERMISSIONS_REQUEST_GETWEATHER = 0;
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private int mPreferredUnit = Weather.CELSIUS;
    private boolean mUseGPS = true;

    private MainViewType mCurrentView;
    private ConstraintLayout mCurrentViewContainer;

    private FusedLocationProviderClient mFusedLocationClient;
    private TextView mTempNowView;
    private TextView mTempLocationView;
    private TextView mConditionView;
    private TextView mHumidityView;
    private TextView mWindView;
    private ImageView mWeatherIconView;
    private HourlyWeatherAdapter mHourlyWeatherAdapter;
    private ClothingRecommendationsAdapter mClothingRecommendationAdapter;
    private SwipeRefreshLayout mSwipeContainer;

    private WeatherClient mClient;

    private WeatherData mCurrentWeatherData;
    private List<WeatherData> mHourlyWeatherData;
    private Place mCachedPlace;
    private List<String> mClothes;

    enum MainViewType {
        HOME,
        NOTIFICATIONS,
        SETTINGS
    }

    /**
     * Android view changes are set here
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mCurrentViewContainer.removeAllViews();
                    getLayoutInflater().inflate(R.layout.actvity_main_home, mCurrentViewContainer, true);
                    SetMainView(MainViewType.HOME);
                    return true;
                case R.id.navigation_notifications:
                    mCurrentViewContainer.removeAllViews();
                    getLayoutInflater().inflate(R.layout.activity_main_notifications, mCurrentViewContainer, true);
                    SetMainView(MainViewType.NOTIFICATIONS);
                    return true;
                case R.id.navigation_settings:
                    mCurrentViewContainer.removeAllViews();
                    getLayoutInflater().inflate(R.layout.activity_main_settings, mCurrentViewContainer, true);
                    SetMainView(MainViewType.SETTINGS);
                    return true;
            }
            return false;
        }
    };

    /**
     * App-logic view changes are set here
     * @param newType new type we're setting to
     */
    private void SetMainView(MainViewType newType) {
        mCurrentView = newType;
        switch(newType) {
            case HOME:
                InitMainScreen();
                UpdateHomeUI();
                break;
            case NOTIFICATIONS:
                //TODO: Logic & UI for Notifications
                break;
            case SETTINGS:
                InitSettings();
                break;
        }
    }

    @Override
    protected void onPause(){
        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (mCachedPlace != null)
            sharedPref.edit().putString(getString(R.string.preference_location), mCachedPlace.getId()).apply();
        sharedPref.edit().putBoolean(getString(R.string.preference_use_gps), mUseGPS).apply();
        WeatherClient.removeListeners(MAIN_ACTIVITY_LISTENER_ID);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init general app logic here
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeContainer = findViewById(R.id.swipeContainer);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                UpdateWeather();
            }
        });

        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean unit = sharedPref.getBoolean(getString(R.string.preference_unit), true);
        mUseGPS = sharedPref.getBoolean(getString(R.string.preference_use_gps), true);
        String placeId = sharedPref.getString(getString(R.string.preference_location), "");
        if (unit) mPreferredUnit = Weather.CELSIUS;
        else mPreferredUnit = Weather.FAHRENHEIT;

        WeatherClient.GetConfig().useGPS = mUseGPS;

        if (!placeId.isEmpty()) {
            Places.getGeoDataClient(this, null).getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                    if (task.isSuccessful()) {
                        PlaceBufferResponse places = task.getResult();
                        final Place myPlace = places.get(0);
                        Log.i(TAG, "Place found: " + myPlace.getName());
                        mCachedPlace = myPlace.freeze();
                        if (!mUseGPS) {
                            LatLng loc = mCachedPlace.getLatLng();
                            WeatherClient.GetConfig().setLocation(loc.longitude, loc.latitude);
                            UpdateWeather();
                        }
                        else {
                            UpdateLocationAndWeather();
                        }
                        places.release();
                    } else {
                        Log.e(TAG, "Place not found.");
                        if (!mUseGPS) {
                            UpdateWeather();
                        }
                        else {
                            UpdateLocationAndWeather();
                        }
                    }
                }
            });
        }

        // Init View object containing our 3 displays
        mCurrentViewContainer = findViewById(R.id.activity_main_container);
        getLayoutInflater().inflate(R.layout.actvity_main_home, mCurrentViewContainer, true);

        // Init bottom navbar
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Default should go to the home view
        SetMainView(MainViewType.HOME);
    }

    private void InitSettings() {
        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        Switch unitSwitch = findViewById(R.id.settings_unit_pref);
        Switch gpsSwitch = findViewById(R.id.settings_use_gps);
        unitSwitch.setChecked(sharedPref.getBoolean(getString(R.string.preference_unit), true));
        gpsSwitch.setChecked(sharedPref.getBoolean(getString(R.string.preference_use_gps), true));
        LinearLayout tempPrefBar = findViewById(R.id.settings_temp_pref);
        LinearLayout wardrobePrefBar = findViewById(R.id.settings_wardrobe_pref);

        tempPrefBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Hook Temp Prefs
            }
        });

        wardrobePrefBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Hook Wardrobe Prefs
            }
        });

        unitSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) mPreferredUnit = Weather.CELSIUS;
                else mPreferredUnit = Weather.FAHRENHEIT;
                sharedPref.edit().putBoolean(getString(R.string.preference_unit), b).apply();
                WeatherClient.GetConfig().setUnit(mPreferredUnit);
            }
        });

        gpsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // TODO: Fallback to (1) location -> (2) cached location -> (3) default location
                mUseGPS = b;
                sharedPref.edit().putBoolean(getString(R.string.preference_use_gps), b).apply();
                if (mUseGPS) {
                    UpdateLocationAndWeather();
                }
                else {
                    if (mCachedPlace != null) {
                        TextView locationView = findViewById(R.id.settings_location_prefs);
                        locationView.setText(mCachedPlace.getName());
                        if (!mUseGPS) {
                            LatLng loc = mCachedPlace.getLatLng();
                            WeatherClient.GetConfig().setLocation(loc.longitude, loc.latitude);
                            UpdateWeather();
                        }
                    }
                }
            }
        });

        LinearLayout locationSetting = findViewById(R.id.settings_location);
        locationSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AutocompleteFilter filter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(filter)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        LinearLayout tempSetting = findViewById(R.id.settings_temp_pref);
        tempSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), TemperatureActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout wardrobeSetting = findViewById(R.id.settings_wardrobe_pref);
        wardrobeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ClosetActivity.class);
                startActivity(intent);
            }
        });

        UpdateSettingsUI();
    }

    private void InitMainScreen() {
        mClient = WeatherClient.GetInstance();
        mTempNowView = findViewById(R.id.temperatureNowView);
        mTempLocationView = findViewById(R.id.Location);
        mConditionView = findViewById(R.id.currentCondition);
        mHumidityView = findViewById(R.id.humidityView);
        mWindView = findViewById(R.id.windView);
        mWeatherIconView = findViewById(R.id.weather_icon);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LinearLayoutManager layoutManagerWeather
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        LinearLayoutManager layoutManagerClothing
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        RecyclerView hourlyWeatherView = findViewById(R.id.hourlyWeatherView);
        RecyclerView clothingRecommendationView = findViewById(R.id.clothingRecommendationView);

        hourlyWeatherView.setLayoutManager(layoutManagerWeather);
        clothingRecommendationView.setLayoutManager(layoutManagerClothing);

        checkPermissions();

        // When we receive updated data on current weather from WeatherClient
        mClient.addOnCurrentWeatherDataReceivedListener(MAIN_ACTIVITY_LISTENER_ID, new WeatherClient.OnCurrentWeatherDataReceivedListener() {
            @Override
            public void onDataLoaded(WeatherData data) {
                mCurrentWeatherData = data;
                if (data != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateHomeUI();
                        }
                    });
                }
            }
            @Override
            public void onDataError(Throwable t) {
                t.printStackTrace();
            }
        });

        // When we receive updated data on hourly forecasts from WeatherClient
        mClient.addOnHourlyWeatherDataReceivedListener(MAIN_ACTIVITY_LISTENER_ID, new WeatherClient.OnHourlyWeatherDataReceivedListener() {
            @Override
            public void onDataLoaded(List<WeatherData> data) {
                mHourlyWeatherData = data;
                if (data != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateHomeUI();
                        }
                    });
                }
            }
            @Override
            public void onDataError(Throwable t) {
                t.printStackTrace();
            }
        });

        RecommendationService.addOnWardrobeUpdateDataReceivedListener(MAIN_ACTIVITY_LISTENER_ID, new RecommendationService.OnWardrobeUpdateDataReceivedListener() {
            @Override
            public void onDataSuccess(Set<String> data, Map<String, Integer> map) {
                List<String> clothes = new ArrayList<>();
                for (String dataItem : data) {
                    if (map.get(dataItem) > 0) {
                        String displayName = dataItem.substring(2);
                        if (dataItem.equals("isTshirt"))
                            displayName = "T-Shirt";
                        if (dataItem.equals("isRainJacket"))
                            displayName = "Rain Jacket";
                        if (dataItem.equals("isWinterCoat"))
                            displayName = "Winter Coat";
                        if (dataItem.equals("isRainBoots"))
                            displayName = "Rain Boots";
                        if (dataItem.equals("isSnowBoots"))
                            displayName = "Snow Boots";
                        clothes.add(displayName);
                    }
                }
                mClothes = clothes;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateHomeUI();
                    }
                });
            }
        });

        if (mUseGPS)
            UpdateLocationAndWeather();
        else
            UpdateWeather();
        RecommendationService.GetInstance().DetermineRecommendations(this);
    }

    private void UpdateHomeUI() {
        // Skip if we aren't on the home screen
        //if (mCurrentView != MainViewType.HOME) return;
        if (mCurrentWeatherData != null) {
            mTempNowView.setText(String.format("%.0f", mCurrentWeatherData.getTemperature(mPreferredUnit)));
            mTempLocationView.setText(mCurrentWeatherData.getCityName());
            String windSpeedUnit = "";
            if (mPreferredUnit == Weather.CELSIUS) windSpeedUnit = getString(R.string.windSpeedUnitMetric);
            else getString(R.string.windSpeedUnitImperial);
            mWindView.setText(String.format("%.2f %s %.2f deg", mCurrentWeatherData.getWindSpeed(), windSpeedUnit, mCurrentWeatherData.getWindDeg()));
            mHumidityView.setText(String.format("%d%%", mCurrentWeatherData.getHumidity()));
            mConditionView.setText(mCurrentWeatherData.getConditionsData().get(0).ConditionDesc);
            mWeatherIconView.setImageResource(mCurrentWeatherData.getConditionsData().get(0).mIconId);
        }
        if (mHourlyWeatherData != null) {
            RecyclerView hourlyWeatherView = findViewById(R.id.hourlyWeatherView);
            if (mHourlyWeatherAdapter == null) {
                mHourlyWeatherAdapter = new HourlyWeatherAdapter(mHourlyWeatherData);
            }
            if (hourlyWeatherView != null && hourlyWeatherView.getAdapter() == null)
                hourlyWeatherView.setAdapter(mHourlyWeatherAdapter);
            mHourlyWeatherAdapter.mWeatherList = mHourlyWeatherData;
            mHourlyWeatherAdapter.notifyDataSetChanged();
        }

        if (mClothes != null) {
            RecyclerView clothingRecommendationView = findViewById(R.id.clothingRecommendationView);
            if (mClothingRecommendationAdapter == null)
                mClothingRecommendationAdapter = new ClothingRecommendationsAdapter(mClothes);
            if (clothingRecommendationView != null && clothingRecommendationView.getAdapter() == null)
                clothingRecommendationView.setAdapter(mClothingRecommendationAdapter);
            mClothingRecommendationAdapter.mRecommendedClothes = mClothes;
            mClothingRecommendationAdapter.notifyDataSetChanged();
        }

        TextView unitView = findViewById(R.id.tempUnit);
        if (unitView != null) {
            if (mPreferredUnit == Weather.CELSIUS)
                unitView.setText(getString(R.string.tempUnitMetric));
            else
                unitView.setText(getString(R.string.tempUnitImperial));
        }
        mSwipeContainer.setRefreshing(false);
    }

    private void UpdateSettingsUI() {
        // Update location if we changed it from a fragment
        TextView locationView = findViewById(R.id.settings_location_prefs);
        String useGPS = (mUseGPS) ? "Use GPS, " : "";
        if (mCachedPlace != null) {
            locationView.setText(useGPS + mCachedPlace.getName());
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState == null) return;
    }

    private void checkPermissions() {
        int internetCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int locationCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (internetCheck == PackageManager.PERMISSION_GRANTED && locationCheck == PackageManager.PERMISSION_GRANTED) {
            UpdateLocationAndWeather();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_GETWEATHER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GETWEATHER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    boolean locationGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean internetGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (mUseGPS && locationGranted && internetGranted) {
                        UpdateLocationAndWeather();
                    } else if (internetGranted) {
                        // check if user has a specific location, otherwise, set to default (Boston)
                        UpdateWeather();
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                mCachedPlace = place;
                if (!mUseGPS) {
                    LatLng loc = mCachedPlace.getLatLng();
                    WeatherClient.GetConfig().setLocation(loc.longitude, loc.latitude);
                    UpdateWeather();
                }
                UpdateSettingsUI();

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * Grabs location from GPS and updates the weather
     */
    private void UpdateLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null && mUseGPS) {
                            Log.e("WL", "onSuccess: " + location.getLatitude() + " " + location.getLongitude());
                            WeatherClient.GetConfig().setLocation(location.getLongitude(), location.getLatitude());
                            UpdateWeather();
                        }
                    }
                });
    }

    /**
     * Force fetch new data from API Service
     */
    private void UpdateWeather() {
        // Used WeatherClient's cached data first
        mCurrentWeatherData = WeatherClient.GetCurrentWeather();
        mHourlyWeatherData = WeatherClient.GetHourlyWeather();
        // Update UI using cached data for the time being
        UpdateHomeUI();
    }

    /**
     * Hourly Weather RecyclerView elements
     */
    public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherAdapter.HourlyWeatherItem> {

        private final int MAX_ITEMS_TO_DISPLAY = 5;
        private List<WeatherData> mWeatherList;

        public class HourlyWeatherItem extends RecyclerView.ViewHolder {

            TextView mTemp;
            TextView mTime;
            TextView mCondition;
            TextView mUnit;

            public HourlyWeatherItem(View view) {
                super(view);
                mTemp = view.findViewById(R.id.hourlyTemp);
                mTime = view.findViewById(R.id.hourlyTime);
                mCondition = view.findViewById(R.id.hourlyCondition);
                mUnit = view.findViewById(R.id.hourlyTempUnit);
            }

            public void Bind(WeatherData data) {
                // Populate item with time, temp, and condition
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
                simpleDateFormat.setTimeZone(TimeZone.getDefault());
                String myDate = simpleDateFormat.format(new Date(data.timeCalculated * 1000L));
                mTime.setText(myDate);
                mTemp.setText(String.format("%.0f", data.getTemperature(mPreferredUnit)));
                mCondition.setText(data.getConditionsData().get(0).ConditionDesc);
                if (mPreferredUnit == Weather.CELSIUS)
                    mUnit.setText(getString(R.string.tempUnitMetric));
                else
                    mUnit.setText(getString(R.string.tempUnitImperial));
            }
        }


        public HourlyWeatherAdapter(List<WeatherData> weatherList) {
            mWeatherList = weatherList;
        }

        @Override
        public HourlyWeatherItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getBaseContext())
                    .inflate(R.layout.hourly_weather_item_view, parent, false);
            return new HourlyWeatherItem(itemView);
        }

        @Override
        public void onBindViewHolder(final HourlyWeatherItem holder, final int position) {
            holder.Bind(mWeatherList.get(position));
        }

        @Override
        public int getItemCount() {
            if (mWeatherList.size() < MAX_ITEMS_TO_DISPLAY)
                return mWeatherList.size();
            else
                return MAX_ITEMS_TO_DISPLAY;
        }
    }

    /**
     * Hourly Weather RecyclerView elements
     */
    public class ClothingRecommendationsAdapter extends RecyclerView.Adapter<ClothingRecommendationsAdapter.ClothingRecommendationItem> {

        private final int MAX_ITEMS_TO_DISPLAY = 4;
        private List<String> mRecommendedClothes;

        public class ClothingRecommendationItem extends RecyclerView.ViewHolder {

            ImageView mIcon;
            TextView mClothingText;

            public ClothingRecommendationItem(View view) {
                super(view);
                // TODO: grab icon and name views
                mIcon = view.findViewById(R.id.clothing_icon);
                mClothingText = view.findViewById(R.id.clothing_name);
            }

            public void Bind(String data) {
                // TODO: set icon based on data
                int id = 0;
                if (data.contains("Beanie")) id = R.drawable.icons8_beanie_50;
                if (data.contains("T-Shirt")) id = R.drawable.icons8_t_shirt_50;
                if (data.contains("Hat")) id = R.drawable.icons8_baseball_cap_50;
                if (data.contains("Sunglasses")) id = R.drawable.icons8_glasses_filled_50;
                if (data.contains("Rain Jacket")) id = R.drawable.icons8_jacket_50;
                if (data.contains("Winter Coat")) id = R.drawable.icons8_coat_50;
                if (data.contains("Scarf")) id = R.drawable.icons8_scarf_50;
                if (data.contains("Gloves")) id = R.drawable.icons8_mittens_50;
                if (data.contains("Umbrella")) id = R.drawable.icons8_umbrella_50;
                if (data.contains("Shorts")) id = R.drawable.icons8_shorts_50;
                if (data.contains("Rain Boots")) id = R.drawable.icons8_boots_50;
                if (data.contains("Snow Boots")) id = R.drawable.icons8_winter_boots_50;
                if (data.contains("Sandals")) id = R.drawable.icons8_flip_flops_50;
                if (data.contains("Sneakers")) id = R.drawable.icons8_trainers_50;

                mIcon.setImageResource(id);
                mClothingText.setText(data);
            }
        }


        public ClothingRecommendationsAdapter(List<String> clothingList) {
            // TODO: set list of clothes to display
            mRecommendedClothes = clothingList;
        }

        @Override
        public ClothingRecommendationItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getBaseContext())
                    .inflate(R.layout.clothing_recommendation_item_view, parent, false);
            return new ClothingRecommendationItem(itemView);
        }

        @Override
        public void onBindViewHolder(final ClothingRecommendationItem holder, final int position) {
            holder.Bind(mRecommendedClothes.get(position));
        }

        @Override
        public int getItemCount() {
            if (mRecommendedClothes.size() < MAX_ITEMS_TO_DISPLAY)
                return mRecommendedClothes.size();
            else
                return MAX_ITEMS_TO_DISPLAY;
        }
    }
}