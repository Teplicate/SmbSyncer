package ru.teplicate.datasyncersmb.data

data class RemoteFileView(
    val fileId: Long,
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val createdAt: Long
)