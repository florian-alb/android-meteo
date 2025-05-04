package com.meteo_app_java.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity class for storing daily forecast data.
 */
@Entity(tableName = "daily_forecast", foreignKeys = @ForeignKey(entity = Weather.class, parentColumns = "id", childColumns = "locationId", onDelete = ForeignKey.CASCADE), indices = {
        @Index("locationId") })
public class DailyForecast {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int locationId;
    private long timestamp;
    private double tempDay;
    private double tempNight;
    private double tempMin;
    private double tempMax;
    private double feelsLikeDay;
    private double feelsLikeNight;
    private int humidity;
    private int pressure;
    private double windSpeed;
    private int windDegree;
    private String weatherMain;
    private String weatherDescription;
    private String weatherIcon;
    private int pop; // Probability of precipitation (0-100%)
    private double rainVolume; // mm
    private long sunrise;
    private long sunset;

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getTempDay() {
        return tempDay;
    }

    public void setTempDay(double tempDay) {
        this.tempDay = tempDay;
    }

    public double getTempNight() {
        return tempNight;
    }

    public void setTempNight(double tempNight) {
        this.tempNight = tempNight;
    }

    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public double getFeelsLikeDay() {
        return feelsLikeDay;
    }

    public void setFeelsLikeDay(double feelsLikeDay) {
        this.feelsLikeDay = feelsLikeDay;
    }

    public double getFeelsLikeNight() {
        return feelsLikeNight;
    }

    public void setFeelsLikeNight(double feelsLikeNight) {
        this.feelsLikeNight = feelsLikeNight;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWindDegree() {
        return windDegree;
    }

    public void setWindDegree(int windDegree) {
        this.windDegree = windDegree;
    }

    public String getWeatherMain() {
        return weatherMain;
    }

    public void setWeatherMain(String weatherMain) {
        this.weatherMain = weatherMain;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public int getPop() {
        return pop;
    }

    public void setPop(int pop) {
        this.pop = pop;
    }

    public double getRainVolume() {
        return rainVolume;
    }

    public void setRainVolume(double rainVolume) {
        this.rainVolume = rainVolume;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }
}