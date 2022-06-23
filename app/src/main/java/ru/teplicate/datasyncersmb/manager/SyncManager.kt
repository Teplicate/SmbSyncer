package ru.teplicate.datasyncersmb.manager

import android.content.Context
import android.net.Uri
import ru.teplicate.core.domain.SyncOption
import ru.teplicate.core.domain.SynchronizationUnit
import ru.teplicate.datasyncersmb.content_processor.ContentProcessor
import ru.teplicate.datasyncersmb.framework.database.entity.SynchronizationUnitEntity
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import java.sql.Date

class SyncManager(
    private val context: Context,
    private val contentProcessor: ContentProcessor,
    private val smbProcessor: SmbProcessor
) {

    fun syncContentFromDirectory(
        synchronizationUnitEntity: SynchronizationUnit,
        syncEventHandler: SmbProcessor.SyncEventHandler
    ) {
        val uri = Uri.parse(synchronizationUnitEntity.contentUri)
        val options = synchronizationUnitEntity.synchronizationOptions
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
        syncEventHandler.onStartSync(totalFiles)

        syncEventHandler.onCopying()
        
        if (groupByDates) {
            val dateGrouped = files.groupByTo(LinkedHashMap()) {
                Date(it.lastModified()).toString()
            }
            smbProcessor.uploadFilesGroupingByDate(
                synchronizationUnitEntity,
                dateGrouped,
                syncEventHandler,
                context.contentResolver
            )
        } else {
            smbProcessor.uploadFilesSavingStructure(
                synchronizationUnitEntity,
                files,
                syncEventHandler,
                context.contentResolver
            )
        }
    }
}