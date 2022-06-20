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
    private val syncUnitRepository: SyncUnitRepository
) : ViewModel() {

    fun loadSyncUnits() = syncUnitRepository.readSyncUnits()

    private val _stateFlow: MutableStateFlow<HomeFragment.HomeUiState> = MutableStateFlow(
        HomeFragment.HomeUiState.SUnitSelected(null)
    )

    val stateFlow: StateFlow<HomeFragment.HomeUiState> = _stateFlow

    fun readAllSyncUnits() =
        syncUnitRepository
            .readSyncUnits()

    fun unitSelected(unit: SynchronizationUnit) {
        viewModelScope.launch {
            _stateFlow.emit(HomeFragment.HomeUiState.SUnitSelected(unit))
        }
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
}