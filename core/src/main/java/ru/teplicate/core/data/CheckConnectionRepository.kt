package ru.teplicate.core.data

class CheckConnectionRepository(private val checkConnectionDatasource: CheckConnectionDatasource) {

    suspend fun checkConnection() = checkConnectionDatasource.checkConnection()
}