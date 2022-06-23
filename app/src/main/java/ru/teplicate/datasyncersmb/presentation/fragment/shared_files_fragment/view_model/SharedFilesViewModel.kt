package ru.teplicate.datasyncersmb.presentation.fragment.shared_files_fragment.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.enums.DownloadEvent
import ru.teplicate.datasyncersmb.enums.RecyclerViewMode
import ru.teplicate.datasyncersmb.manager.DownloadManager
import ru.teplicate.datasyncersmb.presentation.SmbInfoPresentation
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import java.util.*
import kotlin.collections.HashSet

const val PATH_SEPARATOR = "/"
const val EMPTY_PATH = ""

class SharedFilesViewModel(private val downloadManager: DownloadManager) : ViewModel() {

    private val _sharedFiles: MutableLiveData<List<RemoteFileView>> = MutableLiveData(emptyList())
    val sharedFiles: LiveData<List<RemoteFileView>>
        get() = _sharedFiles

    private var path: MutableList<String> = LinkedList<String>().also { it.add(EMPTY_PATH) }

    private val selectedFiles: MutableSet<RemoteFileView> = HashSet()

    private lateinit var connectionInfo: SmbInfoPresentation
    private val _sharedFilesRVMode: MutableLiveData<RecyclerViewMode> =
        MutableLiveData(RecyclerViewMode.CLICK)
    val sharedFilesRVMode: LiveData<RecyclerViewMode>
        get() = _sharedFilesRVMode

    private val _totalDownloadFiles: MutableLiveData<Int?> = MutableLiveData()
    val totalDownloadFiles: LiveData<Int?>
        get() = _totalDownloadFiles

    private val _fileDownloaded: MutableLiveData<RemoteFileView?> = MutableLiveData()
    val fileDownloaded: LiveData<RemoteFileView?>
        get() = _fileDownloaded

    private val _downloadState: MutableLiveData<DownloadEvent> = MutableLiveData(DownloadEvent.IDLE)
    val downloadState: LiveData<DownloadEvent>
        get() = _downloadState

    fun setupConnectionInfo(smbInfo: SmbInfoPresentation) {
        this.connectionInfo = smbInfo
    }

    fun downloadStart(totalElements: Int) {
        _totalDownloadFiles.postValue(totalElements)
    }

    fun fileDownloaded(remoteFileView: RemoteFileView) {
        _fileDownloaded.postValue(remoteFileView)
    }

    fun downloadFinished() {
        _downloadState.postValue(DownloadEvent.DOWNLOAD_FINISHED)
    }


    fun listFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = downloadManager.listFiles(connectionInfo, path.joinToString(PATH_SEPARATOR))
            _sharedFiles.postValue(files)
        }
    }

    fun onDirectorySelected(dirName: String) {
        addChildInPath(child = dirName)
        listFiles()
    }

    private fun addChildInPath(child: String) {
        path += child
    }

    private fun removeChildFromPath() {
        if (path.size == 1) {
            return
        }

        path.removeLast()
        listFiles()
    }

    fun onHierarchyUp() {
        removeChildFromPath()
        listFiles()
    }

    fun onBackToRoot() {
        path.clear()
        path.add(EMPTY_PATH)
        listFiles()
    }

    fun downloadFile(
        fileView: RemoteFileView,
        downloadEventHandler: SmbProcessor.DownloadEventHandler
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadManager.downloadFiles(
                smbInfo = connectionInfo,
                listOf(fileView),
                downloadEventHandler
            )
        }
    }

    fun changeRVMode() {
        _sharedFilesRVMode.value = if (_sharedFilesRVMode.value == RecyclerViewMode.CLICK)
            RecyclerViewMode.SELECT else RecyclerViewMode.CLICK
    }

    fun fileSelected(fileView: RemoteFileView) {
        val selected = fileView.isFileSelected()

        if (selected) {
            selectedFiles.remove(fileView)

            if (selectedFiles.isEmpty()) {
                changeRVMode()
            }

            return
        }

        selectedFiles.add(fileView)
    }
}