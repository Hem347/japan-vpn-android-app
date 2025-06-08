package com.example.japanvpn.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.example.japanvpn.model.Server
import com.example.japanvpn.utils.VpnUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import kotlin.random.Random

class VpnConnection(
    private val service: VpnService,
    private val server: Server,
    private val config: Config = Config()
) {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var tunnel: DatagramChannel? = null
    private var connectionJob: Job? = null
    private var statsJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var startTime = System.currentTimeMillis()

    private val _state = MutableStateFlow<VpnState>(VpnState.Disconnected)
    val state: StateFlow<VpnState> = _state.asStateFlow()

    private var bytesIn = 0L
    private var bytesOut = 0L

    fun connect() {
        if (_state.value is VpnState.Connected || _state.value is VpnState.Connecting) {
            return
        }

        connectionJob = scope.launch {
            try {
                _state.value = VpnState.Connecting

                // Establish VPN interface
                vpnInterface = establishVpnInterface()
                
                // Create UDP tunnel
                tunnel = createTunnel()

                // Start connection monitoring
                startTime = System.currentTimeMillis()
                startStatsMonitoring()

                _state.value = VpnState.Connected(
                    server = server,
                    bytesIn = 0,
                    bytesOut = 0,
                    duration = 0
                )

                // Simulate packet transfer
                while (isActive) {
                    simulatePacketTransfer()
                    delay(100)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Connection error: ${e.message}")
                _state.value = VpnState.Error(e.message ?: "Unknown error occurred")
                cleanup()
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        statsJob?.cancel()
        cleanup()
        _state.value = VpnState.Disconnected
    }

    private fun establishVpnInterface(): ParcelFileDescriptor {
        return VpnUtils.prepareVpnService(service)
            .setSession(server.name)
            .establish() ?: throw IllegalStateException("Failed to establish VPN interface")
    }

    private fun createTunnel(): DatagramChannel {
        return DatagramChannel.open().apply {
            connect(InetSocketAddress(server.hostname, server.port))
            configureBlocking(false)
            service.protect(socket())
        }
    }

    private fun startStatsMonitoring() {
        statsJob = scope.launch {
            while (isActive) {
                updateStats()
                delay(1000)
            }
        }
    }

    private fun updateStats() {
        if (_state.value !is VpnState.Connected) return

        val duration = System.currentTimeMillis() - startTime
        _state.value = VpnState.Connected(
            server = server,
            bytesIn = bytesIn,
            bytesOut = bytesOut,
            duration = duration
        )
    }

    private suspend fun simulatePacketTransfer() {
        // Simulate incoming traffic
        bytesIn += Random.nextLong(1000, 5000)
        
        // Simulate outgoing traffic
        bytesOut += Random.nextLong(500, 2000)
    }

    private fun cleanup() {
        try {
            tunnel?.close()
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
        tunnel = null
        vpnInterface = null
    }

    data class Config(
        val mtu: Int = 1500,
        val localAddress: String = "10.0.0.2",
        val localPrefix: Int = 32,
        val dnsServers: List<String> = listOf("8.8.8.8", "8.8.4.4")
    )

    companion object {
        private const val TAG = "VpnConnection"
        private const val BUFFER_SIZE = 32767
        private val BUFFER = ByteBuffer.allocate(BUFFER_SIZE)
    }
}
