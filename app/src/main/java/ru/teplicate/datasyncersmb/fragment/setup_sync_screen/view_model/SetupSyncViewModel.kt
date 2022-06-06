package ru.teplicate.datasyncersmb.fragment.setup_sync_screen.view_model

import android.app.Application
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.teplicate.datasyncersmb.content_processor.ContentProcessor
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.database.entity.FileInfo
import ru.teplicate.datasyncersmb.database.entity.SmbConnection
import ru.teplicate.datasyncersmb.database.entity.SynchronizationInfo
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.database.repository.SyncUnitRepository
import ru.teplicate.datasyncersmb.enums.SetupSyncEvent
import ru.teplicate.datasyncersmb.enums.SyncOption
import ru.teplicate.datasyncersmb.manager.SyncManager
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import java.sql.Date
import java.util.*
import kotlin.collections.HashSet

class SetupSyncViewModel(
    private val syncUnitRepository: SyncUnitRepository
) :
    ViewModel() {

    private var syncJob: Job? = null

    private val _contentUri: MutableLiveData<Uri?> = MutableLiveData(null)
    val contentUri: LiveData<Uri?>
        get() = _contentUri

    private val _createDirectory = MutableLiveData(false)
    val createDirectory: LiveData<Boolean>
        get() = _createDirectory

    private val _eventState: MutableLiveData<SetupSyncEvent> = MutableLiveData(SetupSyncEvent.IDLE)
    val eventState: LiveData<SetupSyncEvent>
        get() = _eventState

    private val _optionsSet: MutableLiveData<MutableSet<SyncOption>> = MutableLiveData(HashSet())
    val optionsSet: LiveData<MutableSet<SyncOption>>
        get() = _optionsSet

    private var smbInfo: SmbInfo? = null

    private var syncDirectoryName: String? = null

    private var syncUnitName: String? = null

    fun setupContentUri(uri: Uri?) {
        this._contentUri.postValue(uri)
    }

    fun setupSmbInfo(smbInfo: SmbInfo) {
        this.smbInfo = smbInfo
    }

    fun createDirectory(create: Boolean) {
        _createDirectory.postValue(create)
    }

    private fun fireEvent(event: SetupSyncEvent) {
        _eventState.postValue(event)
    }

    fun saveSyncUnit() {
        viewModelScope.launch(Dispatchers.IO) {
            syncUnitRepository.saveSyncUnit(
                requireNotNull(smbInfo),
                requireNotNull(_contentUri.value),
                name = syncUnitName ?: smbInfo!!.address,
                syncDirectoryName = syncDirectoryName,
                options = optionsSet.value?.toList() ?: emptyList()
            )
        }
            .invokeOnCompletion {
                fireEvent(SetupSyncEvent.UNIT_SAVED)
            }
    }

    private fun makeSyncUnit(
        smbInfo: SmbInfo,
        contentUri: Uri,
        name: String?,
        syncDirectoryName: String?,
        options: List<SyncOption>
    ) =
        SynchronizationUnit(
            name = name,
            contentUri = contentUri.toString(),
            smbConnection = SmbConnection(
                address = smbInfo.address,
                user = smbInfo.login,
                password = smbInfo.password,
                sharedDirectory = smbInfo.directory
            ),
            synchronizationInfo = SynchronizationInfo(null, syncDirectoryName),
            synchronizationOptions = options
        )

    fun isContentSet() = _contentUri.value != null

    fun cancelSyncJob() = syncJob?.cancel()

    fun setSyncDirectoryName(text: CharSequence) {
        syncDirectoryName = text.toString()
    }

    fun setSyncUnitName(text: CharSequence) {
        syncUnitName = text.toString()
    }

    fun checkOption(syncOption: SyncOption, add: Boolean) {
        if (add) {
            requireNotNull(optionsSet.value).add(syncOption)
        } else {
            requireNotNull(optionsSet.value).remove(syncOption)
        }

        _optionsSet.postValue(_optionsSet.value)
    }
}