package ru.teplicate.datasyncersmb.presentation.fragment.setup_sync_fragment.view_model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.teplicate.core.domain.SyncOption
import ru.teplicate.datasyncersmb.enums.SetupSyncEvent
import ru.teplicate.datasyncersmb.framework.SyncUnitInteraction
import ru.teplicate.datasyncersmb.presentation.SmbInfoPresentation
import ru.teplicate.datasyncersmb.presentation.SyncUnitPresentation

class SetupSyncViewModel(
    private val syncUnitInteraction: SyncUnitInteraction
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

    private var smbInfo: SmbInfoPresentation? = null

    private var syncDirectoryName: String? = null

    private var syncUnitName: String? = null

    fun setupContentUri(uri: Uri?) {
        this._contentUri.postValue(uri)
    }

    fun setupSmbInfo(smbInfo: SmbInfoPresentation) {
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
            syncUnitInteraction.createSyncUnitUseCase(
                makeSyncUnit(
                    smbInfo = requireNotNull(smbInfo),
                    contentUri = requireNotNull(_contentUri.value),
                    name = syncUnitName,
                    syncDirectoryName = syncDirectoryName,
                    options = optionsSet.value?.toList() ?: emptyList()
                ).toDomain()
            )
        }
            .invokeOnCompletion {
                fireEvent(SetupSyncEvent.UNIT_SAVED)
            }
    }

    private fun makeSyncUnit(
        smbInfo: SmbInfoPresentation,
        contentUri: Uri,
        name: String?,
        syncDirectoryName: String?,
        options: List<SyncOption>
    ) =
        SyncUnitPresentation(
            name = name,
            contentUri = contentUri.toString(),
            smbConnection = smbInfo,
            synchronizationOptions = options,
            targetDirectoryName = syncDirectoryName
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