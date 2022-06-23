package ru.teplicate.core.data

class FileProviderRepository(private val fileProviderDatasource: FileProviderDatasource) {

    suspend fun readFiles(contentUri: String, readNested: Boolean) =
        fileProviderDatasource.readFiles(contentUri, readNested)
}