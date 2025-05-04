package com.meteo_app_java.data.remote;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.meteo_app_java.MeteoApplication;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API client for OpenWeatherMap services.
 */
public class WeatherApiClient {
    private static final String TAG = "WeatherApiClient";

    // Base URL for OpenWeatherMap API v2.5
    private static final String BASE_URL_V2_5 = "https://api.openweathermap.org/data/2.5/";

    // Base URL for Geo API
    private static final String BASE_URL_GEO = "http://api.openweathermap.org/geo/1.0/";

    // Base URL for OpenWeatherMap API v3.0
    private static final String BASE_URL_V3_0 = "https://api.openweathermap.org/data/3.0/";

    private static WeatherApiClient instance;
    private final WeatherApiService weatherApiServiceV2_5;
    private final WeatherApiServiceV3 weatherApiServiceV3_0;
    private final GeoApiService geoApiService;

    private WeatherApiClient() {
        // Create OkHttpClient with logging
        OkHttpClient okHttpClient = createOkHttpClient();

        // Create Gson converter
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Initialize Retrofit for API v2.5
        Retrofit retrofitV2_5 = new Retrofit.Builder()
                .baseUrl(BASE_URL_V2_5)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Initialize Retrofit for Geo API
        Retrofit retrofitGeo = new Retrofit.Builder()
                .baseUrl(BASE_URL_GEO)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Initialize Retrofit for API v3.0
        Retrofit retrofitV3_0 = new Retrofit.Builder()
                .baseUrl(BASE_URL_V3_0)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Create service instances
        weatherApiServiceV2_5 = retrofitV2_5.create(WeatherApiService.class);
        weatherApiServiceV3_0 = retrofitV3_0.create(WeatherApiServiceV3.class);
        geoApiService = retrofitGeo.create(GeoApiService.class);

        Log.d(TAG, "WeatherApiClient initialized");
    }

    /**
     * Create and configure an OkHttpClient instance.
     */
    private OkHttpClient createOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * Get a singleton instance of the API client.
     */
    public static synchronized WeatherApiClient getInstance() {
        if (instance == null) {
            instance = new WeatherApiClient();
        }
        return instance;
    }

    /**
     * Get the API service for Geo API.
     */
    public GeoApiService getGeoApiService() {
        return geoApiService;
    }

    /**
     * Get the API service for OpenWeatherMap v2.5.
     */
    public WeatherApiService getWeatherApiServiceV2_5() {
        return weatherApiServiceV2_5;
    }

    /**
     * Get the API service for OpenWeatherMap v3.0.
     */
    public WeatherApiServiceV3 getWeatherApiServiceV3_0() {
        return weatherApiServiceV3_0;
    }

    /**
     * Get the API key from preferences.
     */
    public String getApiKey() {
        return MeteoApplication.getPreferencesManager().getApiKey();
    }
}