package ru.teplicate.datasyncersmb.fragment.shared_files_fragment.adapter

import android.content.res.Resources
import android.database.Observable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.databinding.SharedFileItemBinding
import ru.teplicate.datasyncersmb.enums.FileType
import ru.teplicate.datasyncersmb.enums.FileType.*
import ru.teplicate.datasyncersmb.enums.RecyclerViewMode
import java.util.*
import kotlin.collections.HashSet

const val UP_DIRECTORY = ".."
const val ROOT_DIRECTORY = "/"

const val BACK_TO_ROOT_TYPE = 0
const val UP_DIRECTORY_TYPE = 1


class SharedFilesAdapter(private val sharedFileClickListener: SharedFileClickListener) :
    ListAdapter<RemoteFileView, SharedFilesAdapter.SharedFileViewHolder>(SharedFileDiffCallback()) {


    private var clickMode: RecyclerViewMode = RecyclerViewMode.CLICK
    private val visibleHolders: MutableSet<SharedFileViewHolder> = HashSet()

    private val files: MutableList<RemoteFileView> = LinkedList()
    private val typeToColorMap: Map<FileType, Int> = mapOf(
        PDF to R.color.momo_200,
        TXT to R.color.tsuki_200,
        IMAGE to R.color.light_orange,
        VIDEO to R.color.solid_blue,
        TABLE to R.color.light_green,
        OTHER to R.color.white,
        DIRECTORY to R.color.purple_05
    )
    private val cachedColors: MutableMap<FileType, Int> = HashMap()

    init {
        submitList(files)
    }

    private class SharedFileDiffCallback : DiffUtil.ItemCallback<RemoteFileView>() {
        override fun areItemsTheSame(oldItem: RemoteFileView, newItem: RemoteFileView): Boolean {
            return oldItem.fileId == newItem.fileId
        }

        override fun areContentsTheSame(oldItem: RemoteFileView, newItem: RemoteFileView): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedFileViewHolder {
        val binding = LayoutInflater.from(parent.context).run {
            SharedFileItemBinding.inflate(this, parent, false)
        }

        return SharedFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SharedFileViewHolder, position: Int) {
        holder.bind(files[position], position)
    }

    fun updateFiles(files: List<RemoteFileView>) {
        val total = this.files.size
        this.files.clear()

        if (total > 0)
            notifyItemRangeRemoved(0, total)

        this.files.addAll(files.sortedBy { it.createdAt })
        val (upDir, rootDir) = getControlFiles()
        this.files.add(0, rootDir)
        this.files.add(1, upDir)
        notifyItemRangeChanged(0, this.files.size)
    }

    private fun getControlFiles() =
        listOf(
            RemoteFileView(
                fileId = -1,
                name = UP_DIRECTORY,
                path = "",
                size = 0,
                isDirectory = true,
                createdAt = -10
            ),
            RemoteFileView(
                fileId = -10,
                name = ROOT_DIRECTORY,
                path = "",
                size = 0,
                isDirectory = true,
                createdAt = -10
            )
        )

    private fun getColorFromCache(fileType: FileType, resources: Resources): Int {
        return cachedColors[fileType] ?: resources.getColor(typeToColorMap.getValue(fileType), null)
            .also { cachedColors[fileType] = it }
    }

    override fun onViewDetachedFromWindow(holder: SharedFileViewHolder) {
        super.onViewDetachedFromWindow(holder)
        visibleHolders.remove(holder)
    }

    override fun onViewAttachedToWindow(holder: SharedFileViewHolder) {
        super.onViewAttachedToWindow(holder)
        visibleHolders.add(holder)
    }

    fun changeMode(mode: RecyclerViewMode) {
        clickMode = mode

        if (mode == RecyclerViewMode.CLICK)
            files.forEach { e -> e.notSelected() }

        visibleHolders.forEach { it.switchMode() }
    }

    inner class SharedFileViewHolder(
        private val binding: SharedFileItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private val resources by lazy { binding.root.resources }

        fun bind(remoteFileView: RemoteFileView, position: Int) {
            when (position) {
                BACK_TO_ROOT_TYPE -> {
                    setupControlView(remoteFileView)
                    binding.containerClickable.setOnClickListener { sharedFileClickListener.onBackToRootClick() }
                }
                UP_DIRECTORY_TYPE -> {
                    setupControlView(remoteFileView)
                    binding.containerClickable.setOnClickListener { sharedFileClickListener.onHierarchyUpClick() }
                }
                else -> {
                    setupRegularView(remoteFileView)
                }
            }

        }

        private fun setupControlView(remoteFileView: RemoteFileView) {
            binding.containerOptions.visibility = View.GONE
            binding.txtFilename.text = remoteFileView.name
        }

        private fun setupRegularView(remoteFileView: RemoteFileView) {
            binding.containerOptions.visibility = View.VISIBLE
            binding.txtFilename.text = remoteFileView.name
            binding.chckSelected.isChecked = remoteFileView.isFileSelected()
            binding.btnDownloadFile.setOnClickListener {
                sharedFileClickListener.onDownloadItemClick(remoteFileView)
            }

            setupByType(remoteFileView)

            binding.containerClickable.setOnClickListener {
                sharedFileClickListener.onItemClick(remoteFileView)
                fileClick(remoteFileView)
            }

            binding.containerClickable.setOnLongClickListener {
                if (clickMode == RecyclerViewMode.CLICK) {
                    sharedFileClickListener.onLongPressFile(remoteFileView)
                    binding.containerClickable.callOnClick()
                    true
                } else false
            }

            switchMode()
        }

        private fun fileClick(remoteFileView: RemoteFileView) {
            when (clickMode) {
                RecyclerViewMode.CLICK -> {}
                RecyclerViewMode.SELECT -> {
                    remoteFileView.switchSelect()
                    binding.chckSelected.isChecked = remoteFileView.isFileSelected()
                }
            }
        }

        private fun setupByType(remoteFileView: RemoteFileView) {
            val (type, color) = if (!remoteFileView.isDirectory) {
                binding.txtFilesize.text = remoteFileView.size.toString()
                setupFileType(remoteFileView)
            } else {
                binding.txtFilesize.text = ""
                DIRECTORY to getColorFromCache(DIRECTORY, resources)
            }

            binding.txtFileType.text = type.viewVal
            binding.txtFileType.setTextColor(color)
        }

        private fun setupFileType(remoteFileView: RemoteFileView): Pair<FileType, Int> {
            val ext = remoteFileView.name.split(".").last().lowercase()
            val resources = resources
            val type = when {
                PDF.strVals.contains(ext) -> PDF
                TXT.strVals.contains(ext) -> TXT
                TABLE.strVals.contains(ext) -> TABLE
                IMAGE.strVals.contains(ext) -> IMAGE
                VIDEO.strVals.contains(ext) -> VIDEO
                else -> OTHER
            }

            val color = getColorFromCache(type, resources)

            return type to color
        }

        fun switchMode() {
            when (clickMode) {
                RecyclerViewMode.SELECT -> {
                    binding.btnDownloadFile.visibility = View.GONE
                    binding.chckSelected.visibility = View.VISIBLE
                }
                RecyclerViewMode.CLICK -> {
                    binding.btnDownloadFile.visibility = View.VISIBLE
                    binding.chckSelected.visibility = View.GONE
                }
            }
        }
    }

    interface SharedFileClickListener {

        fun onItemClick(fileView: RemoteFileView)

        fun onDownloadItemClick(fileView: RemoteFileView)

        fun onLongPressFile(fileView: RemoteFileView)

        fun onBackToRootClick()

        fun onHierarchyUpClick()
    }
}