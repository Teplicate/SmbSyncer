package ru.teplicate.datasyncersmb.fragment.shared_files_fragment

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.databinding.FragmentSharedFilesBinding
import ru.teplicate.datasyncersmb.databinding.SharedFileItemBinding
import ru.teplicate.datasyncersmb.fragment.core.AbstractMasterDetailFragment
import ru.teplicate.datasyncersmb.fragment.shared_files_fragment.adapter.SharedFilesAdapter
import ru.teplicate.datasyncersmb.fragment.shared_files_fragment.view_model.SharedFilesViewModel

class SharedFilesFragment : AbstractMasterDetailFragment(),
    SharedFilesAdapter.SharedFileClickListener {

    override val layoutId: Int
        get() = R.layout.fragment_shared_files

    private lateinit var binding: FragmentSharedFilesBinding

    private val viewModel: SharedFilesViewModel by viewModel()

    override fun bindViews(layoutInflater: LayoutInflater): View {
        binding = FragmentSharedFilesBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun setupViews() {
        binding.rvSharedFiles.adapter = SharedFilesAdapter(this)

        viewModel.setupConnectionInfo(requireNotNull(requireArguments().getParcelable("smbInfo")))
        viewModel.sharedFiles.observe(viewLifecycleOwner, sharedFilesObserver())
        viewModel.listFiles()
    }

    private fun sharedFilesObserver(): Observer<List<RemoteFileView>> {
        return Observer { files ->
            if (files.isNotEmpty())
                (binding.rvSharedFiles.adapter as SharedFilesAdapter)
                    .updateFiles(files)
        }
    }

    override fun onItemClick(fileView: RemoteFileView) {
        if (fileView.isDirectory) {
            viewModel.onDirectorySelected(fileView.name)
        }
    }

    override fun onDownloadItemClick(fileView: RemoteFileView) {

    }

    override fun onLongPressFile(fileView: RemoteFileView) {
        TODO("Not yet implemented")
    }

    override fun onBackToRootClick() {
        viewModel.onBackToRoot()
    }

    override fun onHierarchyUpClick() {
        viewModel.onHierarchyUp()
    }
}