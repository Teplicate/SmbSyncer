package ru.teplicate.datasyncersmb.content_processor

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.sql.Date
import java.util.*

class ContentProcessor {

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
}

data class Data(
    val uri: Uri,
    val date: Date,
    val name: String,
    val size: Long
)