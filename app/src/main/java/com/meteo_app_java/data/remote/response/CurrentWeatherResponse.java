package com.meteo_app_java.data.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO class to parse the current weather API response.
 */
public class CurrentWeatherResponse {

    public Coord coord;
    public List<Weather> weather;
    public String base;
    public Main main;
    public int visibility;
    public Wind wind;
    public Clouds clouds;
    public Rain rain;
    public Snow snow;
    public long dt;
    public Sys sys;
    public int timezone;
    public int id;
    public String name;
    public int cod;

    public static class Coord {
        public double lon;
        public double lat;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Main {
        public double temp;

        @SerializedName("feels_like")
        public double feelsLike;

        @SerializedName("temp_min")
        public double tempMin;

        @SerializedName("temp_max")
        public double tempMax;

        public int pressure;
        public int humidity;

        @SerializedName("sea_level")
        public int seaLevel;

        @SerializedName("grnd_level")
        public int groundLevel;
    }

    public static class Wind {
        public double speed;
        public int deg;
        public double gust;
    }

    public static class Clouds {
        public int all;
    }

    public static class Rain {
        @SerializedName("1h")
        public double oneHour;

        @SerializedName("3h")
        public double threeHour;
    }

    public static class Snow {
        @SerializedName("1h")
        public double oneHour;

        @SerializedName("3h")
        public double threeHour;
    }

    public static class Sys {
        public int type;
        public int id;
        public String country;
        public long sunrise;
        public long sunset;
    }
}