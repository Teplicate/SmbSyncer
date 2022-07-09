package ru.teplicate.core.data

interface ScanDatasource {

    suspend fun scanNetwork()
}