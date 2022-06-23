package ru.teplicate.core.data

import ru.teplicate.core.domain.SynchronizationUnit

class SyncUnitRepository(private val syncUnitDataSource: SyncUnitDataSource) {

    suspend fun readAllSyncUnits() = syncUnitDataSource.readAllSyncUnits()


    suspend fun createSyncUnit(sUnit: SynchronizationUnit) {
        syncUnitDataSource.createSyncUnit(sUnit)
    }

    suspend fun updateSyncUnit(sUnit: SynchronizationUnit) {
        syncUnitDataSource.updateSyncUnit(sUnit)
    }

    suspend fun removeSyncUnit(sUnit: SynchronizationUnit) {
        syncUnitDataSource.removeUnit(sUnit)
    }
}