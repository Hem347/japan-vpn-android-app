package com.example.japanvpn.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.VpnService
import android.os.Build
import java.net.InetAddress
import java.net.NetworkInterface

object VpnUtils {
    
    fun isVpnActive(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
        } else {
            val networks = connectivityManager.allNetworks
            networks.any { network ->
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ?: false
            }
        }
    }

    fun getVpnInterface(): NetworkInterface? {
        return NetworkInterface.getNetworkInterfaces()?.toList()?.find { networkInterface ->
            networkInterface.displayName.startsWith("tun") || networkInterface.displayName.startsWith("ppp")
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    fun prepareVpnService(context: Context): VpnService.Builder {
        return VpnService.Builder().apply {
            // Set MTU
            setMtu(1500)

            // Add default route
            addAddress("10.0.0.2", 32)
            addRoute("0.0.0.0", 0)

            // Add DNS servers
            addDnsServer("8.8.8.8")
            addDnsServer("8.8.4.4")

            // Allow bypass for local networks
            allowBypass()

            // Set session name
            setSession("JapanVPN")
        }
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        }
    }

    fun registerNetworkCallback(context: Context, callback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        }
    }

    fun unregisterNetworkCallback(context: Context, callback: ConnectivityManager.NetworkCallback) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class NetworkCallbackImpl(
        private val onAvailable: (Network) -> Unit = {},
        private val onLost: (Network) -> Unit = {},
        private val onCapabilitiesChanged: (Network, NetworkCapabilities) -> Unit = { _, _ -> }
    ) : ConnectivityManager.NetworkCallback() {
        
        override fun onAvailable(network: Network) {
            onAvailable(network)
        }

        override fun onLost(network: Network) {
            onLost(network)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            onCapabilitiesChanged(network, networkCapabilities)
        }
    }
}
