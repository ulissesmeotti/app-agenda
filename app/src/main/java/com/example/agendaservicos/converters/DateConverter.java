package com.example.agendaservicos.converters;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public Long toTimestamp(Date data)
    {
        return data == null ? null : data.getTime();
    }

    @TypeConverter
    public Date fromTimestamp(Long ts) {
        return ts == null ? null : new Date(ts);
    }
}