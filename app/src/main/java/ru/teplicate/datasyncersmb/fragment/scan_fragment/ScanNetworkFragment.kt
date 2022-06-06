package ru.teplicate.datasyncersmb.fragment.scan_fragment

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.teplicate.datasyncersmb.R
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.databinding.FragmentScanNetworkBinding
import ru.teplicate.datasyncersmb.enums.ConnectionState
import ru.teplicate.datasyncersmb.fragment.core.AbstractMasterDetailFragment
import ru.teplicate.datasyncersmb.fragment.scan_fragment.view_model.ScanNetworkViewModel
import ru.teplicate.datasyncersmb.util.PermissionChecker

class ScanNetworkFragment : AbstractMasterDetailFragment(), AddressDataListener {
    private lateinit var binding: FragmentScanNetworkBinding
    private val viewModel: ScanNetworkViewModel by viewModel()

    private val permissionChecker: PermissionChecker by inject()

    private lateinit var wifiPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var wifiSetupLauncher: ActivityResultLauncher<Intent>
    private lateinit var locationAccessLauncher: ActivityResultLauncher<String>

    private val wifiManager by lazy { requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager }

    private var testConnectionCallback: ((ConnectionState) -> Unit)? = null

    override val layoutId: Int
        get() = R.layout.fragment_scan_network

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wifiPermissionLauncher = wifiStatePermissionLauncher()
        wifiSetupLauncher = requestToTurnWifiOn()
        locationAccessLauncher = requestLocation()
    }

    override fun bindViews(layoutInflater: LayoutInflater): View {
        binding = FragmentScanNetworkBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun setupViews() {
        binding.scanButton.setOnClickListener {
            binding.scanButton.isEnabled = false
            setupSubnetAddress()
        }

        binding.turnOnWifi.setOnClickListener {
            wifiSetupLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
        }


        binding.addressList.adapter = AddressAdapter(this)

        viewModel.subnet.observe(viewLifecycleOwner, getSubnetObserver())
//        viewModel.bssid.observe(viewLifecycleOwner, getBssidObserver())
        viewModel.wifiIsOn.observe(viewLifecycleOwner, getWiFiStateObserver())
        viewModel.testConnect.observe(viewLifecycleOwner, getTestConnectObserver())
        checkWifiState()
    }

    private fun getTestConnectObserver(): Observer<in ConnectionState> {
        return Observer { status ->

            when (status) {
                ConnectionState.IDLE -> return@Observer
                ConnectionState.INVALID_SHARE_NAME -> {
                    binding.editSharedDirectory.error = "Invalid share name"
                }
                else -> testConnectionCallback?.invoke(status)
            }
        }
    }

    private fun isSharedFolderSupplied(): Boolean {
        val folderName = binding.editSharedDirectory.editableText.toString()

        return folderName.trim().isNotEmpty()
    }

    private fun submitAddress(addressData: AddressData) {
        requireActivity().runOnUiThread {
            (binding.addressList.adapter as AddressAdapter).supplyNewItem(addressData)
        }
    }

    private fun finishScanCallback() {
        requireActivity().runOnUiThread {
            binding.scanButton.isEnabled = true
        }
    }

    private fun getWiFiStateObserver(): Observer<in Boolean> {
        return Observer { isOn ->
            if (isOn) {
                binding.scanButton.isEnabled = true
                binding.scanButton.visibility = View.VISIBLE
                binding.turnOnWifi.visibility = View.GONE
                binding.turnOnWifi.isEnabled = false
            } else {
                binding.scanButton.isEnabled = false
                binding.scanButton.visibility = View.GONE
                binding.turnOnWifi.visibility = View.VISIBLE
                binding.turnOnWifi.isEnabled = true
            }
        }
    }

    private fun getSubnetObserver(): Observer<in String?> {
        return Observer { localIp ->
            localIp?.let {
                binding.subnet.text = it
                binding.scanButton.isEnabled = false //make scan animation
                viewModel.scanNetwork(this::submitAddress, this::finishScanCallback, it)
            }
        }
    }

    private fun setupSubnetAddress() {
        when {
            !permissionChecker.checkIfPermissionGranted(
                requireContext(),
                Manifest.permission.ACCESS_WIFI_STATE
            ) -> wifiPermissionLauncher.launch(Manifest.permission.ACCESS_WIFI_STATE)
            !permissionChecker.checkIfPermissionGranted(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> locationAccessLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            else -> {
                checkWifiState()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setupSubnetNew()
                } else {
                    setupSubnetOld()
                }
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.S)
    private fun setupSubnetNew() {
        val connectivityManager = requireContext().getSystemService(ConnectivityManager::class.java)
        val request = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val props = connectivityManager.getLinkProperties(network)
                val serverAddr = props?.dhcpServerAddress
                viewModel.setupSubnetAddress(requireNotNull(serverAddr).hostAddress!!)
            }
        }

        connectivityManager.requestNetwork(request, networkCallback)
    }

    
    private fun setupSubnetOld() {
        //use ssid as prim key
        viewModel.setupSubnetAddress(wifiManager.dhcpInfo.gateway)
    }


    private fun isWifiOn(connectivityManager: ConnectivityManager): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork != null) {
            val currentNetworkCaps = connectivityManager.getNetworkCapabilities(activeNetwork)
            return currentNetworkCaps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }

        return false
    }

    private fun requestToTurnWifiOn(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.setWifiState(true)
            }
        }
    }

    private fun requestLocation(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            lockUi(!granted)
        }
    }

    private fun lockUi(lock: Boolean) {
        if (lock) {
            binding.scanButton.isEnabled = false
            binding.turnOnWifi.isEnabled = false
        } else {
            binding.scanButton.isEnabled = true
            binding.turnOnWifi.isEnabled = true
        }
    }

    private fun wifiStatePermissionLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { success ->
            if (success) {
                checkWifiState()
            }
        }
    }

    private fun checkWifiState() {
        val connectivityManager = requireContext().getSystemService(ConnectivityManager::class.java)
        //check if has network
        if (!isWifiOn(connectivityManager)) {
            viewModel.setWifiState(false)
        } else {
            viewModel.setWifiState(true)
        }
    }

    override fun testConnectionItemClick(
        addressData: AddressData,
        login: String,
        password: String,
        connectionCallback: (ConnectionState) -> Unit
    ) {
        if (!isSharedFolderSupplied()) {
            binding.inputContainerSharedDir.error = "Please supply shared directory name"
            return
        }


        testConnectionCallback = connectionCallback
        val smbInfo = SmbInfo(
            login = login,
            password = password,
            address = addressData.address,
            directory = binding.editSharedDirectory.editableText.toString()
        )
        viewModel.setupConnectionData(
            smbInfo
        )
        viewModel.testSmb(
            smbInfo
        )
    }

    override fun proceedWithAddress() {
        val args = viewModel.makeArgs()

        findNavController().navigate(
            ScanNetworkFragmentDirections.actionAddressFragmentToSetupSyncFragment(
                args
            )
        )
    }
}

interface AddressDataListener {

    fun testConnectionItemClick(
        addressData: AddressData,
        login: String,
        password: String,
        connectionCallback: (ConnectionState) -> Unit
    )

    fun proceedWithAddress()
}



