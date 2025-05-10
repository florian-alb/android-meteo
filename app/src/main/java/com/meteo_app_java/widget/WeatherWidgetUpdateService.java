package com.meteo_app_java.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.meteo_app_java.data.WeatherRepository;
import com.meteo_app_java.models.Weather;
import com.meteo_app_java.utils.PreferencesManager;

public class WeatherWidgetUpdateService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get current location from preferences
        PreferencesManager preferencesManager = new PreferencesManager(this);
        double lat = preferencesManager.getLastLocationLatitude();
        double lon = preferencesManager.getLastLocationLongitude();

        // Get repository instance using the singleton pattern
        WeatherRepository repository = WeatherRepository.getInstance();
        
        // Use a callback approach instead of observe
        repository.getCurrentWeatherByCoordinates(lat, lon).observeForever(new Observer<Weather>() {
            @Override
            public void onChanged(Weather weather) {
                if (weather != null) {
                    // Save updated weather data
                    preferencesManager.saveLastWeatherData(weather);
                    // Update all widgets
                    WeatherWidgetProvider.updateAllWidgets(WeatherWidgetUpdateService.this);
                }
                
                // Remove observer to prevent memory leaks
                repository.getCurrentWeatherByCoordinates(lat, lon).removeObserver(this);
                
                // Stop the service after updating
                stopSelf();
            }
        });
        
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}