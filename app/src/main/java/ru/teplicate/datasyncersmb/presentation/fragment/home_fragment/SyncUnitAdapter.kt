package ru.teplicate.datasyncersmb.presentation.fragment.home_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.teplicate.core.domain.SynchronizationUnit
import ru.teplicate.datasyncersmb.databinding.SyncUnitItemBinding

class SyncUnitAdapter(
    private val syncItemClickListener: SyncItemClickListener
) :
    ListAdapter<SynchronizationUnit, SyncUnitAdapter.SyncUnitViewHolder>(SyncUnitDiffCallback()) {

    private val unitEntityList: MutableList<SynchronizationUnit> = ArrayList()

    init {
        submitList(unitEntityList)
    }

    private class SyncUnitDiffCallback : DiffUtil.ItemCallback<SynchronizationUnit>() {
        override fun areItemsTheSame(
            oldItem: SynchronizationUnit,
            newItem: SynchronizationUnit
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SynchronizationUnit,
            newItem: SynchronizationUnit
        ): Boolean {
            return oldItem == newItem
        }
    }

    fun supplySyncUnits(list: List<SynchronizationUnit>) {
        val diffList = list.toMutableList()
        diffList.removeAll(unitEntityList)
        unitEntityList.addAll(diffList)
        val positionStart = if (unitEntityList.isEmpty()) 0 else unitEntityList.size - 1
        notifyItemRangeInserted(positionStart, diffList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyncUnitViewHolder {
        val binding =
            SyncUnitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SyncUnitViewHolder(binding, syncItemClickListener)
    }

    override fun onBindViewHolder(holder: SyncUnitViewHolder, position: Int) {
        val element = unitEntityList[position]
        holder.bind(element)
    }

    fun removeItem(position: Int) {
        unitEntityList.removeAt(position)
        notifyItemRemoved(position)
    }

    class SyncUnitViewHolder(
        private val binding: SyncUnitItemBinding,
        private val listener: SyncItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var synchronizationUnit: SynchronizationUnit
        private var selected: Boolean = false

        fun bind(unitEntity: SynchronizationUnit) {
            this.synchronizationUnit = unitEntity
            binding.txtPosition.text = (adapterPosition + 1).toString()
            binding.txtTitle.text = unitEntity.name ?: unitEntity.smbConnection.address

            binding.txtLocalIp.text = unitEntity.smbConnection.address
            binding.txtContentDirectory.text = unitEntity.smbConnection.directory
           /* binding.txtLastSyncDate.text =
                unitEntity.synchronizationInfo?.lastSyncDate?.toString() ?: ""*/

            binding.btnDeleteUnit.setOnClickListener {
                listener.onDeleteClickListener(unitEntity, adapterPosition)
            }

            binding.containerHeader.setOnClickListener {
                selected = !selected

                if (selected) {
                    listener.onSelect(synchronizationUnit)
                    binding.expandableContent.visibility = View.VISIBLE
                    binding.btnDeleteUnit.visibility = View.VISIBLE
                    binding.btnEditUnit.visibility = View.VISIBLE
                } else {
                    listener.onUnselect()
                    binding.expandableContent.visibility = View.GONE
                    binding.btnDeleteUnit.visibility = View.GONE
                    binding.btnEditUnit.visibility = View.GONE
                }
            }
        }
    }

    interface SyncItemClickListener {

        fun onSelect(unitEntity: SynchronizationUnit)

        fun onUnselect()

        fun onEditClickListener(unitEntity: SynchronizationUnit)

        fun onDeleteClickListener(unitEntity: SynchronizationUnit, position: Int)
    }
}