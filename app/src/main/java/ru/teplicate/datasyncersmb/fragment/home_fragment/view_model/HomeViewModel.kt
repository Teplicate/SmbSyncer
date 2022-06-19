package ru.teplicate.datasyncersmb.fragment.home_fragment.view_model

import android.app.Application
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.teplicate.datasyncersmb.database.entity.FileInfo
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.database.repository.SyncUnitRepository
import ru.teplicate.datasyncersmb.enums.SyncState
import ru.teplicate.datasyncersmb.fragment.home_fragment.HomeFragment
import ru.teplicate.datasyncersmb.manager.SyncManager
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import java.sql.Date
import java.util.*

class HomeViewModel(
    private val syncUnitRepository: SyncUnitRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    fun loadSyncUnits() = syncUnitRepository.readSyncUnits()

    private val _stateFlow: MutableStateFlow<HomeFragment.HomeUiState> = MutableStateFlow(
        HomeFragment.HomeUiState.SUnitSelected(null)
    )

    val stateFlow: StateFlow<HomeFragment.HomeUiState> = _stateFlow

   /* private val _uploadedFile: MutableLiveData<DocumentFile?> = MutableLiveData(null)
    val uploadedFile: LiveData<DocumentFile?>
        get() = _uploadedFile

    private val _syncState: MutableLiveData<SyncState> = MutableLiveData(SyncState.IDLE)
    val syncState: LiveData<SyncState>
        get() = _syncState

    private val _totalElements: MutableLiveData<Int?> = MutableLiveData(null)
    val totalElements: LiveData<Int?>
        get() = _totalElements
*/
    private var syncJob: Job? = null

    fun readAllSyncUnits() =
        syncUnitRepository
            .readSyncUnits()

    fun unitSelected(unit: SynchronizationUnit) {
        viewModelScope.launch {
            _stateFlow.emit(HomeFragment.HomeUiState.SUnitSelected(unit))
        }
    }

    fun syncInPlace() {
        TODO("REWORK DIALOG ")
        registerSyncObservers()
        val syncEventHandler = syncEventHandler()
        viewModel.syncData(syncEventHandler)
    }

    fun unitUnselected() {
        viewModelScope.launch {
            _stateFlow.emit(HomeFragment.HomeUiState.SUnitSelected(null))
        }
    }


    fun deleteUnit(unit: SynchronizationUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            syncUnitRepository.deleteSyncUnit(unit)
        }
    }

    fun syncData(syncEventHandler: SmbProcessor.SyncEventHandler) {
        /*  syncJob = viewModelScope.launch(Dispatchers.IO) {
              syncManager.syncContentFromDirectory(
                  requireNotNull(_selectedUnit.value),
                  syncEventHandler
              )
          }*/
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
        syncUnit.synchronizationInfo?.lastSyncDate = Date(Calendar.getInstance().timeInMillis)
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

    fun startSync(totalElements: Int) {

    }

    private fun syncEventHandler() = object : SmbProcessor.SyncEventHandler() {
        override fun processedWithException(
            docFile: DocumentFile,
            syncUnit: SynchronizationUnit
        ) {
            syncFailedOn(docFile, syncUnit)
        }

        override fun successfulUploadFile(docFile: DocumentFile) {
            fileUploaded(docFile)
        }

        override fun onStartSync(totalElements: Int) {
            startSync(totalElements)
        }

        override fun onSyncComplete(syncUnit: SynchronizationUnit) {
//            onSyncComplete(syncUnit)
        }

        override fun onReadingFiles() {
            changeSyncState(SyncState.READING_FILES)
        }

        override fun onCopying() {
            changeSyncState(SyncState.COPYING)
        }
    }
}