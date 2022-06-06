package ru.teplicate.datasyncersmb.fragment.home_screen.view_model

import android.app.Application
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.teplicate.datasyncersmb.database.entity.FileInfo
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.database.repository.SyncUnitRepository
import ru.teplicate.datasyncersmb.enums.SyncState
import ru.teplicate.datasyncersmb.fragment.dialog.SyncDialogListener
import ru.teplicate.datasyncersmb.manager.SyncManager
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import java.sql.Date
import java.util.*

class HomeViewModel(
    application: Application,
    private val syncUnitRepository: SyncUnitRepository,
    private val syncManager: SyncManager
) : AndroidViewModel(application) {

    private val _selectedUnit: MutableLiveData<SynchronizationUnit?> = MutableLiveData(null)
    val selectedUnit: LiveData<SynchronizationUnit?>
        get() = _selectedUnit

    private val _uploadedFile: MutableLiveData<DocumentFile?> = MutableLiveData(null)
    val uploadedFile: LiveData<DocumentFile?>
        get() = _uploadedFile

    private val _syncState: MutableLiveData<SyncState> = MutableLiveData(SyncState.IDLE)
    val syncState: LiveData<SyncState>
        get() = _syncState

    private var syncJob: Job? = null

    fun readAllSyncUnits() =
        syncUnitRepository
            .readSyncUnits()
            .asLiveData()

    fun unitSelected(unit: SynchronizationUnit) {
        _selectedUnit.postValue(unit)
    }

    fun unitUnselected() {
        _selectedUnit.postValue(null)
    }

    fun deleteUnit(unit: SynchronizationUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            syncUnitRepository.deleteSyncUnit(unit)
        }
    }

    fun syncData(syncEventHandler: SmbProcessor.SyncEventHandler) {
        syncJob = viewModelScope.launch(Dispatchers.IO) {
            syncManager.syncContentFromDirectory(
                requireNotNull(_selectedUnit.value),
                getApplication<Application>(),
                syncEventHandler
            )
        }
    }

    fun syncFailedOn(docFile: DocumentFile, syncUnit: SynchronizationUnit) {
        syncUnit.failedSyncInfo = FileInfo(
            fileName = docFile.name ?: "",
            fileUri = docFile.uri.toString(),
            fileDate = Date(docFile.lastModified())
        )
        onSyncComplete(syncUnit)
    }

    fun onSyncComplete(syncUnit: SynchronizationUnit) {
        syncUnit.synchronizationInfo.lastSyncDate = Date(Calendar.getInstance().timeInMillis)
        syncUnitRepository.updateSyncUnit(syncUnit)
    }

    fun cancelSyncJob() {
        syncJob?.cancel()
        _uploadedFile.postValue(null)
    }

    fun fileUploaded(docFile: DocumentFile) {
        _uploadedFile.postValue(docFile)
    }

    fun changeSyncState(state: SyncState) {
        _syncState.postValue(state)
    }
}