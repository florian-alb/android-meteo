package com.meteo_app_java.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.meteo_app_java.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for weather-related formatting and conversions.
 */
public class WeatherUtils {

    private static final String TAG = "WeatherUtils";
    private static final String ICON_URL_PATTERN = "https://openweathermap.org/img/wn/%s@2x.png";

    public static String formatTemperature(double temperature, boolean isMetric) {
        return String.format(Locale.getDefault(), "%.0f%s",
                temperature, isMetric ? "°C" : "°F");
    }

    public static String formatWindSpeed(double windSpeed, boolean isMetric) {
        if (isMetric) {
            return String.format(Locale.getDefault(), "%.1f m/s", windSpeed);
        } else {
            return String.format(Locale.getDefault(), "%.1f mph", windSpeed);
        }
    }

    public static String getWeatherIconUrl(String iconCode) {
        return String.format(ICON_URL_PATTERN, iconCode);
    }

    public static void loadWeatherIcon(Context context, String iconCode, ImageView imageView) {
        if (iconCode == null || iconCode.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_weather_unknown);
            return;
        }

        String iconUrl = getWeatherIconUrl(iconCode);
        Log.d(TAG, "Loading weather icon from URL: " + iconUrl);

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_weather_unknown)
                .error(R.drawable.ic_weather_unknown);

        try {
            Glide.with(context)
                    .load(iconUrl)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading weather icon: " + e.getMessage());
            imageView.setImageResource(getWeatherIconResource(context, iconCode));
        }
    }

    public static String formatDayOfWeek(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatShortDayOfWeek(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatShortDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatHour(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("h a", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatTime(long timestamp, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatTime(long timestamp, String pattern, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        if (timeZone != null) {
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        }
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatSunTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatHumidity(int humidity) {
        return String.format(Locale.getDefault(), "%d%%", humidity);
    }

    public static String formatPop(int pop) {
        return String.format(Locale.getDefault(), "%d%%", pop);
    }

    public static int getWeatherIconResource(Context context, String weatherIcon) {
        if (weatherIcon == null)
            return R.drawable.ic_weather_unknown;

        switch (weatherIcon) {
            case "01d":
                return R.drawable.ic_weather_clear_day;
            case "01n":
                return R.drawable.ic_weather_clear_night;
            case "02d":
                return R.drawable.ic_weather_few_clouds_day;
            case "02n":
                return R.drawable.ic_weather_few_clouds_night;
            case "03d":
            case "03n":
                return R.drawable.ic_weather_scattered_clouds;
            case "04d":
            case "04n":
                return R.drawable.ic_weather_broken_clouds;
            case "09d":
            case "09n":
                return R.drawable.ic_weather_shower_rain;
            case "10d":
                return R.drawable.ic_weather_rain_day;
            case "10n":
                return R.drawable.ic_weather_rain_night;
            case "11d":
            case "11n":
                return R.drawable.ic_weather_thunderstorm;
            case "13d":
            case "13n":
                return R.drawable.ic_weather_snow;
            case "50d":
            case "50n":
                return R.drawable.ic_weather_mist;
            default:
                return R.drawable.ic_weather_unknown;
        }
    }

    public static String formatWeatherDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean capitalize = true;

        for (char c : description.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                capitalize = true;
                result.append(c);
            } else if (capitalize) {
                result.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}