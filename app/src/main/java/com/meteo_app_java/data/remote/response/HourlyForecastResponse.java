package com.meteo_app_java.data.remote.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO class to parse the hourly forecast API response.
 */
public class HourlyForecastResponse {

    public String cod;
    public int message;
    public int cnt;
    public List<HourlyForecast> list;
    public City city;

    public static class HourlyForecast {
        public long dt;
        public Main main;
        public List<Weather> weather;
        public Clouds clouds;
        public Wind wind;
        public int visibility;
        public double pop;
        public Rain rain;
        public Snow snow;

        @SerializedName("dt_txt")
        public String dtTxt;
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

        @SerializedName("sea_level")
        public int seaLevel;

        @SerializedName("grnd_level")
        public int groundLevel;

        public int humidity;

        @SerializedName("temp_kf")
        public double tempKf;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Clouds {
        public int all;
    }

    public static class Wind {
        public double speed;
        public int deg;
        public double gust;
    }

    public static class Rain {
        @SerializedName("3h")
        public double threeHour;
    }

    public static class Snow {
        @SerializedName("3h")
        public double threeHour;
    }

    public static class City {
        public int id;
        public String name;
        public Coord coord;
        public String country;
        public int population;
        public int timezone;
        public long sunrise;
        public long sunset;
    }

    public static class Coord {
        public double lat;
        public double lon;
    }
}