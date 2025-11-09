package com.example.quiz_clone.utils;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.util.Date;

public class ModernConverters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}