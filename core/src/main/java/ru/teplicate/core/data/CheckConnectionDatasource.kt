package ru.teplicate.core.data

interface CheckConnectionDatasource {

    suspend fun checkConnection()
}