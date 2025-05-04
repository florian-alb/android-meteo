package com.meteo_app_java.utils;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converter for Date objects used with Room database.
 */
public class DateConverter {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}