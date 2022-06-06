package ru.teplicate.datasyncersmb.database.type_converters

import androidx.room.TypeConverter
import ru.teplicate.datasyncersmb.enums.SyncOption

const val optionSplitter = "@"

class ListConverter {

    @TypeConverter
    fun listToString(optionList: List<SyncOption>): String {
        return optionList.joinToString(optionSplitter)
    }

    @TypeConverter
    fun stringToList(stringList: String): List<SyncOption> {
        return stringList.split(optionSplitter)
            .map { s -> SyncOption.stringToOption(s) }
    }
}