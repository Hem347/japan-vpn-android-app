package com.example.japanvpn.vpn

import com.example.japanvpn.model.Server

sealed class VpnState {
    object Disconnected : VpnState()
    object Connecting : VpnState()
    data class Connected(
        val server: Server,
        val bytesIn: Long = 0,
        val bytesOut: Long = 0,
        val duration: Long = 0
    ) : VpnState()
    data class Error(val message: String) : VpnState()
}

sealed class VpnEvent {
    object Connect : VpnEvent()
    object Disconnect : VpnEvent()
    data class ServerSelected(val server: Server) : VpnEvent()
    data class Error(val message: String) : VpnEvent()
    data class UpdateStats(
        val bytesIn: Long,
        val bytesOut: Long,
        val duration: Long
    ) : VpnEvent()
}

data class VpnStats(
    val bytesIn: Long = 0,
    val bytesOut: Long = 0,
    val duration: Long = 0,
    val lastUpdateTime: Long = System.currentTimeMillis()
) {
    val totalBytes: Long
        get() = bytesIn + bytesOut

    fun getSpeedIn(previousStats: VpnStats): Long {
        val timeDiff = lastUpdateTime - previousStats.lastUpdateTime
        return if (timeDiff > 0) {
            ((bytesIn - previousStats.bytesIn) * 1000) / timeDiff
        } else {
            0
        }
    }

    fun getSpeedOut(previousStats: VpnStats): Long {
        val timeDiff = lastUpdateTime - previousStats.lastUpdateTime
        return if (timeDiff > 0) {
            ((bytesOut - previousStats.bytesOut) * 1000) / timeDiff
        } else {
            0
        }
    }
}

sealed class ConnectionResult {
    object Success : ConnectionResult()
    data class Failure(val reason: String) : ConnectionResult()
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR;

    fun isStable(): Boolean = this == CONNECTED || this == DISCONNECTED
    
    fun isConnecting(): Boolean = this == CONNECTING
    
    fun isConnected(): Boolean = this == CONNECTED
    
    fun isDisconnected(): Boolean = this == DISCONNECTED
    
    fun hasError(): Boolean = this == ERROR
}

data class ConnectionStatistics(
    val uptime: Long = 0,
    val bytesIn: Long = 0,
    val bytesOut: Long = 0,
    val currentServer: Server? = null,
    val lastError: String? = null,
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED
) {
    val isConnected: Boolean
        get() = status == ConnectionStatus.CONNECTED

    val isStable: Boolean
        get() = status.isStable()

    val hasError: Boolean
        get() = lastError != null

    fun getFormattedUptime(): String {
        val seconds = uptime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
    }

    companion object {
        fun createConnected(server: Server) = ConnectionStatistics(
            currentServer = server,
            status = ConnectionStatus.CONNECTED
        )

        fun createError(error: String) = ConnectionStatistics(
            lastError = error,
            status = ConnectionStatus.ERROR
        )
    }
}
