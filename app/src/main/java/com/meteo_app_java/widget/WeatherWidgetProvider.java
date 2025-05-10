package com.meteo_app_java.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.meteo_app_java.R;
import com.meteo_app_java.data.WeatherRepository;
import com.meteo_app_java.models.Weather;
import com.meteo_app_java.ui.MainActivity;
import com.meteo_app_java.utils.PreferencesManager;
import com.meteo_app_java.utils.WeatherUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create an Intent to launch MainActivity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        views.setOnClickPendingIntent(R.id.widget_temperature, pendingIntent);

        // Load data from preferences
        PreferencesManager preferencesManager = new PreferencesManager(context);
        boolean isMetric = preferencesManager.isUsingMetricUnits();
        String location = preferencesManager.getLastLocationName();
        double temperature = preferencesManager.getLastTemperature();
        String description = preferencesManager.getLastWeatherDescription();
        String iconCode = preferencesManager.getLastWeatherIcon(); // Make sure this variable name is used consistently

        long lastUpdated = preferencesManager.getLastUpdated();

        // Update widget content
        views.setTextViewText(R.id.widget_location, location);
        views.setTextViewText(R.id.widget_temperature, WeatherUtils.formatTemperature(temperature, isMetric));
        views.setTextViewText(R.id.widget_description, description);

        // Format and set last updated time
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String updateTime = "Updated: " + sdf.format(new Date(lastUpdated));
        views.setTextViewText(R.id.widget_last_updated, updateTime);

        // Load weather icon using the correct variable name
        String iconUrl = WeatherUtils.getWeatherIconUrl(iconCode);
        
        // Need to use glide to load image from URL
        AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.widget_icon, views, appWidgetId);
        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(iconUrl)
                .into(appWidgetTarget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Called when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Called when the last widget is disabled
    }
}