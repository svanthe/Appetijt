package com.svantheemsche.appetijt.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateConverters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, formatter) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.format(formatter)
    }
}
