package ru.teplicate.datasyncersmb.manager

import androidx.documentfile.provider.DocumentFile
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.smb.SmbProcessor

class DownloadManager(private val smbProcessor: SmbProcessor) {

    fun listFiles(smbInfo: SmbInfo, path: String = "/"): List<RemoteFileView> {
        val files: List<RemoteFileView> = smbProcessor.listFiles(smbInfo, path)
        return files
    }


}