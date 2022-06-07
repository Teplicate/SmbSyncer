package ru.teplicate.datasyncersmb.fragment.shared_files_fragment.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.manager.DownloadManager
import java.util.*

const val PATH_SEPARATOR = "/"
const val EMPTY_PATH = ""

class SharedFilesViewModel(private val downloadManager: DownloadManager) : ViewModel() {

    private val _sharedFiles: MutableLiveData<List<RemoteFileView>> = MutableLiveData(emptyList())
    val sharedFiles: LiveData<List<RemoteFileView>>
        get() = _sharedFiles

    private var path: MutableList<String> = LinkedList<String>().also { it.add(EMPTY_PATH) }
    private lateinit var connectionInfo: SmbInfo

    fun setupConnectionInfo(smbInfo: SmbInfo) {
        this.connectionInfo = smbInfo
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
}