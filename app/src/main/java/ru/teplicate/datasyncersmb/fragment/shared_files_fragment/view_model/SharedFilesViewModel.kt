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

const val pathSeparator = "/"

class SharedFilesViewModel(private val downloadManager: DownloadManager) : ViewModel() {

    private val _sharedFiles: MutableLiveData<List<RemoteFileView>> = MutableLiveData(emptyList())
    val sharedFiles: LiveData<List<RemoteFileView>>
        get() = _sharedFiles

    private var path: MutableList<String> = LinkedList<String>().also { it.add(".") }


    fun listFiles(smbInfo: SmbInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = downloadManager.listFiles(smbInfo, path.joinToString(pathSeparator))
            _sharedFiles.postValue(files)
        }
    }

    fun onDirectorySelected(smbInfo: SmbInfo, dirName: String) {
        addChildInPath(child = dirName)
        listFiles(smbInfo)
    }

    private fun addChildInPath(child: String) {
        path += child
    }

    private fun removeChildFromPath() {
        val found = path.removeLast()
    }

    fun onHierarchyUp(smbInfo: SmbInfo) {
        removeChildFromPath()
        listFiles(smbInfo)
    }
}