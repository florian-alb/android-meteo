package com.meteo_app_java.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.meteo_app_java.MeteoApplication;
import com.meteo_app_java.data.local.DailyForecastDao;
import com.meteo_app_java.data.local.HourlyForecastDao;
import com.meteo_app_java.data.local.MeteoDatabase;
import com.meteo_app_java.data.local.SavedLocationDao;
import com.meteo_app_java.data.local.WeatherDao;
import com.meteo_app_java.data.remote.GeoApiService;
import com.meteo_app_java.data.remote.WeatherApiClient;
import com.meteo_app_java.data.remote.WeatherApiService;
import com.meteo_app_java.data.remote.WeatherApiServiceV3;
import com.meteo_app_java.data.remote.response.CurrentWeatherResponse;
import com.meteo_app_java.data.remote.response.DailyForecastResponse;
import com.meteo_app_java.data.remote.response.HourlyForecastResponse;
import com.meteo_app_java.data.remote.response.LocationSearchResponse;
import com.meteo_app_java.models.DailyForecast;
import com.meteo_app_java.models.HourlyForecast;
import com.meteo_app_java.models.SavedLocation;
import com.meteo_app_java.models.Weather;
import com.meteo_app_java.utils.FileUtils;
import com.meteo_app_java.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository class that manages the data access from different sources.
 */
public class WeatherRepository {
    private static final String TAG = "WeatherRepository";

    // Cache file names
    private static final String CURRENT_WEATHER_CACHE = "current_weather_cache.json";
    private static final String HOURLY_FORECAST_CACHE = "hourly_forecast_cache.json";
    private static final String DAILY_FORECAST_CACHE = "daily_forecast_cache.json";

    private static WeatherRepository instance;

    private final WeatherApiService weatherApiService;
    private final WeatherApiServiceV3 weatherApiServiceV3;
    private final GeoApiService geoApiService;
    private final WeatherDao weatherDao;
    private final HourlyForecastDao hourlyForecastDao;
    private final DailyForecastDao dailyForecastDao;
    private final SavedLocationDao savedLocationDao;
    private final PreferencesManager preferencesManager;
    private final FirebaseFirestore firestore;
    private final Executor executor;
    private final Context context;

    private WeatherRepository() {
        weatherApiService = WeatherApiClient.getInstance().getWeatherApiServiceV2_5();
        weatherApiServiceV3 = WeatherApiClient.getInstance().getWeatherApiServiceV3_0();
        geoApiService = WeatherApiClient.getInstance().getGeoApiService();
        MeteoDatabase database = MeteoApplication.getDatabase();
        weatherDao = database.weatherDao();
        hourlyForecastDao = database.hourlyForecastDao();
        dailyForecastDao = database.dailyForecastDao();
        savedLocationDao = database.savedLocationDao();
        preferencesManager = MeteoApplication.getPreferencesManager();

        // Get Firebase Firestore if available
        if (MeteoApplication.isFirebaseAvailable()) {
            firestore = MeteoApplication.getFirebaseFirestore();
        } else {
            firestore = null;
            Log.w(TAG, "Firebase not available. Firebase features will be disabled.");
        }

        executor = Executors.newFixedThreadPool(3);
        context = MeteoApplication.getAppContext();
    }

    public static synchronized WeatherRepository getInstance() {
        if (instance == null) {
            instance = new WeatherRepository();
        }
        return instance;
    }

