package ru.teplicate.datasyncersmb.presentation.fragment.shared_files_fragment

import android.Manifest
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.lifecycle.Observer
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.databinding.FragmentSharedFilesBinding
import ru.teplicate.datasyncersmb.enums.DownloadEvent
import ru.teplicate.datasyncersmb.enums.RecyclerViewMode
import ru.teplicate.datasyncersmb.presentation.fragment.core.AbstractMasterDetailFragment
import ru.teplicate.datasyncersmb.presentation.fragment.shared_files_fragment.adapter.SharedFilesAdapter
import ru.teplicate.datasyncersmb.presentation.fragment.shared_files_fragment.view_model.SharedFilesViewModel
import ru.teplicate.datasyncersmb.smb.SmbProcessor
import ru.teplicate.datasyncersmb.util.PermissionChecker

class SharedFilesFragment : AbstractMasterDetailFragment(),
    SharedFilesAdapter.SharedFileClickListener {

    override val layoutId: Int
        get() = R.layout.fragment_shared_files

    private lateinit var binding: FragmentSharedFilesBinding

    private val viewModel: SharedFilesViewModel by viewModel()
    private val permissionChecker: PermissionChecker by inject()

    private lateinit var writePermissionLauncher: ActivityResultLauncher<String>


    override fun bindViews(layoutInflater: LayoutInflater): View {
        binding = FragmentSharedFilesBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        writePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    override fun setupViews() {
        binding.rvSharedFiles.adapter = SharedFilesAdapter(this)
        binding.btnCloseProgbar.setOnClickListener {
            binding.btnCloseProgbar.visibility = View.GONE
            binding.containerProgbarWrapper.visibility = View.GONE
        }
        viewModel.setupConnectionInfo(requireNotNull(requireArguments().getParcelable("smbInfo")))
        viewModel.sharedFiles.observe(viewLifecycleOwner, sharedFilesObserver())
        viewModel.sharedFilesRVMode.observe(viewLifecycleOwner, rvModeObserver())
        viewModel.listFiles()
    }

    private fun rvModeObserver(): Observer<RecyclerViewMode> =
        Observer { mode ->
            when (mode!!) {
                RecyclerViewMode.CLICK -> {
                    binding.btnDownloadSelected.visibility = View.GONE
                }
                RecyclerViewMode.SELECT -> {
                    binding.btnDownloadSelected.visibility = View.VISIBLE
                }
            }
            (binding.rvSharedFiles.adapter as SharedFilesAdapter).changeMode(mode)
        }

    private fun sharedFilesObserver(): Observer<List<RemoteFileView>> {
        return Observer { files ->
            if (files.isNotEmpty())
                (binding.rvSharedFiles.adapter as SharedFilesAdapter)
                    .updateFiles(files)
        }
    }

    override fun onItemClick(fileView: RemoteFileView) {
        when (viewModel.sharedFilesRVMode.value) {
            RecyclerViewMode.CLICK -> {
                if (fileView.isDirectory) {
                    viewModel.onDirectorySelected(fileView.name)
                }
            }
            RecyclerViewMode.SELECT -> {
                viewModel.fileSelected(fileView)
            }
        }
    }

    override fun onDownloadItemClick(fileView: RemoteFileView) {
        if (!hasWritePermission())
            return

        viewModel.totalDownloadFiles.observe(viewLifecycleOwner) { total ->
            total?.let {
                setupProgBar(it)
            }
        }

        viewModel.fileDownloaded.observe(viewLifecycleOwner) { file ->
            file?.let {
                updateProgress()
            }
        }

        viewModel.downloadState.observe(viewLifecycleOwner) { event ->
            if (event == DownloadEvent.DOWNLOAD_FINISHED) {
                binding.btnCloseProgbar.visibility = View.VISIBLE
                viewModel.totalDownloadFiles.removeObservers(viewLifecycleOwner)
                viewModel.fileDownloaded.removeObservers(viewLifecycleOwner)
                viewModel.downloadState.removeObservers(viewLifecycleOwner)
            }
        }

        binding.containerProgbarWrapper.visibility = View.VISIBLE

        val downloadHandler = object : SmbProcessor.DownloadEventHandler() {
            override fun processedWithException(fileView: RemoteFileView) {
                binding.containerProgbarWrapper.visibility = View.GONE
            }

            override fun successfulDownloadFile(fileView: RemoteFileView) {
                viewModel.fileDownloaded(fileView)
            }

            override fun onDownloadStart(totalElements: Int) {
                viewModel.downloadStart(totalElements)
            }

            override fun onDownloadComplete() {
                viewModel.downloadFinished()
            }
        }

        viewModel.downloadFile(fileView, downloadHandler)
    }

    override fun onLongPressFile(fileView: RemoteFileView) {
        viewModel.changeRVMode()
    }

    override fun onBackToRootClick() {
        viewModel.onBackToRoot()
    }

    override fun onHierarchyUpClick() {
        viewModel.onHierarchyUp()
    }

    private fun hasWritePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            when {
                permissionChecker.checkIfPermissionGranted(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) -> true
                else -> {
                    writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    false
                }
            }
        } else {
            true
        }
    }


    @MainThread
    private fun setupProgBar(totalElements: Int) {
        binding.progressBarInfo.containerRoot.visibility = View.VISIBLE
        with(binding.progressBarInfo.dialogSyncProgressBar) {
            this.progress = 0
            this.max = totalElements
        }

        binding.progressBarInfo.dialogProgressTotal.text =
            resources.getString(R.string.dialog_progress_total_txt, totalElements)
        binding.progressBarInfo.dialogProgressCurrent.text =
            resources.getString(R.string.dialog_progress_current_txt, 0)
    }

    @MainThread
    fun updateProgress() {
        val current = binding.progressBarInfo.dialogSyncProgressBar.progress + 1
        binding.progressBarInfo.dialogSyncProgressBar.setProgressCompat(
            current,
            true
        )

        binding.progressBarInfo.dialogProgressCurrent.text =
            resources.getString(R.string.dialog_progress_current_txt, current)
    }
}