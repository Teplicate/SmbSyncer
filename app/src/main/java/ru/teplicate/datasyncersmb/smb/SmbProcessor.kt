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
import com.hierynomus.protocol.commons.EnumWithValue
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import ru.teplicate.core.domain.SmbInfo
import ru.teplicate.core.domain.SynchronizationUnit
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.framework.database.entity.SynchronizationUnitEntity
import ru.teplicate.datasyncersmb.enums.ConnectionState
import ru.teplicate.datasyncersmb.presentation.SmbInfoPresentation
import ru.teplicate.datasyncersmb.presentation.SyncUnitPresentation
import java.io.InputStream
import java.util.*


class SmbProcessor {
    private val smbClient = SMBClient()

    @Throws(SMBApiException::class)
    fun testConnection(
        smbInfo: SmbInfoPresentation
    ): ConnectionState {
        val testOk = smbClient.connect(smbInfo.address)
            .use { connection ->

                val auth = if (smbInfo.login.isNotEmpty() || smbInfo.password.isNotEmpty())
                    AuthenticationContext(smbInfo.login, smbInfo.password.toCharArray(), "")
                else AuthenticationContext.guest()

                val session = connection.authenticate(auth)

                with(session as Session) {
                    this.connectShare(smbInfo.directory)
                        .use { }

                    ConnectionState.CONNECTION_OK
                }
            }

        return testOk
    }

    private fun <RV> executeAuthenticatedAction(
        smbInfo: SmbInfoPresentation,
        action: (Session, SmbInfoPresentation) -> RV
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
    private fun testConnection(session: Session, smbInfo: SmbInfoPresentation): ConnectionState {
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
        syncUnitEntity: SyncUnitPresentation,
        files: Map<String, List<DocumentFile>>,
        syncEventHandler: SyncEventHandler,
        contentResolver: ContentResolver
    ) {
        val smbInfo = syncUnitEntity.smbConnection
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
                        syncEventHandler.processedWithException(
                            docFile,
                            syncUnitEntity = syncUnitEntity
                        )
                        return@executeAuthenticatedAction
                    }

                    syncEventHandler.successfulUploadFile(docFile)
                }
            }
            syncEventHandler.onSyncComplete(syncUnitEntity)
        }
    }


    fun uploadFilesSavingStructure(
        syncUnitEntity: SyncUnitPresentation,
        files: List<DocumentFile>,
        syncEventHandler: SyncEventHandler,
        contentResolver: ContentResolver
    ) {
        val smbInfo = syncUnitEntity.smbConnection

        executeAuthenticatedAction(smbInfo) { session, _ ->
            val share = session.connectShare(smbInfo.directory) as DiskShare
            syncRecursive(files, share, contentResolver, syncEventHandler, syncUnitEntity)
        }

        syncEventHandler.onSyncComplete(syncUnitEntity)
    }


    @WorkerThread
    private fun syncRecursive(
        files: List<DocumentFile>,
        share: DiskShare,
        contentResolver: ContentResolver,
        syncEventHandler: SyncEventHandler,
        syncUnitEntity: SyncUnitPresentation,
        parentDir: String = ""
    ) {
        files.forEach { docFile ->
            if (docFile.isFile) {
                try {
                    syncFile(share, parentDir, docFile, contentResolver)
                } catch (e: Exception) {
                    e.printStackTrace()
                    syncEventHandler.processedWithException(
                        docFile,
                        syncUnitEntity = syncUnitEntity
                    )
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
                    syncUnitEntity,
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
        val accessMask = setOf(AccessMask.MAXIMUM_ALLOWED)
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

    fun listFiles(smbInfo: SmbInfoPresentation, path: String): List<RemoteFileView> {
        val files = executeAuthenticatedAction(smbInfo) { session, smbInfo ->
            val share = session.connectShare(smbInfo.directory) as DiskShare

            share.list(path)
                .mapNotNull { e ->
                    if (e.fileName.equals(".") || e.fileName.equals(".."))
                        null
                    else {
                        val isDirectory = EnumWithValue.EnumUtils.isSet(
                            e.fileAttributes,
                            FileAttributes.FILE_ATTRIBUTE_DIRECTORY
                        )
                        RemoteFileView(
                            name = e.fileName,
                            size = e.allocationSize,
                            path = path + "/" + e.fileName,
                            fileId = e.fileId,
                            isDirectory = isDirectory,
                            createdAt = e.creationTime.toEpochMillis()
                        )
                    }
                }
        }

        return files
    }

    fun downloadFiles(
        smbInfo: SmbInfoPresentation,
        filesView: List<RemoteFileView>,
        streamProcessor: (String, InputStream) -> Unit,
        downloadEventHandler: DownloadEventHandler
    ) {
        val accessMask = setOf(AccessMask.GENERIC_READ)
        val attributes = setOf(FileAttributes.FILE_ATTRIBUTE_NORMAL)
        val shareAccess = setOf(SMB2ShareAccess.FILE_SHARE_READ)
        val createDisp = SMB2CreateDisposition.FILE_OPEN
        val createOptions = setOf(SMB2CreateOptions.FILE_RANDOM_ACCESS)

        downloadEventHandler.onDownloadStart(filesView.size)

        executeAuthenticatedAction(smbInfo) { session, _ ->
            val share = session.connectShare(smbInfo.directory) as DiskShare

            filesView.forEach { fileView ->
                if (share.fileExists(fileView.path)) {
                    val file = share.openFile(
                        fileView.path,
                        accessMask,
                        attributes,
                        shareAccess,
                        createDisp,
                        createOptions
                    )
                    file.inputStream.use { io ->
                        streamProcessor(fileView.name, io)
                    }
                }

                downloadEventHandler.successfulDownloadFile(fileView)
            }
        }

        downloadEventHandler.onDownloadComplete()
    }

    abstract class SyncEventHandler {
        abstract fun processedWithException(
            docFile: DocumentFile,
            syncUnitEntity: SyncUnitPresentation
        )

        abstract fun successfulUploadFile(docFile: DocumentFile)

        abstract fun onStartSync(totalElements: Int)

        abstract fun onSyncComplete(syncUnitEntity: SyncUnitPresentation)

        open fun onReadingFiles() {}

        open fun onCopying() {}

        open fun onRemovingFiles() {}
    }

    abstract class DownloadEventHandler {
        abstract fun processedWithException(fileView: RemoteFileView)

        abstract fun successfulDownloadFile(fileView: RemoteFileView)

        abstract fun onDownloadStart(totalElements: Int)

        abstract fun onDownloadComplete()
    }
}