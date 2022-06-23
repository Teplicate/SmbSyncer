package ru.teplicate.core.data

import ru.teplicate.core.domain.File

interface FileProviderDatasource {

    suspend fun readFiles(contentUriStr: String, readNested: Boolean): List<File>
}