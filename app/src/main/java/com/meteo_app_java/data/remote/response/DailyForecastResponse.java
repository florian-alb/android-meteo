package com.meteo_app_java.data.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO class to parse the daily forecast API response (OneCall API).
 */
public class DailyForecastResponse {

    public double lat;
    public double lon;
    public String timezone;

    @SerializedName("timezone_offset")
    public int timezoneOffset;

    public Current current;
    public List<Minute> minutely;
    public List<Hourly> hourly;
    public List<Daily> daily;

    public static class Current {
        public long dt;
        public long sunrise;
        public long sunset;
        public double temp;

        @SerializedName("feels_like")
        public double feelsLike;

        public int pressure;
        public int humidity;

        @SerializedName("dew_point")
        public double dewPoint;

        public double uvi;
        public int clouds;
        public int visibility;

        @SerializedName("wind_speed")
        public double windSpeed;

        @SerializedName("wind_deg")
        public int windDeg;

        @SerializedName("wind_gust")
        public double windGust;

        public List<Weather> weather;
        public Rain rain;
        public Snow snow;
    }

    public static class Minute {
        public long dt;
        public double precipitation;
    }

    public static class Hourly {
        public long dt;
        public double temp;

        @SerializedName("feels_like")
        public double feelsLike;

        public int pressure;
        public int humidity;

        @SerializedName("dew_point")
        public double dewPoint;

        public double uvi;
        public int clouds;
        public int visibility;

        @SerializedName("wind_speed")
        public double windSpeed;

        @SerializedName("wind_deg")
        public int windDeg;

        @SerializedName("wind_gust")
        public double windGust;

        public List<Weather> weather;
        public double pop;
        public Rain rain;
        public Snow snow;
    }

    public static class Daily {
        public long dt;
        public long sunrise;
        public long sunset;
        public long moonrise;
        public long moonset;

        @SerializedName("moon_phase")
        public double moonPhase;

        public Temp temp;

        @SerializedName("feels_like")
        public FeelsLike feelsLike;

        public int pressure;
        public int humidity;

        @SerializedName("dew_point")
        public double dewPoint;

        @SerializedName("wind_speed")
        public double windSpeed;

        @SerializedName("wind_deg")
        public int windDeg;

        @SerializedName("wind_gust")
        public double windGust;

        public List<Weather> weather;
        public int clouds;
        public double pop;
        public double rain;
        public double snow;
        public double uvi;
    }

    public static class Temp {
        public double day;
        public double min;
        public double max;
        public double night;
        public double eve;
        public double morn;
    }

    public static class FeelsLike {
        public double day;
        public double night;
        public double eve;
        public double morn;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Rain {
        @SerializedName("1h")
        public double oneHour;
    }

    public static class Snow {
        @SerializedName("1h")
        public double oneHour;
    }
}