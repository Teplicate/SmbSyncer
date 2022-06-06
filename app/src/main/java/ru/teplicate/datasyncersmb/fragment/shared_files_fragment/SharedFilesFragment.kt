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

class SharedFilesFragment : AbstractMasterDetailFragment() {

    override val layoutId: Int
        get() = R.layout.fragment_shared_files

    private lateinit var binding: FragmentSharedFilesBinding

    private val viewModel: SharedFilesViewModel by viewModel()
    private lateinit var connectionInfo: SmbInfo

    override fun bindViews(layoutInflater: LayoutInflater): View {
        binding = FragmentSharedFilesBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun setupViews() {
        connectionInfo = requireNotNull(requireArguments().getParcelable("smbInfo"))

        viewModel.sharedFiles.observe(viewLifecycleOwner, sharedFilesObserver())

        viewModel.listFiles(connectionInfo)
    }

    private fun sharedFilesObserver(): Observer<List<RemoteFileView>> {
        return Observer { files ->
            if (files.isNotEmpty())
                (binding.rvSharedFiles.adapter as SharedFilesAdapter)
                    .updateFiles(files)
        }
    }
}