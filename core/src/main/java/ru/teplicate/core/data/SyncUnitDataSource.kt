package ru.teplicate.core.data


import kotlinx.coroutines.flow.Flow
import ru.teplicate.core.domain.SynchronizationUnit

interface SyncUnitDataSource {

    suspend fun readAllSyncUnits(): Flow<List<SynchronizationUnit>>

    suspend fun createSyncUnit(sUnit: SynchronizationUnit)

    suspend fun updateSyncUnit(sUnit: SynchronizationUnit)

    suspend fun removeUnit(sUnit: SynchronizationUnit)
}