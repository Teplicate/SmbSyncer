package ru.teplicate.core.data

import ru.teplicate.core.domain.SynchronizationUnit

interface SmbUploadDataSource {

    fun uploadFilesFromUnit(syncUnit: SynchronizationUnit)

}