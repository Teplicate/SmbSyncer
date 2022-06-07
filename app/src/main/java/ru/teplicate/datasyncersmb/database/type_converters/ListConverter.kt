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
        if (stringList.isEmpty())
            return emptyList()

        return stringList.split(optionSplitter)
            .filter { it.isNotEmpty() }
            .map { s -> SyncOption.stringToOption(s) }
    }
}