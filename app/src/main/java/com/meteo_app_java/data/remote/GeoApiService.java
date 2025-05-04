package com.meteo_app_java.data.remote;

import com.meteo_app_java.data.remote.response.LocationSearchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for OpenWeatherMap Geocoding API calls.
 */
public interface GeoApiService {

    /**
     * Search for a location by name.
     *
     * @param q     City name, state code (optional) and country code (optional)
     *              separated by comma
     * @param limit Maximum number of results (optional)
     * @param appid API key
     * @return List of matching locations
     */
    @GET("direct")
    Call<List<LocationSearchResponse.LocationResult>> searchLocationByName(
            @Query("q") String q,
            @Query("limit") int limit,
            @Query("appid") String appid);
}