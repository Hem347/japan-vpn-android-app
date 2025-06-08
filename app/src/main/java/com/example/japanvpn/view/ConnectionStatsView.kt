package com.example.japanvpn.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.japanvpn.R
import com.example.japanvpn.databinding.ViewConnectionStatsBinding
import com.example.japanvpn.model.ConnectionStatistics
import com.example.japanvpn.model.Server
import com.example.japanvpn.vpn.VpnState

class ConnectionStatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewConnectionStatsBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        orientation = VERTICAL
    }

    fun updateState(state: VpnState) {
        when (state) {
            is VpnState.Connected -> {
                showConnectedState(state)
            }
            is VpnState.Connecting -> {
                showConnectingState()
            }
            is VpnState.Disconnected -> {
                showDisconnectedState()
            }
            is VpnState.Error -> {
                showErrorState(state.message)
            }
        }
    }

    private fun showConnectedState(state: VpnState.Connected) {
        with(binding) {
            // Status
            tvStatus.text = "Connected"
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.connected))
            
            // Server info
            serverInfo.isVisible = true
            tvServerName.text = state.server.name
            tvServerLocation.text = "${state.server.city}, ${state.server.country}"
            
            // Connection stats
            statsContainer.isVisible = true
            
            // Duration
            tvDuration.text = formatDuration(state.duration)
            
            // Data usage
            tvDataIn.text = formatBytes(state.bytesIn)
            tvDataOut.text = formatBytes(state.bytesOut)
            
            // Connect button state
            btnConnect.text = "Disconnect"
            btnConnect.setBackgroundResource(R.drawable.bg_button_disconnect)
        }
    }

    private fun showConnectingState() {
        with(binding) {
            tvStatus.text = "Connecting..."
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.connecting))
            serverInfo.isVisible = true
            statsContainer.isVisible = false
            btnConnect.text = "Cancel"
            btnConnect.setBackgroundResource(R.drawable.bg_button_disconnect)
        }
    }

    private fun showDisconnectedState() {
        with(binding) {
            tvStatus.text = "Not Connected"
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.disconnected))
            serverInfo.isVisible = false
            statsContainer.isVisible = false
            btnConnect.text = "Connect"
            btnConnect.setBackgroundResource(R.drawable.bg_button_connect)
        }
    }

    private fun showErrorState(message: String) {
        with(binding) {
            tvStatus.text = "Connection Error"
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.error))
            serverInfo.isVisible = false
            statsContainer.isVisible = false
            btnConnect.text = "Retry"
            btnConnect.setBackgroundResource(R.drawable.bg_button_connect)
        }
    }

    fun setOnConnectClickListener(listener: OnClickListener) {
        binding.btnConnect.setOnClickListener(listener)
    }

    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
