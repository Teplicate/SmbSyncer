package ru.teplicate.datasyncersmb.presentation.fragment.home_fragment.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import ru.teplicate.core.domain.SynchronizationUnit
import ru.teplicate.datasyncersmb.presentation.fragment.home_fragment.HomeFragment
import ru.teplicate.datasyncersmb.framework.SyncUnitInteraction

class HomeViewModel(
    private val syncUnitInteraction: SyncUnitInteraction
) : ViewModel(), KoinComponent {


    private val _stateFlow: MutableStateFlow<HomeFragment.HomeUiState> = MutableStateFlow(
        HomeFragment.HomeUiState.SUnitSelected(null)
    )

    val stateFlow: StateFlow<HomeFragment.HomeUiState> = _stateFlow

    suspend fun readAllSyncUnits() =
        syncUnitInteraction.readSyncUnits()

    fun unitSelected(unitEntity: SynchronizationUnit) {
        viewModelScope.launch {
            _stateFlow.emit(HomeFragment.HomeUiState.SUnitSelected(unitEntity))
        }
    }

    fun unitUnselected() {
        viewModelScope.launch {
            _stateFlow.emit(HomeFragment.HomeUiState.SUnitSelected(null))
        }
    }

    fun deleteUnit(unitEntity: SynchronizationUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            syncUnitInteraction.deleteSyncUnit(unitEntity)
        }
    }
}