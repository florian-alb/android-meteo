package com.meteo_app_java.data.remote;

import com.meteo_app_java.data.remote.response.CurrentWeatherResponse;
import com.meteo_app_java.data.remote.response.HourlyForecastResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for OpenWeatherMap API v2.5 calls.
 */
public interface WeatherApiService {

    /**
     * Get current weather for a location by coordinates.
     *
     * @param lat   Latitude
     * @param lon   Longitude
     * @param appid API key
     * @param units Units (metric, imperial)
     * @return Current weather data
     */
    @GET("weather")
    Call<CurrentWeatherResponse> getCurrentWeatherByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String appid,
            @Query("units") String units);

    /**
     * Get current weather for a location by city name.
     *
     * @param q     City name
     * @param appid API key
     * @param units Units (metric, imperial)
     * @return Current weather data
     */
    @GET("weather")
    Call<CurrentWeatherResponse> getCurrentWeatherByCity(
            @Query("q") String q,
            @Query("appid") String appid,
            @Query("units") String units);

    /**
     * Get 5-day forecast with 3-hour step (using API v2.5).
     *
     * @param lat   Latitude
     * @param lon   Longitude
     * @param appid API key
     * @param units Units (metric, imperial)
     * @return 5-day forecast data
     */
    @GET("forecast")
    Call<HourlyForecastResponse> getHourlyForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String appid,
            @Query("units") String units);
}