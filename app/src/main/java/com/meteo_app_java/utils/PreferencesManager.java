package com.meteo_app_java.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.meteo_app_java.models.Weather;

/**
 * Utility class to manage SharedPreferences operations.
 */
public class PreferencesManager {
    private static final String PREFERENCES_NAME = "MeteoPreferences";

    // Preference keys
    private static final String KEY_USE_METRIC = "use_metric_units";
    private static final String KEY_LAST_LOCATION_LAT = "last_location_lat";
    private static final String KEY_LAST_LOCATION_LON = "last_location_lon";
    private static final String KEY_LAST_LOCATION_NAME = "last_location_name";
    private static final String KEY_LAST_UPDATED = "last_updated";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_USE_LOCATION = "use_device_location";

    private SharedPreferences preferences;

    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if the app is using metric units (°C, m/s, etc.).
     */
    public boolean isUsingMetricUnits() {
        return preferences.getBoolean(KEY_USE_METRIC, true);
    }

    /**
     * Set whether to use metric units.
     */
    public void setUseMetricUnits(boolean useMetric) {
        preferences.edit().putBoolean(KEY_USE_METRIC, useMetric).apply();
    }

    /**
     * Get the last location latitude.
     */
    public double getLastLocationLatitude() {
        return Double.longBitsToDouble(preferences.getLong(KEY_LAST_LOCATION_LAT, Double.doubleToLongBits(0.0)));
    }

    /**
     * Get the last location longitude.
     */
    public double getLastLocationLongitude() {
        return Double.longBitsToDouble(preferences.getLong(KEY_LAST_LOCATION_LON, Double.doubleToLongBits(0.0)));
    }

    /**
     * Gets the last saved temperature
     * @return The temperature value
     */
    public double getLastTemperature() {
        return preferences.getFloat("last_temperature", 0);
    }

    /**
     * Gets the last saved location name
     * @return The location name
     */
    public String getLastLocationName() {
        return preferences.getString("last_location_name", "Unknown");
    }

    /**
     * Gets the last saved weather description
     * @return The weather description
     */
    public String getLastWeatherDescription() {
        return preferences.getString("last_weather_description", "Clear");
    }

    /**
     * Gets the last saved weather icon code
     * @return The icon code (e.g., "01d")
     */
    public String getLastWeatherIcon() {
        return preferences.getString("last_weather_icon", "01d");
    }

    /**
     * Gets the timestamp when weather data was last updated
     * @return The timestamp in milliseconds
     */
    public long getLastUpdated() {
        return preferences.getLong("last_updated", System.currentTimeMillis());
    }

    /**
     * Save the last used location.
     */
    public void saveLastLocation(double latitude, double longitude, String locationName) {
        preferences.edit()
                .putLong(KEY_LAST_LOCATION_LAT, Double.doubleToRawLongBits(latitude))
                .putLong(KEY_LAST_LOCATION_LON, Double.doubleToRawLongBits(longitude))
                .putString(KEY_LAST_LOCATION_NAME, locationName)
                .putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
                .apply();
    }

    /**
     * Check if this is the first launch of the application.
     */
    public boolean isFirstLaunch() {
        boolean isFirst = preferences.getBoolean(KEY_FIRST_LAUNCH, true);
        if (isFirst) {
            preferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        }
        return isFirst;
    }

    /**
     * Store the OpenWeatherMap API key.
     */
    public void saveApiKey(String apiKey) {
        preferences.edit().putString(KEY_API_KEY, apiKey).apply();
    }

    /**
     * Get the stored API key.
     */
    public String getApiKey() {
        return preferences.getString(KEY_API_KEY, "5796abbde9106b7da4febfae8c44c232");
    }

    /**
     * Check if the app should use the device's location.
     */
    public boolean shouldUseDeviceLocation() {
        return preferences.getBoolean(KEY_USE_LOCATION, true);
    }

    /**
     * Set whether to use the device's location.
     */
    public void setUseDeviceLocation(boolean useLocation) {
        preferences.edit().putBoolean(KEY_USE_LOCATION, useLocation).apply();
    }

    /**
     * Clear all preferences.
     */
    public void clearPreferences() {
        preferences.edit().clear().apply();
    }

    /**
     * Saves the last weather data for use in the widget
     * @param weather The weather object containing data to save
     */
    public void saveLastWeatherData(Weather weather) {
        if (weather == null) return;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("last_location_name", weather.getCityName());
        editor.putFloat("last_temperature", (float) weather.getTemperature());
        editor.putString("last_weather_description", weather.getWeatherDescription());
        editor.putString("last_weather_icon", weather.getWeatherIcon());
        editor.putLong("last_updated", System.currentTimeMillis());
        editor.apply();
    }
}