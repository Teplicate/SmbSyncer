package ru.teplicate.datasyncersmb.framework.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.teplicate.datasyncersmb.framework.database.entity.SynchronizationUnitEntity

@Dao
interface SynchronizationUnitDAO {

    @Query("SELECT * FROM synchronization_unit")
    fun readAllSyncUnitsAsFlow(): Flow<List<SynchronizationUnitEntity>>

    @Insert
    fun saveSyncUnit(syncUnitEntity: SynchronizationUnitEntity)

    @Delete
    fun deleteSyncUnit(syncUnitEntity: SynchronizationUnitEntity)

    @Update
    fun updateSyncUnit(syncUnitEntity: SynchronizationUnitEntity)
}