    /**
     * Get current weather by coordinates from API.
     */
    public LiveData<Weather> getCurrentWeatherByCoordinates(double lat, double lon) {
        MutableLiveData<Weather> result = new MutableLiveData<>();

        // Get unit preference
        String units = preferencesManager.isUsingMetricUnits() ? "metric" : "imperial";

        // Make API call
        weatherApiService.getCurrentWeatherByCoordinates(lat, lon,
                WeatherApiClient.getInstance().getApiKey(), units)
                .enqueue(new Callback<CurrentWeatherResponse>() {
                    @Override
                    public void onResponse(Call<CurrentWeatherResponse> call,
                            Response<CurrentWeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Cache response
                            cacheCurrentWeatherResponse(response.body());

                            // Convert to entity and save to database
                            Weather weather = convertToWeatherEntity(response.body());
                            saveWeatherToDatabase(weather);

                            // Log to Firebase if available
                            if (MeteoApplication.isFirebaseAvailable()) {
                                logWeatherRequestToFirebase(weather);
                            }

                            // Update last location in preferences
                            preferencesManager.saveLastLocation(lat, lon, weather.getCityName());

                            result.setValue(weather);
                        } else {
                            Log.e(TAG, "Error fetching current weather: " + response.message());
                            // Try to get data from cache
                            Weather cachedWeather = getWeatherFromCache();
                            if (cachedWeather != null) {
                                result.setValue(cachedWeather);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        // Try to get data from cache
                        Weather cachedWeather = getWeatherFromCache();
                        if (cachedWeather != null) {
                            result.setValue(cachedWeather);
                        }
                    }
                });

        return result;
    }

    /**
     * Get current weather by city name from API.
     */
    public LiveData<Weather> getCurrentWeatherByCity(String cityName) {
        MutableLiveData<Weather> result = new MutableLiveData<>();

        // Get unit preference
        String units = preferencesManager.isUsingMetricUnits() ? "metric" : "imperial";

        // Make API call
        weatherApiService.getCurrentWeatherByCity(cityName,
                WeatherApiClient.getInstance().getApiKey(), units)
                .enqueue(new Callback<CurrentWeatherResponse>() {
                    @Override
                    public void onResponse(Call<CurrentWeatherResponse> call,
                            Response<CurrentWeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Cache response
                            cacheCurrentWeatherResponse(response.body());

                            // Convert to entity and save to database
                            Weather weather = convertToWeatherEntity(response.body());
                            saveWeatherToDatabase(weather);

                            // Add to saved locations
                            saveLocation(weather.getCityName(), weather.getCountryCode(),
                                    weather.getLatitude(), weather.getLongitude());

                            // Log to Firebase if available
                            if (MeteoApplication.isFirebaseAvailable()) {
                                logWeatherRequestToFirebase(weather);
                            }

                            // Update last location in preferences
                            preferencesManager.saveLastLocation(weather.getLatitude(),
                                    weather.getLongitude(), weather.getCityName());

                            result.setValue(weather);
                        } else {
                            Log.e(TAG, "Error fetching current weather: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                    }
                });

        return result;
    }

    /**
     * Get hourly forecast by coordinates from API.
     */
    public LiveData<List<HourlyForecast>> getHourlyForecast(double lat, double lon) {
        MutableLiveData<List<HourlyForecast>> result = new MutableLiveData<>();

        // Get unit preference
        String units = preferencesManager.isUsingMetricUnits() ? "metric" : "imperial";

        // Make API call
        weatherApiService.getHourlyForecast(lat, lon,
                WeatherApiClient.getInstance().getApiKey(), units)
                .enqueue(new Callback<HourlyForecastResponse>() {
                    @Override
                    public void onResponse(Call<HourlyForecastResponse> call,
                            Response<HourlyForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Cache response
                            cacheHourlyForecastResponse(response.body());

                            // Convert to entities and save to database
                            List<HourlyForecast> forecasts = convertToHourlyForecastEntities(response.body());
                            saveHourlyForecastsToDatabase(forecasts);

                            result.setValue(forecasts);
                        } else {
                            Log.e(TAG, "Error fetching hourly forecast: " + response.message());
                            // Try to get data from cache
                            List<HourlyForecast> cachedForecasts = getHourlyForecastsFromCache();
                            if (cachedForecasts != null && !cachedForecasts.isEmpty()) {
                                result.setValue(cachedForecasts);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        // Try to get data from cache
                        List<HourlyForecast> cachedForecasts = getHourlyForecastsFromCache();
                        if (cachedForecasts != null && !cachedForecasts.isEmpty()) {
                            result.setValue(cachedForecasts);
                        }
                    }
                });

        return result;
    }

    /**
     * Get daily forecast by coordinates from API.
     */
    public LiveData<List<DailyForecast>> getDailyForecast(double lat, double lon) {
        MutableLiveData<List<DailyForecast>> result = new MutableLiveData<>();

        // Get unit preference
        String units = preferencesManager.isUsingMetricUnits() ? "metric" : "imperial";

        // Make API call using v3.0 service
        weatherApiServiceV3.getDailyForecast(lat, lon,
                WeatherApiClient.getInstance().getApiKey(), units,
                "minutely,alerts")
                .enqueue(new Callback<DailyForecastResponse>() {
                    @Override
                    public void onResponse(Call<DailyForecastResponse> call, Response<DailyForecastResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Cache response
                            cacheDailyForecastResponse(response.body());

                            // Convert to entities and save to database
                            List<DailyForecast> forecasts = convertToDailyForecastEntities(response.body());
                            saveDailyForecastsToDatabase(forecasts);

                            result.setValue(forecasts);
                        } else {
                            Log.e(TAG, "Error fetching daily forecast: " + response.message());
                            // Try to get data from cache or database
                            List<DailyForecast> cachedForecasts = getDailyForecastsFromCache();
                            if (!cachedForecasts.isEmpty()) {
                                result.setValue(cachedForecasts);
                            } else {
                                // If cache is empty, try to get from database
                                executor.execute(() -> {
                                    List<DailyForecast> dbForecasts = dailyForecastDao.getRecentForecasts(10);
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        result.setValue(dbForecasts.isEmpty() ? new ArrayList<>() : dbForecasts);
                                    });
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<DailyForecastResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        // Try to get data from cache or database
                        List<DailyForecast> cachedForecasts = getDailyForecastsFromCache();
                        if (!cachedForecasts.isEmpty()) {
                            result.setValue(cachedForecasts);
                        } else {
                            // If cache is empty, try to get from database
                            executor.execute(() -> {
                                List<DailyForecast> dbForecasts = dailyForecastDao.getRecentForecasts(10);
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    result.setValue(dbForecasts.isEmpty() ? new ArrayList<>() : dbForecasts);
                                });
                            });
                        }
                    }
                });

        return result;
    }

    /**
     * Search for a location by name from API.
     */
    public LiveData<List<LocationSearchResponse.LocationResult>> searchLocationByName(String query) {
        MutableLiveData<List<LocationSearchResponse.LocationResult>> result = new MutableLiveData<>();

        // Make API call using the GeoApiService
        geoApiService.searchLocationByName(query, 5,
                WeatherApiClient.getInstance().getApiKey())
                .enqueue(new Callback<List<LocationSearchResponse.LocationResult>>() {
                    @Override
                    public void onResponse(Call<List<LocationSearchResponse.LocationResult>> call,
                            Response<List<LocationSearchResponse.LocationResult>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            result.setValue(response.body());
                        } else {
                            Log.e(TAG, "Error searching locations: " + response.message());
                            result.setValue(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<LocationSearchResponse.LocationResult>> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage());
                        result.setValue(new ArrayList<>());
                    }
                });

        return result;
    }

    // Database operations

    /**
     * Save a location to the database.
     */
    public void saveLocation(String cityName, String countryCode, double latitude, double longitude) {
        executor.execute(() -> {
            SavedLocation location = new SavedLocation();
            location.setCityName(cityName);
            location.setCountryCode(countryCode);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setLastAccessed(new Date());

            savedLocationDao.insert(location);
        });
    }

    /**
     * Get recent locations from the database.
     */
    public LiveData<List<SavedLocation>> getRecentLocations() {
        return savedLocationDao.getRecentLocations();
    }

    /**
     * Get favorite locations from the database.
     */
    public LiveData<List<SavedLocation>> getFavoriteLocations() {
        return savedLocationDao.getFavoriteLocations();
    }

    /**
     * Toggle favorite status for a location.
     */
    public void toggleFavorite(SavedLocation location) {
        executor.execute(() -> {
            location.setFavorite(!location.isFavorite());
            savedLocationDao.update(location);
        });
    }

    /**
     * Delete a location from the database.
     */
    public void deleteLocation(SavedLocation location) {
        executor.execute(() -> {
            savedLocationDao.delete(location);
        });
    }

    /**
     * Delete all saved locations from the database.
     */
    public void deleteAllLocations() {
        executor.execute(() -> {
            savedLocationDao.deleteAll();
        });
    }

    // Conversion methods

    /**
     * Convert API response to Weather entity.
     */
    private Weather convertToWeatherEntity(CurrentWeatherResponse response) {
        Weather weather = new Weather();
        weather.setCityName(response.name);
        weather.setCountryCode(response.sys.country);
        weather.setLatitude(response.coord.lat);
        weather.setLongitude(response.coord.lon);
        weather.setTemperature(response.main.temp);
        weather.setFeelsLike(response.main.feelsLike);
        weather.setTempMin(response.main.tempMin);
        weather.setTempMax(response.main.tempMax);
        weather.setHumidity(response.main.humidity);
        weather.setPressure(response.main.pressure);
        weather.setWindSpeed(response.wind.speed);
        weather.setWindDegree(response.wind.deg);

        if (response.weather != null && !response.weather.isEmpty()) {
            weather.setWeatherMain(response.weather.get(0).main);
            weather.setWeatherDescription(response.weather.get(0).description);
            weather.setWeatherIcon(response.weather.get(0).icon);
        }

        weather.setTimestamp(response.dt);
        weather.setLastUpdated(new Date());

        return weather;
    }

    /**
     * Convert API response to list of HourlyForecast entities.
     */
    private List<HourlyForecast> convertToHourlyForecastEntities(HourlyForecastResponse response) {
        List<HourlyForecast> forecasts = new ArrayList<>();

        // Get the location ID (use 1 as default if not available)
        int locationId = 1;

        // Convert each hourly forecast
        for (HourlyForecastResponse.HourlyForecast item : response.list) {
            HourlyForecast forecast = new HourlyForecast();
            forecast.setLocationId(locationId);
            forecast.setTimestamp(item.dt);
            forecast.setTemperature(item.main.temp);
            forecast.setFeelsLike(item.main.feelsLike);
            forecast.setHumidity(item.main.humidity);
            forecast.setPressure(item.main.pressure);
            forecast.setWindSpeed(item.wind.speed);
            forecast.setWindDegree(item.wind.deg);
            forecast.setPop((int) (item.pop * 100)); // Convert to percentage

            if (item.rain != null) {
                forecast.setRainVolume(item.rain.threeHour);
            }

            if (item.weather != null && !item.weather.isEmpty()) {
                forecast.setWeatherMain(item.weather.get(0).main);
                forecast.setWeatherDescription(item.weather.get(0).description);
                forecast.setWeatherIcon(item.weather.get(0).icon);
            }

            forecasts.add(forecast);
        }

        return forecasts;
    }

    /**
     * Convert API response to list of DailyForecast entities.
     */
    private List<DailyForecast> convertToDailyForecastEntities(DailyForecastResponse response) {
        List<DailyForecast> forecasts = new ArrayList<>();

        // Get the location ID (use 1 as default if not available)
        int locationId = 1;

        // Convert each daily forecast
        for (DailyForecastResponse.Daily item : response.daily) {
            DailyForecast forecast = new DailyForecast();
            forecast.setLocationId(locationId);
            forecast.setTimestamp(item.dt);
            forecast.setTempDay(item.temp.day);
            forecast.setTempNight(item.temp.night);
            forecast.setTempMin(item.temp.min);
            forecast.setTempMax(item.temp.max);
            forecast.setFeelsLikeDay(item.feelsLike.day);
            forecast.setFeelsLikeNight(item.feelsLike.night);
            forecast.setHumidity(item.humidity);
            forecast.setPressure(item.pressure);
            forecast.setWindSpeed(item.windSpeed);
            forecast.setWindDegree(item.windDeg);
            forecast.setPop((int) (item.pop * 100)); // Convert to percentage
            forecast.setRainVolume(item.rain);
            forecast.setSunrise(item.sunrise);
            forecast.setSunset(item.sunset);

            if (item.weather != null && !item.weather.isEmpty()) {
                forecast.setWeatherMain(item.weather.get(0).main);
                forecast.setWeatherDescription(item.weather.get(0).description);
                forecast.setWeatherIcon(item.weather.get(0).icon);
            }

            forecasts.add(forecast);
        }

        return forecasts;
    }

    // Database save methods

    /**
     * Save weather entity to database.
     */
    private void saveWeatherToDatabase(Weather weather) {
        executor.execute(() -> {
            long id = weatherDao.insert(weather);
            Log.d(TAG, "Weather saved to database with ID: " + id);
        });
    }

    /**
     * Save hourly forecasts to database.
     */
    private void saveHourlyForecastsToDatabase(List<HourlyForecast> forecasts) {
        executor.execute(() -> {
            hourlyForecastDao.insertAll(forecasts);
            Log.d(TAG, "Saved " + forecasts.size() + " hourly forecasts to database");
        });
    }

    /**
     * Save daily forecasts to database.
     */
    private void saveDailyForecastsToDatabase(List<DailyForecast> forecasts) {
        executor.execute(() -> {
            dailyForecastDao.insertAll(forecasts);
            Log.d(TAG, "Saved " + forecasts.size() + " daily forecasts to database");
        });
    }

    // Cache methods

    /**
     * Cache the current weather response to a file.
     */
    private void cacheCurrentWeatherResponse(CurrentWeatherResponse response) {
        executor.execute(() -> {
            String json = new Gson().toJson(response);
            boolean success = FileUtils.writeToFile(context, CURRENT_WEATHER_CACHE, json);
            Log.d(TAG, "Cached current weather to file: " + success);
        });
    }

    /**
     * Cache the hourly forecast response to a file.
     */
    private void cacheHourlyForecastResponse(HourlyForecastResponse response) {
        executor.execute(() -> {
            String json = new Gson().toJson(response);
            boolean success = FileUtils.writeToFile(context, HOURLY_FORECAST_CACHE, json);
            Log.d(TAG, "Cached hourly forecast to file: " + success);
        });
    }

    /**
     * Cache the daily forecast response to a file.
     */
    private void cacheDailyForecastResponse(DailyForecastResponse response) {
        executor.execute(() -> {
            String json = new Gson().toJson(response);
            boolean success = FileUtils.writeToFile(context, DAILY_FORECAST_CACHE, json);
            Log.d(TAG, "Cached daily forecast to file: " + success);
        });
    }

    /**
     * Get weather from the cache file.
     */
    private Weather getWeatherFromCache() {
        String json = FileUtils.readFromFile(context, CURRENT_WEATHER_CACHE);
        if (json != null && !json.trim().isEmpty()) {
            try {
                CurrentWeatherResponse response = new Gson().fromJson(json, CurrentWeatherResponse.class);
                if (response != null) {
                    return convertToWeatherEntity(response);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing weather cache", e);
            }
        }
        return null;
    }

    /**
     * Get hourly forecasts from the cache file.
     */
    private List<HourlyForecast> getHourlyForecastsFromCache() {
        String json = FileUtils.readFromFile(context, HOURLY_FORECAST_CACHE);
        if (json != null && !json.trim().isEmpty()) {
            try {
                HourlyForecastResponse response = new Gson().fromJson(json, HourlyForecastResponse.class);
                if (response != null && response.list != null && !response.list.isEmpty()) {
                    return convertToHourlyForecastEntities(response);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing hourly forecast cache", e);
            }
        }
        // Return empty list instead of null
        return new ArrayList<>();
    }

    /**
     * Get daily forecasts from the cache file.
     */
    private List<DailyForecast> getDailyForecastsFromCache() {
        String json = FileUtils.readFromFile(context, DAILY_FORECAST_CACHE);
        if (json != null && !json.trim().isEmpty()) {
            try {
                DailyForecastResponse response = new Gson().fromJson(json, DailyForecastResponse.class);
                if (response != null && response.daily != null && !response.daily.isEmpty()) {
                    return convertToDailyForecastEntities(response);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing daily forecast cache", e);
            }
        }
        // Return empty list instead of null
        return new ArrayList<>();
    }

    /**
     * Log weather request to Firebase.
     */
    private void logWeatherRequestToFirebase(Weather weather) {
        Map<String, Object> log = new HashMap<>();
        log.put("city", weather.getCityName());
        log.put("country", weather.getCountryCode());
        log.put("lat", weather.getLatitude());
        log.put("lon", weather.getLongitude());
        log.put("timestamp", new Date());

        if (firestore != null) {
            firestore.collection("weather_requests")
                    .add(log)
                    .addOnSuccessListener(
                            documentReference -> Log.d(TAG, "Logged to Firebase: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error logging to Firebase", e));
        } else {
            Log.w(TAG, "Firebase not available. Weather request not logged to Firebase.");
        }
    }
}