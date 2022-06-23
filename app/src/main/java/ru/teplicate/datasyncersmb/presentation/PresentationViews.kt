package ru.teplicate.datasyncersmb.presentation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.teplicate.core.domain.SmbInfo
import ru.teplicate.core.domain.SyncOption
import ru.teplicate.core.domain.SynchronizationUnit

@Parcelize
class SyncUnitPresentation(
    val id: Int = 0,
    var name: String?,
    var contentUri: String,
    val smbConnection: SmbInfoPresentation,
    var targetDirectoryName: String?,
    val synchronizationOptions: List<SyncOption>
) : Parcelable {

    companion object {
        fun fromDomainToPresent(syncUnit: SynchronizationUnit) = SyncUnitPresentation(
            id = syncUnit.id,
            name = syncUnit.name,
            contentUri = syncUnit.contentUri,
            smbConnection = SmbInfoPresentation.fromDomainToPresent(syncUnit.smbConnection),
            targetDirectoryName = syncUnit.targetDirectoryName,
            synchronizationOptions = syncUnit.synchronizationOptions
        )
    }

    fun toDomain() = SynchronizationUnit(
        id = this.id,
        name = this.name,
        contentUri = this.contentUri,
        smbConnection = this.smbConnection.toDomain(),
        targetDirectoryName = this.targetDirectoryName,
        synchronizationOptions = this.synchronizationOptions.toMutableList()
    )
}

@Parcelize
class SmbInfoPresentation(
    val login: String,
    val password: String,
    val directory: String,
    val address: String
) : Parcelable {

    companion object {
        fun fromDomainToPresent(smbInfo: SmbInfo) = SmbInfoPresentation(
            login = smbInfo.login,
            password = smbInfo.password,
            directory = smbInfo.directory,
            address = smbInfo.address
        )
    }

    fun toDomain() = SmbInfo(
        login = this.login,
        password = this.password,
        directory = this.directory,
        address = this.address
    )
}