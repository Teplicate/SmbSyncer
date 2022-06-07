package ru.teplicate.datasyncersmb.manager

import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.smb.SmbProcessor

class DownloadManager(private val smbProcessor: SmbProcessor) {

    fun listFiles(smbInfo: SmbInfo, path: String = "/"): List<RemoteFileView> {
        return smbProcessor.listFiles(smbInfo, path)
    }


    fun downloadFile(smbInfo: SmbInfo, fileView: RemoteFileView){

    }
}