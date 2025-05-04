package com.meteo_app_java.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.meteo_app_java.models.SavedLocation;

import java.util.List;

/**
 * Data Access Object for the SavedLocation table.
 */
@Dao
public interface SavedLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SavedLocation location);

    @Update
    void update(SavedLocation location);

    @Delete
    void delete(SavedLocation location);

    @Query("DELETE FROM saved_location")
    void deleteAll();

    @Query("SELECT * FROM saved_location WHERE id = :id")
    LiveData<SavedLocation> getLocationById(int id);

    @Query("SELECT * FROM saved_location WHERE cityName LIKE :cityName LIMIT 1")
    LiveData<SavedLocation> getLocationByCity(String cityName);

    @Query("SELECT * FROM saved_location ORDER BY lastAccessed DESC LIMIT 10")
    LiveData<List<SavedLocation>> getRecentLocations();

    @Query("SELECT * FROM saved_location WHERE isFavorite = 1 ORDER BY cityName ASC")
    LiveData<List<SavedLocation>> getFavoriteLocations();

    @Query("SELECT * FROM saved_location ORDER BY lastAccessed DESC")
    LiveData<List<SavedLocation>> getAllLocations();
}