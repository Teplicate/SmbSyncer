package ru.teplicate.datasyncersmb.manager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import ru.teplicate.core.domain.SmbInfo
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.presentation.SmbInfoPresentation
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DownloadManager(
    private val context: Context,
    private val smbProcessor: SmbProcessor
) {

    fun listFiles(smbInfo: SmbInfoPresentation, path: String = "/"): List<RemoteFileView> {
//        return smbProcessor.listFiles(smbInfo, path)
        return emptyList()
    }


    fun downloadFiles(
        smbInfo: SmbInfoPresentation,
        files: List<RemoteFileView>,
        downloadEventHandler: SmbProcessor.DownloadEventHandler
    ) {
        val ioStreamProcessor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this::downloadFileQ
        } else this::downloadFile

//        smbProcessor.downloadFiles(smbInfo, files, ioStreamProcessor, downloadEventHandler)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun downloadFileQ(fileName: String, fileStream: InputStream) {
        val values = ContentValues()
            .apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) as Uri
        fileStream.use {
            val o = resolver.openOutputStream(uri)!!
            fileStream.copyTo(o, DEFAULT_BUFFER_SIZE)
        }
    }

    private fun downloadFile(fileName: String, ioStream: InputStream) {
        val target = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        ioStream.use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
    }
}