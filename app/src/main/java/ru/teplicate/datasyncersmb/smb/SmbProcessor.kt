package ru.teplicate.datasyncersmb.smb

import android.content.ContentResolver
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.coroutineScope
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.enums.ConnectionState
import java.util.*
import kotlin.coroutines.coroutineContext


class SmbProcessor {
    private val smbClient = SMBClient()

    @Throws(SMBApiException::class)
    fun testConnection(
        smbInfo: SmbInfo
    ): ConnectionState {
        val testOk = smbClient.connect(smbInfo.address)
            .use { connection ->

                val auth = if (smbInfo.login.isNotEmpty() || smbInfo.password.isNotEmpty())
                    AuthenticationContext(smbInfo.login, smbInfo.password.toCharArray(), "")
                else AuthenticationContext.guest()

                val session = connection.authenticate(auth)

                with(session as Session) {
                    val share = this.connectShare(smbInfo.directory)

                    ConnectionState.CONNECTION_OK
                }
            }

        return testOk
    }

    private fun <RV> executeAuthenticatedAction(
        smbInfo: SmbInfo,
        action: (Session, SmbInfo) -> RV
    ): RV {
        smbClient.connect(smbInfo.address)
            .use { connection ->

                val auth = if (smbInfo.login.isNotEmpty() || smbInfo.password.isNotEmpty())
                    AuthenticationContext(smbInfo.login, smbInfo.password.toCharArray(), "")
                else AuthenticationContext.guest()
                val session = connection.authenticate(auth)

                return action(session, smbInfo)
            }
    }

    @Throws(SMBApiException::class)
    private fun testConnection(session: Session, smbInfo: SmbInfo): ConnectionState {
        return with(session) {
            this.connectShare(smbInfo.directory)

            ConnectionState.CONNECTION_OK
        }
    }

    private fun processErrorNtStatus(status: NtStatus): ConnectionState {
        return when (status.name) {
            "STATUS_ACCESS_DENIED" -> ConnectionState.AUTH_REQUIRED
            "STATUS_BAD_NETWORK_NAME" -> ConnectionState.INVALID_SHARE_NAME
            else -> throw IllegalArgumentException("new argument ${status.name}")
        }
    }

    fun processException(smbApiException: SMBApiException): ConnectionState {
        val ntStatus = smbApiException.status
        return when {
            ntStatus.isError -> {
                processErrorNtStatus(ntStatus)
            }
            else -> ConnectionState.NA
        }
    }


    fun uploadFilesGroupingByDate(
        syncUnit: SynchronizationUnit,
        files: Map<String, List<DocumentFile>>,
        syncEventHandler: SyncEventHandler,
        contentResolver: ContentResolver
    ) {
        val smbInfo = syncUnit.smbConnection.toSmbInfo()
        executeAuthenticatedAction(smbInfo) { session, _ ->
            val share = session.connectShare(smbInfo.directory) as DiskShare

            for ((date, docFiles) in files) {
                if (!share.folderExists(date)) {
                    share.mkdir(date)
                }

                docFiles.forEach { docFile ->
                    try {
                        syncFile(share, date, docFile, contentResolver)
                    } catch (e: Exception) {
                        syncEventHandler.processedWithException(docFile, syncUnit = syncUnit)
                        return@executeAuthenticatedAction
                    }

                    syncEventHandler.successfulUploadFile(docFile)
                }
            }
            syncEventHandler.onSyncComplete(syncUnit)
        }
    }


    fun uploadFilesSavingStructure(
        syncUnit: SynchronizationUnit,
        files: List<DocumentFile>,
        syncEventHandler: SyncEventHandler,
        contentResolver: ContentResolver
    ) {
        val smbInfo = syncUnit.smbConnection.toSmbInfo()

        executeAuthenticatedAction(smbInfo) { session, _ ->
            val share = session.connectShare(smbInfo.directory) as DiskShare
            syncRecursive(files, share, contentResolver, syncEventHandler, syncUnit)
        }

        syncEventHandler.onSyncComplete(syncUnit)
    }


    @WorkerThread
    private fun syncRecursive(
        files: List<DocumentFile>,
        share: DiskShare,
        contentResolver: ContentResolver,
        syncEventHandler: SyncEventHandler,
        syncUnit: SynchronizationUnit,
        parentDir: String = ""
    ) {
        files.forEach { docFile ->
            if (docFile.isFile) {
                try {
                    syncFile(share, parentDir, docFile, contentResolver)
                } catch (e: Exception) {
                    syncEventHandler.processedWithException(docFile, syncUnit = syncUnit)
                    return
                }
            } else if (docFile.isDirectory) {
                val subDirName = requireNotNull(docFile.name)
                share.mkdir(subDirName)

                val sortedSubdir = docFile.listFiles()
                    .map { it }
                    .sortedBy { it.lastModified() }

                val parentDirNested =
                    if (parentDir.isEmpty()) subDirName else "$parentDir/$subDirName"

                syncRecursive(
                    sortedSubdir,
                    share,
                    contentResolver,
                    syncEventHandler,
                    syncUnit,
                    parentDirNested
                )
            }
            syncEventHandler.successfulUploadFile(docFile)
        }
    }


    @Throws(Exception::class)
    private fun syncFile(
        share: DiskShare,
        parentDir: String,
        docFile: DocumentFile,
        contentResolver: ContentResolver
    ) {
        val accessMask = setOf(AccessMask.GENERIC_ALL)
        val attributes = setOf(FileAttributes.FILE_ATTRIBUTE_NORMAL)
        val shareAccess = setOf(SMB2ShareAccess.FILE_SHARE_WRITE)
        val createDisp = SMB2CreateDisposition.FILE_OPEN_IF
        val createOptions = setOf(SMB2CreateOptions.FILE_RANDOM_ACCESS)
        val fileName = "${docFile.name ?: UUID.randomUUID()}"
        val path = if (parentDir.isEmpty()) fileName else "$parentDir/$fileName"
        val file = share.openFile(
            path,
            accessMask,
            attributes,
            shareAccess,
            createDisp,
            createOptions
        )

        file.outputStream.use { outStream ->
            requireNotNull(contentResolver.openInputStream(docFile.uri)).use { inStream ->
                val buffer = ByteArray(1024)
                var length: Int

                while (inStream.read(buffer).also { length = it } != -1) {
                    outStream.write(buffer, 0, length)
                }
                outStream.flush()
            }
        }
    }

    fun listFiles(smbInfo: SmbInfo, path: String): List<RemoteFileView> {
        val files = executeAuthenticatedAction(smbInfo) { session, smbInfo ->
            val share = session.connectShare(smbInfo.directory) as DiskShare

            share.list(path)
                .map { e ->
                    RemoteFileView(name = e.fileName, size = e.allocationSize, path = path, fileId = e.fileId)
                }
        }

        return files
    }

    abstract class SyncEventHandler {
        abstract fun processedWithException(docFile: DocumentFile, syncUnit: SynchronizationUnit)

        abstract fun successfulUploadFile(docFile: DocumentFile)

        abstract fun totalElements(totalElements: Int)

        abstract fun onSyncComplete(syncUnit: SynchronizationUnit)

        open fun onReadingFiles() {}

        open fun onCopying() {}

        open fun onRemovingFiles() {}
    }
}