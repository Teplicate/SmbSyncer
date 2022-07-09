package ru.teplicate.datasyncersmb.presentation.fragment.scan_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.databinding.AddressCardBinding
import ru.teplicate.datasyncersmb.enums.ConnectionState

class AddressAdapter(private val addressDataListener: AddressDataListener) :
    ListAdapter<AddressData, AddressAdapter.AddressViewHolder>(AddressDiffCallback()) {

    private val addressSet = HashSet<AddressData>();

    private class AddressDiffCallback : DiffUtil.ItemCallback<AddressData>() {

        override fun areItemsTheSame(oldItem: AddressData, newItem: AddressData): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: AddressData, newItem: AddressData): Boolean {
            return oldItem == newItem
        }
    }

    fun supplyNewItem(address: AddressData) {
        if (addressSet.add(address)) {
            submitList(addressSet.toList())
            notifyItemInserted(addressSet.size - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = AddressCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding, addressDataListener)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class AddressViewHolder(
        private val bind: AddressCardBinding,
        private val listener: AddressDataListener
    ) :
        RecyclerView.ViewHolder(bind.root) {

        fun bind(address: AddressData) {
            val resources = bind.root.resources
            this.bind.textAddressData.text =
                resources.getString(R.string.device_ip, address.address)

            bind.root.setOnClickListener {
                bind.cardContent.visibility =
                    if (bind.cardContent.visibility == View.GONE) View.VISIBLE else {
                        setupConnectionStatus(ConnectionState.IDLE)
                        View.GONE
                    }

                bind.buttonTestConnect.setOnClickListener {
                    val login = bind.editLogin.editText?.editableText?.toString() ?: ""
                    val password = bind.editPassword.editText?.editableText?.toString() ?: ""
                    listener.testConnectionItemClick(
                        address,
                        login,
                        password,
                        this::connectionCallback
                    )
//                    setupConnectionStatus(connected)
                }

                bind.buttonSetupDevice.setOnClickListener {
                    listener.proceedWithAddress()
                }
            }
        }

        private fun connectionCallback(status: ConnectionState) {
            setupConnectionStatus(status)
        }

        private fun setupConnectionStatus(status: ConnectionState) {
            when (status) {
                ConnectionState.CONNECTION_OK -> {
                    bind.buttonSetupDevice.visibility = View.VISIBLE
                }
                ConnectionState.AUTH_REQUIRED -> {
                    bind.editLogin.error = "Provide login and password"
                }
                ConnectionState.IDLE -> {
                    bind.buttonSetupDevice.visibility = View.GONE
                }
                else -> throw IllegalArgumentException("Unexpected status ${status.name}")
            }
        }
    }
}

data class AddressData(
    val address: String
)