package ru.teplicate.core.interactors

import ru.teplicate.core.data.SyncUnitRepository
import ru.teplicate.core.domain.SynchronizationUnit


class CreateSyncUnit(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke(syncUnit: SynchronizationUnit) =
        syncUnitRepository.createSyncUnit(syncUnit)
}

class ReadSyncUnits(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke() = syncUnitRepository.readAllSyncUnits()
}

class UpdateSyncUnit(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke(syncUnit: SynchronizationUnit) =
        syncUnitRepository.updateSyncUnit(syncUnit)
}

class DeleteSyncUnit(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke(syncUnit: SynchronizationUnit) =
        syncUnitRepository.removeSyncUnit(syncUnit)
}