package ru.teplicate.datasyncersmb.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit

@Dao
interface SynchronizationUnitDAO {

    @Query("SELECT * FROM synchronization_unit")
    fun readAllSyncUnitsAsFlow(): Flow<List<SynchronizationUnit>>

    @Insert
    fun saveSyncUnit(syncUnit: SynchronizationUnit)

    @Delete
    fun deleteSyncUnit(syncUnit: SynchronizationUnit)

    @Update
    fun updateSyncUnit(syncUnit: SynchronizationUnit)
}