package com.meteo_app_java.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to manage SharedPreferences operations.
 */
public class PreferencesManager {
    private static final String PREF_NAME = "meteo_app_preferences";

    // Preference keys
    private static final String KEY_USE_METRIC = "use_metric_units";
    private static final String KEY_LAST_LOCATION_LAT = "last_location_lat";
    private static final String KEY_LAST_LOCATION_LON = "last_location_lon";
    private static final String KEY_LAST_LOCATION_NAME = "last_location_name";
    private static final String KEY_LAST_UPDATED = "last_updated";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_USE_LOCATION = "use_device_location";

    private final SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if the app is using metric units (°C, m/s, etc.).
     */
    public boolean isUsingMetricUnits() {
        return sharedPreferences.getBoolean(KEY_USE_METRIC, true);
    }

    /**
     * Set whether to use metric units.
     */
    public void setUseMetricUnits(boolean useMetric) {
        sharedPreferences.edit().putBoolean(KEY_USE_METRIC, useMetric).apply();
    }

    /**
     * Get the last location latitude.
     */
    public double getLastLocationLatitude() {
        return Double.longBitsToDouble(sharedPreferences.getLong(KEY_LAST_LOCATION_LAT, Double.doubleToLongBits(0.0)));
    }

    /**
     * Get the last location longitude.
     */
    public double getLastLocationLongitude() {
        return Double.longBitsToDouble(sharedPreferences.getLong(KEY_LAST_LOCATION_LON, Double.doubleToLongBits(0.0)));
    }

    /**
     * Get the last location name.
     */
    public String getLastLocationName() {
        return sharedPreferences.getString(KEY_LAST_LOCATION_NAME, null);
    }

    /**
     * Save the last used location.
     */
    public void saveLastLocation(double latitude, double longitude, String locationName) {
        sharedPreferences.edit()
                .putLong(KEY_LAST_LOCATION_LAT, Double.doubleToRawLongBits(latitude))
                .putLong(KEY_LAST_LOCATION_LON, Double.doubleToRawLongBits(longitude))
                .putString(KEY_LAST_LOCATION_NAME, locationName)
                .putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                .apply();
    }

    /**
     * Get the timestamp when the data was last updated.
     */
    public long getLastUpdated() {
        return sharedPreferences.getLong(KEY_LAST_UPDATED, 0L);
    }

    /**
     * Check if this is the first launch of the application.
     */
    public boolean isFirstLaunch() {
        boolean isFirst = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true);
        if (isFirst) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        }
        return isFirst;
    }

    /**
     * Store the OpenWeatherMap API key.
     */
    public void saveApiKey(String apiKey) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply();
    }

    /**
     * Get the stored API key.
     */
    public String getApiKey() {
        return sharedPreferences.getString(KEY_API_KEY, "5796abbde9106b7da4febfae8c44c232");
    }

    /**
     * Check if the app should use the device's location.
     */
    public boolean shouldUseDeviceLocation() {
        return sharedPreferences.getBoolean(KEY_USE_LOCATION, true);
    }

    /**
     * Set whether to use the device's location.
     */
    public void setUseDeviceLocation(boolean useLocation) {
        sharedPreferences.edit().putBoolean(KEY_USE_LOCATION, useLocation).apply();
    }

    /**
     * Clear all preferences.
     */
    public void clearPreferences() {
        sharedPreferences.edit().clear().apply();
    }
}