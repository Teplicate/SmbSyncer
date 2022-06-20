package ru.teplicate.datasyncersmb.database.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.enums.SyncOption
import java.sql.Date

@Parcelize
@Entity(tableName = "synchronization_unit")
data class SynchronizationUnit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String?,
    var contentUri: String,
    @Embedded
    val smbConnection: SmbConnection,
    @Embedded
    val synchronizationInfo: SynchronizationInfo?,
    @Embedded
    var failedSyncInfo: FileInfo? = null,
    var synchronizationOptions: List<SyncOption>
) : Parcelable

@Parcelize
data class SmbConnection(
    var address: String,
    var user: String?,
    var password: String?,
    var sharedDirectory: String
) : Parcelable {
    fun toSmbInfo(): SmbInfo {
        return SmbInfo(user ?: "", password ?: "", sharedDirectory, address)
    }
}

@Parcelize
data class SynchronizationInfo(
    var lastSyncDate: Date?,
    var targetDirectoryName: String?,
) : Parcelable


@Parcelize
data class FileInfo(
    val fileName: String,
    val fileUri: String,
    val fileDate: Date
) : Parcelable

