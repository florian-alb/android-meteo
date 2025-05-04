package com.meteo_app_java.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meteo_app_java.R;
import com.meteo_app_java.data.WeatherRepository;
import com.meteo_app_java.databinding.ActivityMainBinding;
import com.meteo_app_java.models.DailyForecast;
import com.meteo_app_java.models.HourlyForecast;
import com.meteo_app_java.models.Weather;
import com.meteo_app_java.ui.adapters.DailyForecastAdapter;
import com.meteo_app_java.ui.adapters.HourlyForecastAdapter;
import com.meteo_app_java.utils.LocationUtils;
import com.meteo_app_java.utils.PreferencesManager;
import com.meteo_app_java.utils.WeatherUtils;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    private WeatherRepository weatherRepository;
    private PreferencesManager preferencesManager;
    private LocationUtils locationUtils;

    private HourlyForecastAdapter hourlyAdapter;
    private DailyForecastAdapter dailyAdapter;

    private boolean isMetric;
    private double currentLat;
    private double currentLon;
    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize repositories and utilities
        weatherRepository = WeatherRepository.getInstance();
        preferencesManager = new PreferencesManager(this);
        locationUtils = new LocationUtils(this);

        // Get unit preference
        isMetric = preferencesManager.isUsingMetricUnits();

        // Setup recycler views
        setupRecyclerViews();

        // Setup click listeners
        setupClickListeners();

        // Setup swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshWeatherData);

        // Setup back pressed callback
        setupBackPressedCallback();

        // Check if we're coming from search
        if (getIntent().hasExtra("useDeviceLocation")) {
            boolean useDeviceLocation = getIntent().getBooleanExtra("useDeviceLocation", false);
            preferencesManager.setUseDeviceLocation(useDeviceLocation);
        }

        // Check if we have location data in intent
        if (getIntent().hasExtra("latitude") && getIntent().hasExtra("longitude")) {
            double lat = getIntent().getDoubleExtra("latitude", 0);
            double lon = getIntent().getDoubleExtra("longitude", 0);
            String city = getIntent().getStringExtra("city");

            currentLat = lat;
            currentLon = lon;
            currentCity = city;

            loadWeatherData(lat, lon);
        } else {
            // Load last known location or request current location
            loadInitialLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data if needed
        long lastUpdated = preferencesManager.getLastUpdated();
        long currentTime = System.currentTimeMillis();

        // Refresh if data is older than 30 minutes
        if (currentTime - lastUpdated > 30 * 60 * 1000) {
            refreshWeatherData();
        }
    }

    private void setupRecyclerViews() {
        // Setup hourly forecast RecyclerView
        hourlyAdapter = new HourlyForecastAdapter(isMetric);
        binding.rvHourlyForecast.setAdapter(hourlyAdapter);
        binding.rvHourlyForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Setup daily forecast RecyclerView
        dailyAdapter = new DailyForecastAdapter(isMetric);
        binding.rvDailyForecast.setAdapter(dailyAdapter);
    }

    private void setupClickListeners() {
        // Search button click
        binding.btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // View details click
        binding.tvViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailedForecastActivity.class);
            intent.putExtra("latitude", currentLat);
            intent.putExtra("longitude", currentLon);
            intent.putExtra("city", currentCity);
            startActivity(intent);
        });

        // Refresh button click
        binding.fabRefresh.setOnClickListener(v -> refreshWeatherData());
    }

    private void loadInitialLocation() {
        // Check if we should use device location
        if (preferencesManager.shouldUseDeviceLocation()) {
            if (locationUtils.hasLocationPermission()) {
                requestCurrentLocation();
            } else {
                locationUtils.requestLocationPermission(this);
            }
        } else {
            // Use last saved location
            double lat = preferencesManager.getLastLocationLatitude();
            double lon = preferencesManager.getLastLocationLongitude();
            String city = preferencesManager.getLastLocationName();

            if (lat != 0.0 && lon != 0.0) {
                currentLat = lat;
                currentLon = lon;
                currentCity = city;
                loadWeatherData(lat, lon);
            } else {
                // Default to a well-known location if no saved location
                loadWeatherData(48.8566, 2.3522); // Paris
            }
        }
    }

    private void requestCurrentLocation() {
        showLoading(true);
        locationUtils.getLastLocation(new LocationUtils.OnLocationResultListener() {
            @Override
            public void onLocationResult(Location location) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                loadWeatherData(currentLat, currentLon);
            }

            @Override
            public void onLocationFailed(String message) {
                showLoading(false);
                Toast.makeText(MainActivity.this, getString(R.string.error_location), Toast.LENGTH_SHORT).show();

                // Fall back to last saved location
                double lat = preferencesManager.getLastLocationLatitude();
                double lon = preferencesManager.getLastLocationLongitude();
                if (lat != 0.0 && lon != 0.0) {
                    loadWeatherData(lat, lon);
                }
            }
        });
    }

    private void loadWeatherData(double latitude, double longitude) {
        showLoading(true);

        // Load current weather
        weatherRepository.getCurrentWeatherByCoordinates(latitude, longitude)
                .observe(this, new Observer<Weather>() {
                    @Override
                    public void onChanged(Weather weather) {
                        if (weather != null) {
                            updateCurrentWeatherUI(weather);
                            currentCity = weather.getCityName();

                            // Save to preferences
                            preferencesManager.saveLastLocation(
                                    latitude, longitude, weather.getCityName());
                        }
                        showLoading(false);
                    }
                });

        // Load hourly forecast
        weatherRepository.getHourlyForecast(latitude, longitude)
                .observe(this, new Observer<List<HourlyForecast>>() {
                    @Override
                    public void onChanged(List<HourlyForecast> hourlyForecasts) {
                        if (hourlyForecasts != null && !hourlyForecasts.isEmpty()) {
                            hourlyAdapter.submitList(hourlyForecasts);
                        }
                    }
                });

        // Load daily forecast
        weatherRepository.getDailyForecast(latitude, longitude)
                .observe(this, new Observer<List<DailyForecast>>() {
                    @Override
                    public void onChanged(List<DailyForecast> dailyForecasts) {
                        if (dailyForecasts != null && !dailyForecasts.isEmpty()) {
                            dailyAdapter.submitList(dailyForecasts);
                        }
                    }
                });
    }

    private void updateCurrentWeatherUI(Weather weather) {
        // Set location
        binding.tvLocation.setText(weather.getCityName() + ", " + weather.getCountryCode());

        // Set last updated
        String lastUpdatedText = getString(R.string.last_updated,
                new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                        .format(weather.getLastUpdated()));
        binding.tvLastUpdated.setText(lastUpdatedText);

        // Set temperature
        binding.tvTemperature.setText(WeatherUtils.formatTemperature(
                weather.getTemperature(), isMetric));

        // Set feels like
        String feelsLikeText = getString(R.string.feels_like,
                WeatherUtils.formatTemperature(weather.getFeelsLike(), isMetric));
        binding.tvFeelsLike.setText(feelsLikeText);

        // Set weather description
        binding.tvWeatherDescription.setText(
                WeatherUtils.formatWeatherDescription(weather.getWeatherDescription()));

        // Set weather icon using the new method
        WeatherUtils.loadWeatherIcon(this, weather.getWeatherIcon(), binding.ivWeatherIcon);

        // Set humidity
        String humidityText = getString(R.string.humidity,
                WeatherUtils.formatHumidity(weather.getHumidity()));
        binding.tvHumidity.setText(humidityText);

        // Set wind
        String windText = getString(R.string.wind,
                WeatherUtils.formatWindSpeed(weather.getWindSpeed(), isMetric));
        binding.tvWind.setText(windText);

        // Set pressure
        String pressureText = getString(R.string.pressure, weather.getPressure());
        binding.tvPressure.setText(pressureText);
    }

    private void refreshWeatherData() {
        if (currentLat != 0.0 && currentLon != 0.0) {
            loadWeatherData(currentLat, currentLon);
        } else {
            loadInitialLocation();
        }
    }

    private void showLoading(boolean isLoading) {
        binding.swipeRefreshLayout.setRefreshing(isLoading);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                requestCurrentLocation();
            } else {
                // Permission denied
                showLocationPermissionDeniedMessage();
            }
        }
    }

    private void showLocationPermissionDeniedMessage() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.location_permission_title)
                .setMessage(R.string.location_permission_denied)
                .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                    locationUtils.requestLocationPermission(MainActivity.this);
                })
                .setNegativeButton(R.string.deny, (dialog, which) -> {
                    // Use last saved location or default
                    double lat = preferencesManager.getLastLocationLatitude();
                    double lon = preferencesManager.getLastLocationLongitude();
                    if (lat != 0.0 && lon != 0.0) {
                        loadWeatherData(lat, lon);
                    } else {
                        loadWeatherData(48.8566, 2.3522); // Paris
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Open settings (not implemented yet)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show exit confirmation dialog
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.exit_confirmation_title)
                        .setMessage(R.string.exit_confirmation_message)
                        .setPositiveButton(R.string.exit_yes, (dialog, which) -> {
                            // User confirms exit, remove callback and trigger system back
                            this.setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        })
                        .setNegativeButton(R.string.exit_no, (dialog, which) -> {
                            // User cancels, do nothing
                            dialog.dismiss();
                        })
                        .setCancelable(false) // Prevent dismissing by clicking outside
                        .show();
            }
        });
    }
}