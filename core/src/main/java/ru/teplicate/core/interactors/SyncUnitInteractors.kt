package ru.teplicate.core.interactors

import ru.teplicate.core.data.SyncUnitRepository
import ru.teplicate.core.domain.SynchronizationUnit


class CreateSyncUnitUseCase(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke(syncUnit: SynchronizationUnit) =
        syncUnitRepository.createSyncUnit(syncUnit)
}

class ReadSyncUnitsUseCase(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke() = syncUnitRepository.readAllSyncUnits()
}

class UpdateSyncUnitUseCase(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke(syncUnit: SynchronizationUnit) =
        syncUnitRepository.updateSyncUnit(syncUnit)
}

class DeleteSyncUnitUseCase(private val syncUnitRepository: SyncUnitRepository) {
    suspend operator fun invoke(syncUnit: SynchronizationUnit) =
        syncUnitRepository.removeSyncUnit(syncUnit)
}