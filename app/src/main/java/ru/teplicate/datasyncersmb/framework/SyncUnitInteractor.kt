package ru.teplicate.datasyncersmb.framework

import ru.teplicate.core.interactors.CreateSyncUnit
import ru.teplicate.core.interactors.DeleteSyncUnit
import ru.teplicate.core.interactors.ReadSyncUnits
import ru.teplicate.core.interactors.UpdateSyncUnit

data class SyncUnitInteractor(
    val createSyncUnit: CreateSyncUnit,
    val updateSyncUnit: UpdateSyncUnit,
    val readSyncUnits: ReadSyncUnits,
    val deleteSyncUnit: DeleteSyncUnit
)