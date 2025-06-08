package com.example.japanvpn

import android.app.Application
import android.util.Log
import com.example.japanvpn.repository.ServerRepository
import com.example.japanvpn.utils.PreferencesManager
import com.example.japanvpn.utils.VpnUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JapanVpnApplication : Application() {

    // Application-wide dependencies
    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var serverRepository: ServerRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize dependencies
        initializeDependencies()

        // Create notification channel
        VpnUtils.createNotificationChannel(this)

        // Initialize preferences and check for auto-connect
        applicationScope.launch {
            preferencesManager.autoConnect.collect { shouldAutoConnect ->
                if (shouldAutoConnect) {
                    handleAutoConnect()
                }
            }
        }
    }

    private fun initializeDependencies() {
        try {
            preferencesManager = PreferencesManager(applicationContext)
            serverRepository = ServerRepository()

            Log.d(TAG, "Dependencies initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing dependencies: ${e.message}")
        }
    }

    private fun handleAutoConnect() {
        applicationScope.launch {
            try {
                // Get last connected server
                preferencesManager.lastConnectedServer.collect { serverId ->
                    if (!serverId.isNullOrEmpty()) {
                        // Auto-connect logic would go here
                        Log.d(TAG, "Auto-connect triggered for server: $serverId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in auto-connect: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "JapanVpnApplication"
        
        @Volatile
        private var instance: JapanVpnApplication? = null

        fun getInstance(): JapanVpnApplication {
            return instance ?: synchronized(this) {
                instance ?: throw IllegalStateException("Application not initialized")
            }
        }
    }
}
