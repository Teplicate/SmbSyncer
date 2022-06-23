package ru.teplicate.datasyncersmb.presentation.fragment.setup_sync_fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.teplicate.core.domain.SyncOption
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.databinding.FragmentSetupSyncBinding
import ru.teplicate.datasyncersmb.enums.SetupSyncEvent
import ru.teplicate.datasyncersmb.presentation.SmbInfoPresentation
import ru.teplicate.datasyncersmb.presentation.fragment.core.AbstractMasterDetailFragment
import ru.teplicate.datasyncersmb.presentation.fragment.setup_sync_fragment.view_model.SetupSyncViewModel

class SetupSyncFragment : AbstractMasterDetailFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_setup_sync

    private lateinit var smbInfo: SmbInfoPresentation
    private lateinit var binding: FragmentSetupSyncBinding
    private lateinit var contentProvider: ActivityResultLauncher<Intent>
    private val viewModel: SetupSyncViewModel by viewModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contentProvider = callFileManager()
    }

    override fun bindViews(layoutInflater: LayoutInflater): View {
        binding = FragmentSetupSyncBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun setupViews() {
        smbInfo = requireArguments().get("smbInfo") as SmbInfoPresentation
        viewModel.setupSmbInfo(smbInfo)

        binding.txtIp.text = smbInfo.address
        binding.fragSetupSyncBtnContent.setOnClickListener {
            contentProvider.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
        }

        binding.txtDirectory.text = smbInfo.directory

        binding.chckCreateDir.setOnCheckedChangeListener { _, status ->
            viewModel.createDirectory(status)
        }
/*
        binding.fragSetupSyncBtn.setOnClickListener {
            if (allSetup())
                syncInPlace()
        }*/

        binding.fragSetupSaveBtn.setOnClickListener {
            if (allSetup()) {
                saveSyncUnit()
            }
        }

        binding.editSyncDirectory.doOnTextChanged { text, _, _, _ ->
            text?.let {
                viewModel.setSyncDirectoryName(text)
            }
        }

        binding.editSyncUnitName.doOnTextChanged { text, _, _, _ ->
            text?.let {
                viewModel.setSyncUnitName(text)
            }
        }

        binding.chckCreateDir.setOnCheckedChangeListener { _, b ->
            viewModel.checkOption(SyncOption.CREATE_SYNC_DIR, b)
        }

        binding.chckGroupDate.setOnCheckedChangeListener { _, b ->
            viewModel.checkOption(SyncOption.GROUP_BY_DATE, b)
        }

        binding.chckRemoveSynced.setOnCheckedChangeListener { _, b ->
            viewModel.checkOption(SyncOption.REMOVE_SYNCED, b)
        }

        binding.chckSyncNested.setOnCheckedChangeListener { _, b ->
            viewModel.checkOption(SyncOption.SYNC_NESTED, b)
        }

//        viewModel.createDirectory.observe(viewLifecycleOwner, createDirectoryObserver())
        viewModel.contentUri.observe(viewLifecycleOwner, contentUriObserver())
        viewModel.eventState.observe(viewLifecycleOwner, eventStateObserver())
        viewModel.optionsSet.observe(viewLifecycleOwner, optionsObserver())
    }

    private fun optionsObserver(): Observer<MutableSet<SyncOption>> {
        return Observer { set ->

            checkIfCreateDirRequired(set)

            set.map { option ->
                when (option) {
                    SyncOption.REMOVE_SYNCED -> {
                        binding.chckRemoveSynced.isChecked = true
                    }
                    SyncOption.GROUP_BY_DATE -> {
                        binding.chckGroupDate.isChecked = true
                    }
                    SyncOption.CREATE_SYNC_DIR -> {
                        binding.chckCreateDir.isChecked = true
                    }
                }
            }
        }
    }

    private fun checkIfCreateDirRequired(set: MutableSet<SyncOption>) {
        if (set.contains(SyncOption.CREATE_SYNC_DIR)) {
            binding.inputContainerSyncDir.visibility = View.VISIBLE
            binding.inputContainerSyncDir.requestFocus()
        } else {
            binding.inputContainerSyncDir.visibility = View.GONE
        }
    }

    private fun eventStateObserver(): Observer<in SetupSyncEvent> {
        return Observer { event ->
            when (event) {
                SetupSyncEvent.IDLE -> {}
                SetupSyncEvent.UNIT_SAVED -> {
                    findNavController().navigate(SetupSyncFragmentDirections.actionSetupSyncFragmentToHomeFragment())
                }
                SetupSyncEvent.SYNC_COMPLETED -> {

                }
            }
        }
    }

    private fun saveSyncUnit() {
        viewModel.saveSyncUnit()
    }

    private fun allSetup(): Boolean {
        val directorySetup = if (requireNotNull(viewModel.createDirectory.value)) {
            val dirName = binding.editSyncDirectory.text?.toString() ?: ""
            return if (dirName.isEmpty()) {
                binding.inputContainerSyncDir.error =
                    resources.getString(R.string.error_set_directory_name)
                false
            } else true
        } else {
            true
        }

        val contentDirectory = viewModel.isContentSet()

        return directorySetup && contentDirectory
    }

/*    private fun createDirectoryObserver(): Observer<in Boolean> {
        return Observer { create ->
            if (create) {
                binding.inputContainerSyncDir.visibility = View.VISIBLE
            } else {
                binding.inputContainerSyncDir.visibility = View.GONE
            }
        }
    }*/

    private fun contentUriObserver() = Observer<Uri?> { uri ->
        uri?.let {
            binding.txtContentDirectory.text = it.toString()
        }
    }

    private fun callFileManager() =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            viewModel.setupContentUri(uri)
            requireActivity().contentResolver.takePersistableUriPermission(
                uri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val res = DocumentFile.fromTreeUri(requireContext(), uri)
            val files = res?.listFiles()!!

            Log.i("aa", files.size.toString())
        }
}