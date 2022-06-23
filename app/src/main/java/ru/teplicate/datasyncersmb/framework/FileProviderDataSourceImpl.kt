package ru.teplicate.datasyncersmb.framework

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import ru.teplicate.core.data.FileProviderDatasource
import ru.teplicate.core.domain.File
import java.util.*

class FileProviderDataSourceImpl(private val context: Context) : FileProviderDatasource {

    override suspend fun readFiles(contentUriStr: String, readNested: Boolean): List<File> {
        val contentUri = Uri.parse(contentUriStr)
        val res = DocumentFile.fromTreeUri(context, contentUri)
        val files = res?.listFiles()!!
        val readFiles =
            if (readNested) {
                aggregateContentRecursive(files)
                    .sortedBy { it.lastModified }
            } else {
                files
                    .filter { it.isFile }
                    .map { docFileToFile(it) }
                    .sortedBy { it.lastModified }
            }

        return readFiles
    }

    private fun aggregateContentRecursive(files: Array<DocumentFile>): List<File> {
        val listData = LinkedList<File>()
        val allFiles = files.fold(listData) { acc, file ->
            if (file.isDirectory) {
                acc.addAll(aggregateContentRecursive(file.listFiles()))
                acc
            } else {
                acc.add(docFileToFile(file))
                acc
            }
        }

        return allFiles
    }

    private fun docFileToFile(documentFile: DocumentFile) =
        File(
            name = documentFile.name ?: "",
            size = documentFile.length(),
            uri = documentFile.uri.toString(),
            isDirectory = documentFile.isDirectory,
            lastModified = documentFile.lastModified()
        )
}