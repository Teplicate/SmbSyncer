package ru.teplicate.datasyncersmb.presentation.fragment.home_fragment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.teplicate.core.domain.SynchronizationUnit
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.databinding.FragmentHomeBinding
import ru.teplicate.datasyncersmb.presentation.fragment.core.AbstractMasterDetailFragment
import ru.teplicate.datasyncersmb.presentation.fragment.dialog.SyncDialog
import ru.teplicate.datasyncersmb.presentation.fragment.dialog.SyncDialogListener
import ru.teplicate.datasyncersmb.presentation.fragment.home_fragment.view_model.HomeViewModel
import ru.teplicate.datasyncersmb.presentation.SyncUnitPresentation
import ru.teplicate.datasyncersmb.framework.service.SYNC_DIALOG_MESSENGER
import ru.teplicate.datasyncersmb.framework.service.SYNC_UNIT_KEY
import ru.teplicate.datasyncersmb.framework.service.SyncServiceForeground

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
            binding.btnSyncDirectory.isEnabled = false
            syncInPlace()
        }

        binding.btnUploadSelection.setOnClickListener {

        }
    }

    private fun syncStarted() {
//        launchProgressDialog()
    }

    private fun unitSelected(sUnit: SynchronizationUnit?) {
        binding.buttonsContainer.visibility =
            if (sUnit != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupSUnits(syncUnitEntities: List<SynchronizationUnit>) {
        if (syncUnitEntities.isEmpty()) {
            binding.containerSavedConnections.visibility = View.GONE
            binding.btnSetupConnection.visibility = View.VISIBLE
            binding.fabNewConnection.visibility = View.GONE
        } else {
            binding.containerSavedConnections.visibility = View.VISIBLE
            binding.btnSetupConnection.visibility = View.GONE
            binding.fabNewConnection.visibility = View.VISIBLE
            setupSyncUnitsRv(syncUnitEntities)
        }
    }

    private fun UpdateUI(uiState: HomeUiState) {

    }

    private fun setupSyncUnitsRv(syncUnitEntities: List<SynchronizationUnit>) {
        (binding.rvSavedConnections.adapter as SyncUnitAdapter).supplySyncUnits(syncUnitEntities)
    }

    override fun onSelect(unitEntity: SynchronizationUnit) {
        viewModel.unitSelected(unitEntity)
        binding.fabNewConnection.visibility = View.GONE
    }

    override fun onUnselect() {
        viewModel.unitUnselected()
    }

    override fun onEditClickListener(unitEntity: SynchronizationUnit) {
        TODO("Not yet implemented")
    }

    override fun onDeleteClickListener(unitEntity: SynchronizationUnit, position: Int) {
        viewModel.deleteUnit(unitEntity)
        viewModel.unitUnselected()
        (binding.rvSavedConnections.adapter as SyncUnitAdapter).removeItem(position)
    }

    private fun syncInPlace() {
        launchProgressDialog()
    }

    private fun launchProgressDialog() {
        syncDialog = SyncDialog(this)
        requireNotNull(syncDialog).show(childFragmentManager, SyncDialog.TAG)
    }

    override fun onDialogInitialized() {
        val serviceIntent = Intent(requireContext(), SyncServiceForeground::class.java).also {
            it.putExtra(
                SYNC_UNIT_KEY,
                SyncUnitPresentation.fromDomainToPresent(requireNotNull(viewModel.stateFlow.value as HomeUiState.SUnitSelected).sUnit!!)
            )
            it.putExtra(SYNC_DIALOG_MESSENGER, syncDialog!!.createSyncHandlerMessenger())
        }

        requireContext().startService(serviceIntent)
    }

    override fun onHideDialog() {
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