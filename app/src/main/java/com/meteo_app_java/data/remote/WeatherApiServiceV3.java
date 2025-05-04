package com.meteo_app_java.data.remote;

import com.meteo_app_java.data.remote.response.DailyForecastResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for OpenWeatherMap API v3.0 calls.
 */
public interface WeatherApiServiceV3 {

    /**
     * Get daily forecast (using API v3.0 OneCall).
     *
     * @param lat     Latitude
     * @param lon     Longitude
     * @param appid   API key
     * @param units   Units (metric, imperial)
     * @param exclude Parts to exclude from the response
     * @return Daily forecast data
     */
    @GET("onecall")
    Call<DailyForecastResponse> getDailyForecast(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String appid,
            @Query("units") String units,
            @Query("exclude") String exclude);
}