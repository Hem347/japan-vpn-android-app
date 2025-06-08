package com.example.japanvpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.japanvpn.JapanVpnApplication
import com.example.japanvpn.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed received")
            handleBootCompleted(context)
        }
    }

    private fun handleBootCompleted(context: Context) {
        scope.launch {
            try {
                val preferencesManager = (context.applicationContext as JapanVpnApplication).preferencesManager

                // Check if auto-start is enabled
                if (preferencesManager.autoConnect.first()) {
                    Log.d(TAG, "Auto-start is enabled, attempting to start VPN")
                    
                    // Get the last connected server
                    val lastServerId = preferencesManager.lastConnectedServer.first()
                    if (!lastServerId.isNullOrEmpty()) {
                        startVpnService(context, lastServerId)
                    } else {
                        Log.w(TAG, "No last connected server found")
                    }
                } else {
                    Log.d(TAG, "Auto-start is disabled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during boot handling: ${e.message}")
            }
        }
    }

    private fun startVpnService(context: Context, serverId: String) {
        try {
            // Create an intent to start the VPN service
            val serviceIntent = Intent(context, com.example.japanvpn.JapanVpnService::class.java).apply {
                action = com.example.japanvpn.JapanVpnService.ACTION_CONNECT
                putExtra(com.example.japanvpn.JapanVpnService.EXTRA_SERVER, serverId)
            }

            // Start the service
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.d(TAG, "VPN service start requested for server: $serverId")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN service: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
