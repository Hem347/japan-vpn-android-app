package com.example.japanvpn.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.japanvpn.R
import com.example.japanvpn.model.Server

class ServerItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var tvServerName: TextView
    private var tvServerLocation: TextView
    private var tvPing: TextView
    private var tvLoad: TextView
    private var tvProtocol: TextView

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_server_item, this, true)

        // Initialize views
        tvServerName = findViewById(R.id.tvServerName)
        tvServerLocation = findViewById(R.id.tvServerLocation)
        tvPing = findViewById(R.id.tvPing)
        tvLoad = findViewById(R.id.tvLoad)
        tvProtocol = findViewById(R.id.tvProtocol)
    }

    fun setServer(server: Server) {
        tvServerName.text = server.name
        tvServerLocation.text = server.city
        tvProtocol.text = server.protocol.toString()

        // Set ping status
        val pingText = when {
            server.ping < 0 -> "---"
            server.ping < 50 -> "${server.ping}ms (Excellent)"
            server.ping < 100 -> "${server.ping}ms (Good)"
            server.ping < 200 -> "${server.ping}ms (Fair)"
            else -> "${server.ping}ms (Poor)"
        }
        tvPing.text = pingText

        // Set ping color
        val pingColor = when {
            server.ping < 0 -> R.color.text_secondary
            server.ping < 50 -> R.color.connected
            server.ping < 100 -> R.color.connecting
            else -> R.color.disconnected
        }
        tvPing.setTextColor(ContextCompat.getColor(context, pingColor))

        // Set load status
        val loadText = when {
            server.load < 50 -> "${server.load}% (Low)"
            server.load < 80 -> "${server.load}% (Medium)"
            else -> "${server.load}% (High)"
        }
        tvLoad.text = loadText

        // Set load color
        val loadColor = when {
            server.load < 50 -> R.color.connected
            server.load < 80 -> R.color.connecting
            else -> R.color.disconnected
        }
        tvLoad.setTextColor(ContextCompat.getColor(context, loadColor))

        // Update background if server is active
        isSelected = server.isActive
        if (server.isActive) {
            setBackgroundResource(R.drawable.bg_server_selected)
        } else {
            setBackgroundResource(R.drawable.bg_server_normal)
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            setBackgroundResource(R.drawable.bg_server_selected)
        } else {
            setBackgroundResource(R.drawable.bg_server_normal)
        }
    }
}
