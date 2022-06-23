package ru.teplicate.core.domain

data class File(
    val name: String,
    val size: Long,
    val isDirectory: Boolean,
    val uri: String,
    val lastModified: Long
)