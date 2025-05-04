package com.meteo_app_java.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.meteo_app_java.models.Weather;

import java.util.List;

/**
 * Data Access Object for the Weather table.
 */
@Dao
public interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Weather weather);

    @Update
    void update(Weather weather);

    @Delete
    void delete(Weather weather);

    @Query("DELETE FROM weather")
    void deleteAll();

    @Query("SELECT * FROM weather WHERE id = :id")
    LiveData<Weather> getWeatherById(int id);

    @Query("SELECT * FROM weather WHERE cityName LIKE :cityName LIMIT 1")
    LiveData<Weather> getWeatherByCity(String cityName);

    @Query("SELECT * FROM weather WHERE latitude = :lat AND longitude = :lon LIMIT 1")
    LiveData<Weather> getWeatherByCoordinates(double lat, double lon);

    @Query("SELECT * FROM weather WHERE isFavorite = 1")
    LiveData<List<Weather>> getFavoriteWeather();

    @Query("SELECT * FROM weather ORDER BY lastUpdated DESC LIMIT 10")
    LiveData<List<Weather>> getRecentWeather();
}