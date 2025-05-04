package com.meteo_app_java.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.meteo_app_java.models.DailyForecast;
import com.meteo_app_java.models.HourlyForecast;
import com.meteo_app_java.models.SavedLocation;
import com.meteo_app_java.models.Weather;
import com.meteo_app_java.utils.DateConverter;

/**
 * Room database for the application.
 */
@Database(entities = {
        Weather.class,
        HourlyForecast.class,
        DailyForecast.class,
        SavedLocation.class
}, version = 1, exportSchema = false)
@TypeConverters({ DateConverter.class })
public abstract class MeteoDatabase extends RoomDatabase {

    /**
     * Returns the DAO for Weather data.
     */
    public abstract WeatherDao weatherDao();

    /**
     * Returns the DAO for HourlyForecast data.
     */
    public abstract HourlyForecastDao hourlyForecastDao();

    /**
     * Returns the DAO for DailyForecast data.
     */
    public abstract DailyForecastDao dailyForecastDao();

    /**
     * Returns the DAO for SavedLocation data.
     */
    public abstract SavedLocationDao savedLocationDao();
}