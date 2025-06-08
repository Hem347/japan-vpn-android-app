package com.example.japanvpn

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.japanvpn.model.ConnectionStatistics
import com.example.japanvpn.model.Server
import com.example.japanvpn.repository.ServerRepository
import com.example.japanvpn.utils.PreferencesManager
import com.example.japanvpn.vpn.VpnState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val serverRepository = ServerRepository()
    private val preferencesManager = PreferencesManager(application)

    // VPN State
    private val _vpnState = MutableStateFlow<VpnState>(VpnState.Disconnected)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    // Servers
    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    // Selected Server
    private val _selectedServer = MutableStateFlow<Server?>(null)
    val selectedServer: StateFlow<Server?> = _selectedServer.asStateFlow()

    // Connection Statistics
    private val _connectionStats = MutableStateFlow(ConnectionStatistics())
    val connectionStats: StateFlow<ConnectionStatistics> = _connectionStats.asStateFlow()

    // Loading State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            // Load initial servers list
            _servers.value = serverRepository.servers.value

            // Load last connected server
            preferencesManager.lastConnectedServerId.collect { serverId ->
                serverId?.let { id ->
                    serverRepository.getServerById(id)?.let { server ->
                        _selectedServer.value = server
                    }
                }
            }
        }

        // Start observing server updates
        viewModelScope.launch {
            serverRepository.servers.collect { servers ->
                _servers.value = servers
            }
        }
    }

    fun refreshServers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                serverRepository.refreshServers()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedServer(server: Server) {
        viewModelScope.launch {
            _selectedServer.value = server
            preferencesManager.setLastConnectedServerId(server.id)
        }
    }

    fun connectVpn() {
        val server = _selectedServer.value ?: return
        
        viewModelScope.launch {
            _vpnState.value = VpnState.Connecting
            
            // Start VPN Service
            val intent = JapanVpnService.createIntent(
                context = getApplication(),
                action = JapanVpnService.ACTION_CONNECT,
                server = server
            )
            getApplication<Application>().startService(intent)
        }
    }

    fun disconnectVpn() {
        viewModelScope.launch {
            // Stop VPN Service
            val intent = JapanVpnService.createIntent(
                context = getApplication(),
                action = JapanVpnService.ACTION_DISCONNECT
            )
            getApplication<Application>().startService(intent)
            
            _vpnState.value = VpnState.Disconnected
            _connectionStats.value = ConnectionStatistics()
        }
    }

    fun updateVpnState(state: VpnState) {
        _vpnState.value = state
        if (state is VpnState.Connected) {
            _connectionStats.value = ConnectionStatistics(
                uptime = state.duration,
                bytesIn = state.bytesIn,
                bytesOut = state.bytesOut,
                currentServer = state.server,
                status = ConnectionStatistics.ConnectionStatus.CONNECTED
            )
        }
    }

    // Settings
    val autoConnect = preferencesManager.autoConnect
    val killSwitch = preferencesManager.killSwitch
    val splitTunneling = preferencesManager.splitTunneling
    val autoStart = preferencesManager.autoStart
    val notificationsEnabled = preferencesManager.notificationsEnabled
    val selectedProtocol = preferencesManager.selectedProtocol

    fun setAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoConnect(enabled)
        }
    }

    fun setKillSwitch(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setKillSwitch(enabled)
        }
    }

    fun setSplitTunneling(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSplitTunneling(enabled)
        }
    }

    fun setAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoStart(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun setSelectedProtocol(protocol: String) {
        viewModelScope.launch {
            preferencesManager.setSelectedProtocol(protocol)
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
