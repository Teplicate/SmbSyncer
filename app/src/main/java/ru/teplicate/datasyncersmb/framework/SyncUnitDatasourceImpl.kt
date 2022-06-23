package ru.teplicate.datasyncersmb.framework

import kotlinx.coroutines.flow.*
import ru.teplicate.core.data.SyncUnitDataSource
import ru.teplicate.core.domain.SmbInfo
import ru.teplicate.core.domain.SynchronizationUnit
import ru.teplicate.datasyncersmb.framework.database.dao.SynchronizationUnitDAO
import ru.teplicate.datasyncersmb.framework.database.entity.SmbConnection
import ru.teplicate.datasyncersmb.framework.database.entity.SynchronizationUnitEntity

class SyncUnitDatasourceImpl(private val syncUnitDao: SynchronizationUnitDAO) : SyncUnitDataSource {

    override suspend fun readAllSyncUnits(): Flow<List<SynchronizationUnit>> {
        return syncUnitDao.readAllSyncUnitsAsFlow()
            .map { list ->
                list.map { entity ->
                    entityToDomain(entity)
                }
            }
    }

    override suspend fun createSyncUnit(sUnit: SynchronizationUnit) {
        val entityUnit = domainToEntity(sUnit)

        syncUnitDao.saveSyncUnit(entityUnit)
    }

    override suspend fun updateSyncUnit(sUnit: SynchronizationUnit) {
        syncUnitDao.updateSyncUnit(domainToEntity(sUnit))
    }

    override suspend fun removeUnit(sUnit: SynchronizationUnit) {
        syncUnitDao.deleteSyncUnit(domainToEntity(sUnit))
    }

    private fun entityToDomain(entity: SynchronizationUnitEntity): SynchronizationUnit {
        return SynchronizationUnit(
            id = entity.id,
            name = entity.name,
            contentUri = entity.contentUri,
            smbConnection = with(entity.smbConnection) {
                SmbInfo(
                    login = this.user ?: "",
                    password = this.password ?: "",
                    directory = this.sharedDirectory,
                    address = this.address
                )
            },
            targetDirectoryName = entity.syncDirectoryName,
            synchronizationOptions = entity.synchronizationOptions
                .toMutableList()
        )
    }

    private fun domainToEntity(domain: SynchronizationUnit): SynchronizationUnitEntity {
        val entityUnit = SynchronizationUnitEntity(
            id = domain.id,
            name = domain.name,
            contentUri = domain.contentUri,
            smbConnection = with(domain.smbConnection) {
                SmbConnection(
                    user = this.login,
                    password = this.password,
                    sharedDirectory = this.directory,
                    address = this.address
                )
            },
            syncDirectoryName = domain.targetDirectoryName,
            synchronizationOptions = domain.synchronizationOptions
        )

        return entityUnit
    }
}