package ru.teplicate.datasyncersmb.manager

import android.content.Context
import android.net.Uri
import ru.teplicate.datasyncersmb.content_processor.ContentProcessor
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.enums.SyncOption
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import java.sql.Date

class SyncManager(
    private val contentProcessor: ContentProcessor,
    private val smbProcessor: SmbProcessor
) {

    fun syncContentFromDirectory(
        synchronizationUnit: SynchronizationUnit,
        context: Context,
        syncEventHandler: SmbProcessor.SyncEventHandler
    ) {
        val uri = Uri.parse(synchronizationUnit.contentUri)
        val options = synchronizationUnit.synchronizationOptions
        val getNested =
            options.contains(SyncOption.SYNC_NESTED) && options.contains(SyncOption.GROUP_BY_DATE)

        syncEventHandler.onReadingFiles()

        val files = contentProcessor.getContent(
            uri,
            context,
            getNested
        )

        val groupByDates = options.contains(SyncOption.GROUP_BY_DATE)
        val totalFiles: Int = files.size
        syncEventHandler.totalElements(totalFiles)

        syncEventHandler.onCopying()
        
        if (groupByDates) {
            val dateGrouped = files.groupByTo(LinkedHashMap()) {
                Date(it.lastModified()).toString()
            }
            smbProcessor.uploadFilesGroupingByDate(
                synchronizationUnit,
                dateGrouped,
                syncEventHandler,
                context.contentResolver
            )
        } else {
            smbProcessor.uploadFilesSavingStructure(
                synchronizationUnit,
                files,
                syncEventHandler,
                context.contentResolver
            )
        }

    }
}