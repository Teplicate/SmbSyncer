package ru.teplicate.datasyncersmb.network_scanner

import android.util.Log
import ru.teplicate.datasyncersmb.fragment.scan_fragment.AddressData
import java.lang.Exception
import java.net.InetAddress

class NetworkScanner {
    private val tag = NetworkScanner::class.java.name
    private val timeout = 100


    fun scanNetwork(
        callback: (AddressData) -> Unit,
        finishCallback: () -> Unit,
        localIp: String
    ) {
        val lastNum = localIp.split(".").last().toInt()
        val subnet = localIp.removeSuffix(".$lastNum")
        for (i in 0..255) {

            if (i == lastNum)
                continue

            val host = "$subnet.$i"
            val addressData = try {
                val k = InetAddress.getByName(host)
                val r = k.isReachable(timeout)

                if (r) {
                    AddressData(host)
                } else null
            } catch (e: Exception) {
                Log.i(tag, e.toString())
                null
            }

            addressData?.let {
                callback(it)
            }
        }

        finishCallback()
    }
}