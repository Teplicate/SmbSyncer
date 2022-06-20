package ru.teplicate.datasyncersmb.fragment.home_fragment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.databinding.FragmentHomeBinding
import ru.teplicate.datasyncersmb.enums.SyncState
import ru.teplicate.datasyncersmb.fragment.core.AbstractMasterDetailFragment
import ru.teplicate.datasyncersmb.fragment.dialog.SyncDialog
import ru.teplicate.datasyncersmb.fragment.dialog.SyncDialogListener
import ru.teplicate.datasyncersmb.fragment.home_fragment.view_model.HomeViewModel
import ru.teplicate.datasyncersmb.service.SYNC_UNIT_KEY
import ru.teplicate.datasyncersmb.service.SyncServiceForeground
import ru.teplicate.datasyncersmb.smb.SmbProcessor

class HomeFragment : AbstractMasterDetailFragment(), SyncUnitAdapter.SyncItemClickListener,
    SyncDialogListener {

    override val layoutId: Int
        get() = R.layout.fragment_home

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModel()
    private var syncDialog: SyncDialog? = null

    override fun bindViews(layoutInflater: LayoutInflater): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun setupViews() {
        binding.rvSavedConnections.adapter = SyncUnitAdapter(this)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.readAllSyncUnits().collect { units ->
                        setupSUnits(units)
                    }
                }

                launch {
                    viewModel.stateFlow.collect { uiState ->
                        when (uiState) {
//                        is HomeUiState.LoadedSUnits -> setupSUnits(uiState.syncUnits)
                            is HomeUiState.SUnitSelected -> unitSelected(uiState.sUnit)
                            is HomeUiState.SyncStarted -> syncStarted()
                        }
                    }
                }
            }
        }


        binding.btnSetupConnection.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddressFragment())
        }


        binding.fabNewConnection.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddressFragment())
        }

        binding.btnSyncDirectory.setOnClickListener {
            syncInPlace()
        }

        binding.btnUploadSelection.setOnClickListener {

        }
    }

    private fun syncStarted() {
        launchProgressDialog()
    }

    private fun unitSelected(sUnit: SynchronizationUnit?) {
        binding.buttonsContainer.visibility =
            if (sUnit != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupSUnits(syncUnits: List<SynchronizationUnit>) {
        if (syncUnits.isEmpty()) {
            binding.containerSavedConnections.visibility = View.GONE
            binding.btnSetupConnection.visibility = View.VISIBLE
            binding.fabNewConnection.visibility = View.GONE
        } else {
            binding.containerSavedConnections.visibility = View.VISIBLE
            binding.btnSetupConnection.visibility = View.GONE
            binding.fabNewConnection.visibility = View.VISIBLE
            setupSyncUnitsRv(syncUnits)
        }
    }

    private fun UpdateUI(uiState: HomeUiState) {

    }

    private fun setupSyncUnitsRv(syncUnits: List<SynchronizationUnit>) {
        (binding.rvSavedConnections.adapter as SyncUnitAdapter).supplySyncUnits(syncUnits)
    }

    override fun onSelect(unit: SynchronizationUnit) {
        viewModel.unitSelected(unit)
        binding.fabNewConnection.visibility = View.GONE
    }

    override fun onUnselect() {
        viewModel.unitUnselected()
    }

    override fun onEditClickListener(unit: SynchronizationUnit) {
        TODO("Not yet implemented")
    }

    override fun onDeleteClickListener(unit: SynchronizationUnit, position: Int) {
        viewModel.deleteUnit(unit)
        viewModel.unitUnselected()
        (binding.rvSavedConnections.adapter as SyncUnitAdapter).removeItem(position)
    }

    private fun syncInPlace() {
        val serviceIntent = Intent(requireContext(), SyncServiceForeground::class.java).also {
            it.putExtra(
                SYNC_UNIT_KEY,
                requireNotNull(viewModel.stateFlow.value as HomeUiState.SUnitSelected).sUnit
            )
        }
        requireContext().startService(serviceIntent)
    }

    private fun launchProgressDialog() {
        syncDialog = SyncDialog(this)
        requireNotNull(syncDialog).show(childFragmentManager, SyncDialog.TAG)
    }

    private fun uploadedFileObserver(): Observer<in DocumentFile?> {
        return Observer { doc ->
            doc?.let {
                syncDialog?.updateProgress()
            }
        }
    }

    override fun onCancelSync() {
//        removeSyncObservers()
//        viewModel.cancelSyncJob()
        syncDialog = null
    }

    override fun onFinish() {
        TODO("Not yet implemented")
    }


    sealed class HomeUiState {
        class SUnitSelected(val sUnit: SynchronizationUnit?) : HomeUiState()

        class SyncStarted() : HomeUiState()
    }
}