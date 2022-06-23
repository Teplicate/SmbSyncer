package ru.teplicate.core.domain

data class SmbInfo(
    val login: String,
    val password: String,
    val directory: String,
    val address: String
)