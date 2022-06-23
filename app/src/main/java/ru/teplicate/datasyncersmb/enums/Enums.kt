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

enum class DownloadEvent {
    IDLE,
    DOWNLOAD_FINISHED
}

enum class SyncState {
    STARTING,
    READING_FILES,
    FILE_UPLOAD,
    COMPLETE,
    REMOVING
}

enum class FileType(val strVals: List<String>, val viewVal: String) {
    PDF(listOf("pdf"), "PDF"),
    IMAGE(listOf("jpg", "jpeg", "png"), "IMG"),
    TXT(listOf("txt", "text"), "TXT"),
    VIDEO(listOf("mp4", "mov", "mkv"), "VID"),
    TABLE(listOf("xlsx", "xls"), "TAB"),
    OTHER(emptyList(), "FIL"),
    DIRECTORY(emptyList(), "DIR")
}

enum class RecyclerViewMode {
    SELECT,
    CLICK
}