package ru.teplicate.datasyncersmb.enums

enum class ConnectionState {
    IDLE,
    AUTH_REQUIRED,
    INVALID_SHARE_NAME,
    CONNECTION_OK,
    NA
}

enum class SetupSyncEvent {
    UNIT_SAVED,
    SYNC_COMPLETED,
    SYNC_FAILED,
    IDLE
}

enum class SyncOption {
    SYNC_NESTED,
    REMOVE_SYNCED,
    GROUP_BY_DATE,
    CREATE_SYNC_DIR;

    companion object {
        fun stringToOption(string: String): SyncOption = SyncOption.valueOf(string)
    }
}

enum class SyncState{
    IDLE,
    READING_FILES,
    COPYING,
    REMOVING
}