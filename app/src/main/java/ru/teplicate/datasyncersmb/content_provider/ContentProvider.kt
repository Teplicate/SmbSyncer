/*
package ru.teplicate.datasyncersmb.content_provider

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider

class ContentProvider : DocumentsProvider() {

    private val DEFAULT_ROOT_PROJECTION: Array<String> = arrayOf(
        DocumentsContract.Root.COLUMN_ROOT_ID,
        DocumentsContract.Root.COLUMN_MIME_TYPES,
        DocumentsContract.Root.COLUMN_FLAGS,
        DocumentsContract.Root.COLUMN_ICON,
        DocumentsContract.Root.COLUMN_TITLE,
        DocumentsContract.Root.COLUMN_SUMMARY,
        DocumentsContract.Root.COLUMN_DOCUMENT_ID,
        DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
    )
    private val DEFAULT_DOCUMENT_PROJECTION: Array<String> = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        DocumentsContract.Document.COLUMN_FLAGS,
        DocumentsContract.Document.COLUMN_SIZE
    )

    private val rootId = "ROOT"

    override fun onCreate(): Boolean {
        TODO("Not yet implemented")
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val result = MatrixCursor(projection)

        result.newRow()
            .apply {
                add(DocumentsContract.Root.COLUMN_ROOT_ID, rootId)
            }

        return result
    }

    override fun queryDocument(p0: String?, p1: Array<out String>?): Cursor {
        TODO("Not yet implemented")
    }

    override fun queryChildDocuments(
        parentDocId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return MatrixCursor(projection).apply {
            val parent: File = getFileForDocId(parentDocumentId)
            parent.listFiles()
                .forEach { file ->
                    includeFile(this, null, file)
                }
        }
        Intent.createChooser()
    }

    @Throws(FileNotFoundException::class)
    private fun getFileForDocId(docId: String): File? {
        var target: File = mBaseDir
        if (docId == ROOT) {
            return target
        }
        val splitIndex = docId.indexOf(':', 1)
        return if (splitIndex < 0) {
            throw FileNotFoundException("Missing root for $docId")
        } else {
            val path = docId.substring(splitIndex + 1)
            target = File(target, path)
            if (!target.exists()) {
                throw FileNotFoundException("Missing file for $docId at $target")
            }
            target
        }
    }

    override fun openDocument(
        p0: String?,
        p1: String?,
        p2: CancellationSignal?
    ): ParcelFileDescriptor {
        TODO("Not yet implemented")
    }
}*/
