package ru.teplicate.datasyncersmb.database.type_converters

import androidx.room.TypeConverter
import java.sql.Date

class DateConverter {
    @TypeConverter
    fun dateToTimestamp(date: Date?) = date?.time

    @TypeConverter
    fun timestampToDate(timestamp: Long?) = timestamp?.let { Date(it) }
}