package ru.teplicate.datasyncersmb.framework.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.teplicate.datasyncersmb.framework.database.dao.SynchronizationUnitDAO
import ru.teplicate.datasyncersmb.framework.database.entity.SynchronizationUnitEntity
import ru.teplicate.datasyncersmb.framework.database.type_converters.DateConverter
import ru.teplicate.datasyncersmb.framework.database.type_converters.ListConverter

const val DATABASE_NAME = "syncer_db"

@Database(entities = [SynchronizationUnitEntity::class], version = 4)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun syncUnitDao(): SynchronizationUnitDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room
                .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}