package ru.teplicate.datasyncersmb.fragment.home_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.teplicate.datasyncersmb.database.entity.SynchronizationUnit
import ru.teplicate.datasyncersmb.databinding.SyncUnitItemBinding

class SyncUnitAdapter(
    private val syncItemClickListener: SyncItemClickListener
) :
    ListAdapter<SynchronizationUnit, SyncUnitAdapter.SyncUnitViewHolder>(SyncUnitDiffCallback()) {

    private val unitList: MutableList<SynchronizationUnit> = ArrayList()

    init {
        submitList(unitList)
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
        diffList.removeAll(unitList)
        unitList.addAll(diffList)
        val positionStart = if (unitList.isEmpty()) 0 else unitList.size - 1
        notifyItemRangeInserted(positionStart, diffList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SyncUnitViewHolder {
        val binding =
            SyncUnitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SyncUnitViewHolder(binding, syncItemClickListener)
    }

    override fun onBindViewHolder(holder: SyncUnitViewHolder, position: Int) {
        val element = unitList[position]
        holder.bind(element)
    }

    fun removeItem(position: Int) {
        unitList.removeAt(position)
        notifyItemRemoved(position)
    }

    class SyncUnitViewHolder(
        private val binding: SyncUnitItemBinding,
        private val listener: SyncItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var synchronizationUnit: SynchronizationUnit
        private var selected: Boolean = false

        fun bind(unit: SynchronizationUnit) {
            this.synchronizationUnit = unit
            binding.txtPosition.text = (adapterPosition + 1).toString()
            binding.txtTitle.text = unit.name ?: unit.smbConnection.address

            binding.txtLocalIp.text = unit.smbConnection.address
            binding.txtContentDirectory.text = unit.smbConnection.sharedDirectory
            binding.txtLastSyncDate.text =
                unit.synchronizationInfo.lastSyncDate?.toString() ?: ""

            binding.btnDeleteUnit.setOnClickListener {
                listener.onDeleteClickListener(unit, adapterPosition)
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

        fun onSelect(unit: SynchronizationUnit)

        fun onUnselect()

        fun onEditClickListener(unit: SynchronizationUnit)

        fun onDeleteClickListener(unit: SynchronizationUnit, position: Int)
    }
}