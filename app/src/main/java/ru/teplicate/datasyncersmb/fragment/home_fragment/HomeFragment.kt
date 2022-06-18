package ru.teplicate.datasyncersmb.fragment.home_fragment

import android.view.LayoutInflater
import android.view.View
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collect
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
                viewModel.readAllSyncUnits().collect { syncUnits ->
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
            }
        }

        viewModel.selectedUnit.observe(viewLifecycleOwner, selectedUnitObserver())
        viewModel.totalElements.observe(viewLifecycleOwner) { total ->
            total?.let {
                startSync(it)
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

        binding.btnDownloadFrom.setOnClickListener {
            val smbInfo = requireNotNull(viewModel.selectedUnit.value).smbConnection.toSmbInfo()
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToSharedFilesFragment(smbInfo)
            )
        }

        binding.btnUploadSelection.setOnClickListener {

        }
    }

    private fun UpdateUI(uiState: HomeUiState) {

    }

    private fun syncStateObserver(): Observer<in SyncState> {
        return Observer { state ->
            if (state != SyncState.IDLE) {
                requireNotNull(syncDialog).changeState(state)
            }
        }
    }

    private fun selectedUnitObserver(): Observer<in SynchronizationUnit?> {
        return Observer { selected ->
            if (selected != null) {
                binding.buttonsContainer.visibility = View.VISIBLE
            } else {
                binding.buttonsContainer.visibility = View.GONE
            }
        }
    }

    private fun syncUnitsObserver(): Observer<List<SynchronizationUnit>> {
        return Observer { syncUnits ->
        }
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
        registerSyncObservers()
        val syncEventHandler = syncEventHandler()

        launchProgressDialog()
        viewModel.syncData(syncEventHandler)
    }

    private fun removeSyncObservers() {
        viewModel.syncState.removeObservers(viewLifecycleOwner)
        viewModel.uploadedFile.removeObservers(viewLifecycleOwner)
    }

    private fun registerSyncObservers() {
        viewModel.uploadedFile.observe(viewLifecycleOwner, uploadedFileObserver())
        viewModel.syncState.observe(viewLifecycleOwner, syncStateObserver())
    }

    private fun syncEventHandler() = object : SmbProcessor.SyncEventHandler() {
        override fun processedWithException(
            docFile: DocumentFile,
            syncUnit: SynchronizationUnit
        ) {
            viewModel.syncFailedOn(docFile, syncUnit)
        }

        override fun successfulUploadFile(docFile: DocumentFile) {
            viewModel.fileUploaded(docFile)
        }

        override fun onStartSync(totalElements: Int) {
            viewModel.startSync(totalElements)
        }

        override fun onSyncComplete(syncUnit: SynchronizationUnit) {
            viewModel.onSyncComplete(syncUnit)
        }

        override fun onReadingFiles() {
            viewModel.changeSyncState(SyncState.READING_FILES)
        }

        override fun onCopying() {
            viewModel.changeSyncState(SyncState.COPYING)
        }
    }

    private fun startSync(totalElements: Int) {
        requireNotNull(syncDialog).setupProgBar(totalElements)
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
        removeSyncObservers()
        viewModel.cancelSyncJob()
        syncDialog = null
    }

    override fun onFinish() {
        TODO("Not yet implemented")
    }

    enum class HomeUiState {
        NO_SUNITS,
        HAS_SUNITS,
        SUNIT_SELECTED,
        SUNIT_UNSELECTED,
        SYNC_STARTED,
        SYNC_CANCELLED
    }
}