package ru.teplicate.datasyncersmb.content_processor

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.documentfile.provider.DocumentFile
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class ContentProcessor {

    val projection = arrayOf(
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.DATE_ADDED,
        MediaStore.Files.FileColumns.DATA,
        MediaStore.Files.FileColumns.SIZE
    )

    fun getContent(
        contentDir: Uri,
        context: Context,
        getNested: Boolean
    ): List<DocumentFile> {
        val res = DocumentFile.fromTreeUri(context, contentDir)
        val files = res?.listFiles()!!
        val dateGrouped =
            if (getNested) {
                aggregateContentRecursive(files)
                    .sortedBy { it.lastModified() }
            } else {
                files
                    .filter { it.isFile }
                    .sortedBy { it.lastModified() }
            }

        return dateGrouped
    }

    private fun aggregateContentRecursive(files: Array<DocumentFile>): List<DocumentFile> {
        val listData = LinkedList<DocumentFile>()
        val allFiles = files.fold(listData) { acc, file ->
            if (file.isDirectory) {
                acc.addAll(aggregateContentRecursive(file.listFiles()))
                acc
            } else {
                acc.add(file)
                acc
            }
        }

        return allFiles
    }

    fun syncDateContent(
        dateStr: String,
        contentDir: Uri,
        contentResolver: ContentResolver
    ): List<Data> {
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} ASC"
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE)
        val dataList = ArrayList<Data>()
        val selectionArgs = null
        contentResolver.query(
            contentDir,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
            val uriColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)



            while (cursor.moveToNext()) {
                val name = cursor.getString(nameColumn)
                val date = cursor.getLong(dateColumn)
                val uri = cursor.getString(uriColumn)
                val size = cursor.getInt(sizeColumn)

                dataList
            }
        }

        return dataList
    }
}

data class Data(
    val uri: Uri,
    val date: Date,
    val name: String,
    val size: Long
)