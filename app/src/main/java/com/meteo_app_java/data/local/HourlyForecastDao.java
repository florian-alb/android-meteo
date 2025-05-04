package com.meteo_app_java.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.meteo_app_java.models.HourlyForecast;

import java.util.List;

/**
 * Data Access Object for the HourlyForecast table.
 */
@Dao
public interface HourlyForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HourlyForecast> forecasts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HourlyForecast forecast);

    @Update
    void update(HourlyForecast forecast);

    @Delete
    void delete(HourlyForecast forecast);

    @Query("DELETE FROM hourly_forecast WHERE locationId = :locationId")
    void deleteByLocationId(int locationId);

    @Query("DELETE FROM hourly_forecast")
    void deleteAll();

    @Query("SELECT * FROM hourly_forecast WHERE id = :id")
    LiveData<HourlyForecast> getHourlyForecastById(int id);

    @Query("SELECT * FROM hourly_forecast WHERE locationId = :locationId ORDER BY timestamp ASC")
    LiveData<List<HourlyForecast>> getHourlyForecastsByLocationId(int locationId);

    @Query("SELECT * FROM hourly_forecast WHERE locationId = :locationId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    LiveData<List<HourlyForecast>> getHourlyForecastsForDay(int locationId, long startTime, long endTime);
}