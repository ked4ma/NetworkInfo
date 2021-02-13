package com.github.ked4ama.android.networkinfo.app.vm

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ked4ama.android.networkinfo.app.data.NetworkInfo
import com.github.ked4ama.android.networkinfo.util.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    private val telephonyManager: TelephonyManager,
) : ViewModel() {

    private val _networkState = MutableStateFlow<NetworkInfo>(NetworkInfo.NONE)
    val networkState: StateFlow<NetworkInfo> = _networkState

    private fun updateNetworkState(info: NetworkInfo) {
        viewModelScope.launch {
            _networkState.emit(info)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermission(context: Activity): Boolean {
        if (PERMISSIONS.any {
                context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
        ) {
            context.requestPermissions(PERMISSIONS, Const.RequestCode.NETWORK_REQUEST_CODE)
            return false
        }
        return true
    }

    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            when {
                isWifi() -> getWifiNetworkInfo()
                isCellular(connectivityManager.getNetworkCapabilities(network)) ->
                    getMobileNetworkInfo()
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            updateNetworkState(NetworkInfo.NONE)
        }
    }

    fun init() {
        // register callback for network change
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(connectivityCallback)
        } else {
            // TODO not verified cuz my physical devices are later than or equals to api29 at all.
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
//            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
//            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                .build()
            connectivityManager.registerNetworkCallback(request, connectivityCallback)
        }
    }

    private fun isWifi() = wifiManager.connectionInfo.networkId != -1

    private fun isCellular(capabilities: NetworkCapabilities?) =
        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false

    private fun getWifiNetworkInfo() {
        val info = if (isWifi()) {
            val ssid = wifiManager.connectionInfo.ssid
            val ip: Int = wifiManager.connectionInfo.ipAddress
            NetworkInfo.Wifi(ssid, ip)
        } else {
            NetworkInfo.NONE
        }
        updateNetworkState(info)
    }

    private fun getMobileNetworkInfo() {
        println("-------------")
        println("here")
        println(telephonyManager.networkOperatorName)
        println("-------------")
        val operatorName = telephonyManager.networkOperatorName
        if (operatorName.isNullOrBlank()) {
            updateNetworkState(NetworkInfo.NONE)
            return
        }

        val ip = NetworkInterface.getNetworkInterfaces().toList().flatMap { intf ->
            intf.inetAddresses.toList().mapNotNull { address ->
                if (address.isLoopbackAddress || address !is Inet4Address) return@mapNotNull null
                var addr = 0
                for (b in address.address.reversed()) {
                    addr = addr shl 8 or (b.toInt() and 0xFF)
                }
                addr
            }
        }.firstOrNull()

        val info = if (ip == null) {
            NetworkInfo.NONE
        } else {
            NetworkInfo.Mobile(operatorName, ip)
        }
        updateNetworkState(info)
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(connectivityCallback)
    }

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
        )
    }
}