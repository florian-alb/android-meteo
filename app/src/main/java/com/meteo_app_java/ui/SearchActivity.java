package com.meteo_app_java.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.meteo_app_java.R;
import com.meteo_app_java.data.WeatherRepository;
import com.meteo_app_java.data.remote.response.LocationSearchResponse;
import com.meteo_app_java.databinding.ActivitySearchBinding;
import com.meteo_app_java.models.SavedLocation;
import com.meteo_app_java.ui.adapters.LocationWithGpsAdapter;
import com.meteo_app_java.utils.LocationUtils;
import com.meteo_app_java.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private WeatherRepository weatherRepository;
    private LocationWithGpsAdapter locationAdapter;
    private PreferencesManager preferencesManager;
    private LocationUtils locationUtils;
    private double currentLat;
    private double currentLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize repository
        weatherRepository = WeatherRepository.getInstance();
        preferencesManager = new PreferencesManager(this);
        locationUtils = new LocationUtils(this);

        // Get last known coordinates
        currentLat = preferencesManager.getLastLocationLatitude();
        currentLon = preferencesManager.getLastLocationLongitude();

        // If no saved location, use default coordinates (will be updated later)
        if (currentLat == 0 && currentLon == 0) {
            currentLat = 48.8566;
            currentLon = 2.3522; // Paris as default
        }

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup search functionality
        setupSearchFunctionality();

        // Setup click listeners
        setupClickListeners();

        // Load saved locations
        loadSavedLocations();

        // Update GPS coordinates if needed
        if (preferencesManager.shouldUseDeviceLocation() && locationUtils.hasLocationPermission()) {
            updateCurrentLocation();
        }
    }

    private void updateCurrentLocation() {
        locationUtils.getLastLocation(new LocationUtils.OnLocationResultListener() {
            @Override
            public void onLocationResult(android.location.Location location) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                locationAdapter.updateGpsCoordinates(currentLat, currentLon);
            }

            @Override
            public void onLocationFailed(String message) {
                Toast.makeText(SearchActivity.this, R.string.error_location, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        locationAdapter = new LocationWithGpsAdapter(currentLat, currentLon);
        binding.rvLocations.setAdapter(locationAdapter);
        binding.rvLocations.setLayoutManager(new LinearLayoutManager(this));

        // Set click listener on adapter
        locationAdapter.setOnItemClickListener(new LocationWithGpsAdapter.OnItemClickListener() {
            @Override
            public void onGpsLocationClick() {
                // Start MainActivity with GPS location
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                intent.putExtra("latitude", currentLat);
                intent.putExtra("longitude", currentLon);
                intent.putExtra("useDeviceLocation", true);
                startActivity(intent);
                finish();
            }

            @Override
            public void onLocationClick(SavedLocation location) {
                // Start MainActivity with selected location
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                intent.putExtra("latitude", location.getLatitude());
                intent.putExtra("longitude", location.getLongitude());
                intent.putExtra("city", location.getCityName());
                intent.putExtra("useDeviceLocation", false);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFavoriteClick(SavedLocation location) {
                // Toggle favorite status
                weatherRepository.toggleFavorite(location);
            }

            @Override
            public void onRemoveClick(SavedLocation location) {
                // Remove location
                weatherRepository.deleteLocation(location);
            }
        });
    }

    private void setupSearchFunctionality() {
        // Handle keyboard search action
        binding.etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(binding.etSearch.getText().toString());
                    return true;
                }
                return false;
            }
        });

        // Handle search button click
        binding.btnSearch.setOnClickListener(v -> performSearch(binding.etSearch.getText().toString()));
    }

    private void setupClickListeners() {
        binding.tvClearAll.setOnClickListener(v -> {
            // Clear all saved locations
            weatherRepository.deleteAllLocations();
        });
    }

    private void loadSavedLocations() {
        weatherRepository.getRecentLocations().observe(this, new Observer<List<SavedLocation>>() {
            @Override
            public void onChanged(List<SavedLocation> locations) {
                if (locations != null && !locations.isEmpty()) {
                    binding.tvNoLocations.setVisibility(View.GONE);
                    locationAdapter.setLocations(locations);
                } else {
                    binding.tvNoLocations.setVisibility(View.GONE); // Always hide since we now have GPS
                    locationAdapter.setLocations(new ArrayList<>());
                }
            }
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            return;
        }

        showLoading(true);

        weatherRepository.searchLocationByName(query).observe(this,
                new Observer<List<LocationSearchResponse.LocationResult>>() {
                    @Override
                    public void onChanged(List<LocationSearchResponse.LocationResult> results) {
                        showLoading(false);

                        if (results != null && !results.isEmpty()) {
                            // Process the first result
                            LocationSearchResponse.LocationResult result = results.get(0);

                            // Save location to database
                            SavedLocation location = new SavedLocation();
                            location.setCityName(result.name);
                            location.setCountryCode(result.country);
                            location.setLatitude(result.lat);
                            location.setLongitude(result.lon);
                            location.setLastAccessed(new java.util.Date());

                            weatherRepository.saveLocation(location.getCityName(),
                                    location.getCountryCode(),
                                    location.getLatitude(),
                                    location.getLongitude());

                            // Start MainActivity with selected location
                            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                            intent.putExtra("latitude", result.lat);
                            intent.putExtra("longitude", result.lon);
                            intent.putExtra("city", result.name);
                            intent.putExtra("useDeviceLocation", false);
                            startActivity(intent);
                            finish();
                        } else {
                            // No results found
                            Toast.makeText(SearchActivity.this, R.string.search_no_results,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}