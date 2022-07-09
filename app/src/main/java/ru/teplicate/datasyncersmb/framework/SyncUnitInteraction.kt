package ru.teplicate.datasyncersmb.framework

import ru.teplicate.core.interactors.CreateSyncUnitUseCase
import ru.teplicate.core.interactors.DeleteSyncUnitUseCase
import ru.teplicate.core.interactors.ReadSyncUnitsUseCase
import ru.teplicate.core.interactors.UpdateSyncUnitUseCase

data class SyncUnitInteraction(
    val createSyncUnitUseCase: CreateSyncUnitUseCase,
    val updateSyncUnit: UpdateSyncUnitUseCase,
    val readSyncUnits: ReadSyncUnitsUseCase,
    val deleteSyncUnit: DeleteSyncUnitUseCase
)