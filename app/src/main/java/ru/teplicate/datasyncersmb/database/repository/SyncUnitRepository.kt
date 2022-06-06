package ru.teplicate.datasyncersmb.database.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.database.dao.SynchronizationUnitDAO
import ru.teplicate.datasyncersmb.database.entity.SmbConnection
import ru.teplicate.datasyncersmb.database.entity.SynchronizationInfo
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.enums.SyncOption

class SyncUnitRepository(private val syncUnitDao: SynchronizationUnitDAO) {

    fun readSyncUnits(): Flow<List<SynchronizationUnit>> {
        return syncUnitDao.readAllSyncUnitsAsFlow()
            .onStart { emptyList<SynchronizationUnit>() }
    }

    fun saveSyncUnit(
        smbInfo: SmbInfo,
        contentUri: Uri,
        name: String?,
        syncDirectoryName: String?,
        options: List<SyncOption>
    ) {
        val syncUnit = SynchronizationUnit(
            name = name,
            contentUri = contentUri.toString(),
            smbConnection = SmbConnection(
                address = smbInfo.address,
                user = smbInfo.login,
                password = smbInfo.password,
                sharedDirectory = smbInfo.directory
            ),
            synchronizationInfo = SynchronizationInfo(null, syncDirectoryName),
            synchronizationOptions = options
        )

        syncUnitDao.saveSyncUnit(syncUnit)
    }

    fun deleteSyncUnit(unit: SynchronizationUnit) {
        syncUnitDao.deleteSyncUnit(unit)
    }

    fun updateSyncUnit(unit: SynchronizationUnit) {
        syncUnitDao.updateSyncUnit(unit)
    }
}