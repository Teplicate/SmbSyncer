package ru.teplicate.core.data

class ScanRepository(private val scanDatasource: ScanDatasource) {

    suspend fun scan() = scanDatasource.scanNetwork()
}