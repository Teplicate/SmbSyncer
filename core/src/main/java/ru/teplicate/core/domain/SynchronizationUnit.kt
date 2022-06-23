package ru.teplicate.core.domain


data class SynchronizationUnit(
    val id: Int = 0,
    var name: String?,
    var contentUri: String,
    val smbConnection: SmbInfo,
    var targetDirectoryName: String?,
    val synchronizationOptions: MutableList<SyncOption>
)

enum class SyncOption {
    SYNC_NESTED,
    REMOVE_SYNCED,
    GROUP_BY_DATE,
    CREATE_SYNC_DIR;

    companion object {
        fun stringToOption(string: String): SyncOption = SyncOption.valueOf(string)
    }
}