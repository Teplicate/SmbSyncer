package ru.teplicate.datasyncersmb.fragment.scan_fragment.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hierynomus.mssmb2.SMBApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.teplicate.datasyncersmb.data.SmbInfo
import ru.teplicate.datasyncersmb.enums.ConnectionState
import ru.teplicate.datasyncersmb.fragment.scan_fragment.AddressData
import ru.teplicate.datasyncersmb.network_scanner.NetworkScanner
import ru.teplicate.datasyncersmb.smb.SmbProcessor

class ScanNetworkViewModel(
    private val networkScanner: NetworkScanner,
    private val smbProcessor: SmbProcessor
) : ViewModel() {
    private val tag = this::class.java.name

    private val _wifiIsOn = MutableLiveData(false)
    val wifiIsOn: LiveData<Boolean>
        get() = _wifiIsOn

    private val _testConnect = MutableLiveData(ConnectionState.IDLE)
    val testConnect: LiveData<ConnectionState>
        get() = _testConnect

    private var guest: Boolean = false

    private val _subnet = MutableLiveData<String?>(null)
    val subnet: LiveData<String?>
        get() = _subnet

    /*   private val _bssid = MutableLiveData<String?>(null)
       val bssid: LiveData<String?>
           get() = _bssid*/

    private var smbInfo: SmbInfo? = null

    fun setupSubnetAddress(addr: Any) {
        when (addr) {
            is Int -> {
                setupAddressFromInt(addr)
            }
            is String -> {
                setupAddressFromStr(addr)
            }
            else -> throw IllegalArgumentException("Unexpected address type")
        }
    }

    private fun setupAddressFromStr(address: String) {
        _subnet.postValue(address)
        Log.i(tag, address)
    }

    private fun setupAddressFromInt(address: Int) {
        val addr = getAddress(address)

        _subnet.postValue(addr)
        Log.i(tag, addr)
    }

    private fun getAddress(address: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            address and 0xff,
            address shr 8 and 0xff,
            address shr 16 and 0xff,
            address shl 32 and 0xff
        )
    }

    private fun getAddress(address: String): String {
        return address
    }

    fun setWifiState(isOn: Boolean) {
        if (_wifiIsOn.value == isOn)
            return

        _wifiIsOn.value = isOn
    }

    /*   fun setupBssid(bssid: String?) {
           bssid?.let {
               _bssid.postValue(bssid)
           }
       }*/

    fun scanNetwork(callback: (AddressData) -> Unit, finishCallback: () -> Unit, localIp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkScanner.scanNetwork(callback, finishCallback, localIp)
        }
    }

    fun testSmb(smbInfo: SmbInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val isConnected = try {
                smbProcessor.testConnection(smbInfo)
            } catch (smbApiException: SMBApiException) {
                smbApiException.printStackTrace()

                if (smbApiException.message?.contains("Error closing connection to") == true)
                    ConnectionState.CONNECTION_OK
                else smbProcessor.processException(smbApiException)
            }
            _testConnect.postValue(isConnected)
        }
    }

    fun setupConnectionData(smbInfo: SmbInfo) {
        this.smbInfo = smbInfo
    }

    fun makeArgs(): SmbInfo = requireNotNull(this.smbInfo)

    fun setupGuest(guest: Boolean) {
        this.guest = guest
    }
}