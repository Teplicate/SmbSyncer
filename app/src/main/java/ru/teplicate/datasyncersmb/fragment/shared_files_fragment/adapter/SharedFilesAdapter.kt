package ru.teplicate.datasyncersmb.fragment.shared_files_fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.teplicate.datasyncersmb.data.RemoteFileView
import ru.teplicate.datasyncersmb.databinding.SharedFileItemBinding
import java.util.*

class SharedFilesAdapter :
    ListAdapter<RemoteFileView, SharedFilesAdapter.SharedFileViewHolder>(SharedFileDiffCallback()) {

    private val files: MutableList<RemoteFileView> = LinkedList()

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
        holder.bind(files[position])
    }

    fun updateFiles(files: List<RemoteFileView>) {
        this.files.clear()
        this.files.addAll(files)
        notifyItemRangeChanged(0, files.size)
    }

    class SharedFileViewHolder(private val binding: SharedFileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(remoteFileView: RemoteFileView) {

        }
    }
}