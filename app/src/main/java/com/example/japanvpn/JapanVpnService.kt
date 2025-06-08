package com.example.japanvpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.japanvpn.model.Server
import com.example.japanvpn.utils.PreferencesManager
import com.example.japanvpn.vpn.VpnConfig
import com.example.japanvpn.vpn.VpnConnection
import com.example.japanvpn.vpn.VpnState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class JapanVpnService : VpnService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var connection: VpnConnection? = null
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val server = intent.getParcelableExtra<Server>(EXTRA_SERVER)
                    ?: return START_NOT_STICKY
                connect(server)
            }
            ACTION_DISCONNECT -> disconnect()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun connect(server: Server) {
        serviceScope.launch {
            // Get VPN configuration from preferences
            val config = VpnConfig(
                splitTunneling = preferencesManager.splitTunneling.value,
                allowedApps = emptySet(), // TODO: Implement app selection
                mtu = 1500,
                dns = listOf("8.8.8.8", "8.8.4.4")
            )

            // Create and start VPN connection
            connection = VpnConnection(this@JapanVpnService, server, config, serviceScope).also { conn ->
                // Observe connection state
                serviceScope.launch {
                    conn.state.collectLatest { state ->
                        updateNotification(state)
                        broadcastState(state)
                    }
                }
                conn.connect()
            }

            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification(VpnState.Connecting))
        }
    }

    private fun disconnect() {
        connection?.disconnect()
        connection = null
        stopForeground(true)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows VPN connection status"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(state: VpnState): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val (title, content) = when (state) {
            is VpnState.Connected -> {
                val duration = state.duration / 1000 // Convert to seconds
                val hours = duration / 3600
                val minutes = (duration % 3600) / 60
                val seconds = duration % 60
                Pair(
                    "Connected to ${state.server.name}",
                    String.format("Connected for %02d:%02d:%02d", hours, minutes, seconds)
                )
            }
            is VpnState.Connecting -> Pair(
                "Connecting...",
                "Establishing VPN connection"
            )
            is VpnState.Disconnected -> Pair(
                "Disconnected",
                "Tap to reconnect"
            )
            is VpnState.Error -> Pair(
                "Connection Error",
                state.message
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn_lock)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(state !is VpnState.Disconnected)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(state: VpnState) {
        val notification = createNotification(state)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun broadcastState(state: VpnState) {
        sendBroadcast(Intent(ACTION_VPN_STATE_CHANGED).apply {
            putExtra(EXTRA_VPN_STATE, state.toString())
        })
    }

    companion object {
        private const val CHANNEL_ID = "vpn_status"
        private const val NOTIFICATION_ID = 1

        const val ACTION_CONNECT = "com.example.japanvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.japanvpn.DISCONNECT"
        const val ACTION_VPN_STATE_CHANGED = "com.example.japanvpn.VPN_STATE_CHANGED"
        
        const val EXTRA_SERVER = "server"
        const val EXTRA_VPN_STATE = "vpn_state"

        fun createIntent(context: Context, action: String, server: Server? = null): Intent {
            return Intent(context, JapanVpnService::class.java).apply {
                this.action = action
                server?.let { putExtra(EXTRA_SERVER, it) }
            }
        }
    }
}
