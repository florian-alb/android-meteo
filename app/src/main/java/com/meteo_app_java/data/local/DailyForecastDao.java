package com.meteo_app_java.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.meteo_app_java.models.DailyForecast;

import java.util.List;

/**
 * Data Access Object for the DailyForecast table.
 */
@Dao
public interface DailyForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DailyForecast> forecasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DailyForecast forecast);

    @Update
    void update(DailyForecast forecast);

    @Delete
    void delete(DailyForecast forecast);

    @Query("DELETE FROM daily_forecast WHERE locationId = :locationId")
    void deleteByLocationId(int locationId);

    @Query("DELETE FROM daily_forecast")
    void deleteAll();

    @Query("SELECT * FROM daily_forecast WHERE id = :id")
    LiveData<DailyForecast> getDailyForecastById(int id);

    @Query("SELECT * FROM daily_forecast WHERE locationId = :locationId ORDER BY timestamp ASC LIMIT 10")
    LiveData<List<DailyForecast>> getDailyForecastsByLocationId(int locationId);

    @Query("SELECT * FROM daily_forecast WHERE locationId = :locationId AND timestamp = :timestamp LIMIT 1")
    LiveData<DailyForecast> getDailyForecastByTimestamp(int locationId, long timestamp);

    @Query("SELECT * FROM daily_forecast ORDER BY timestamp DESC LIMIT :limit")
    List<DailyForecast> getRecentForecasts(int limit);
}