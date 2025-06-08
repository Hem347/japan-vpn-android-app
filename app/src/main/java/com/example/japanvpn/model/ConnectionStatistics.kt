package com.example.japanvpn.model

import com.example.japanvpn.vpn.VpnState
import java.util.concurrent.TimeUnit

data class ConnectionStatistics(
    val bytesIn: Long = 0,
    val bytesOut: Long = 0,
    val duration: Long = 0,
    val status: VpnState = VpnState.Disconnected,
    val currentServer: Server? = null
) {
    fun getFormattedUptime(): String {
        return String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(duration),
            TimeUnit.MILLISECONDS.toMinutes(duration) % 60,
            TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        )
    }

    fun getFormattedBytesIn(): String {
        return formatDataUsage(bytesIn)
    }

    fun getFormattedBytesOut(): String {
        return formatDataUsage(bytesOut)
    }

    private fun formatDataUsage(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    companion object {
        fun empty() = ConnectionStatistics()
    }
}
