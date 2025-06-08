package com.example.japanvpn

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.japanvpn.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        setupUI()
        loadSettings()
    }

    private fun setupUI() {
        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        // Setup preference change listeners
        binding.switchAutoConnect.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_AUTO_CONNECT, isChecked)
            if (isChecked) {
                Toast.makeText(this, "Auto-connect enabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchKillSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_KILL_SWITCH, isChecked)
            if (isChecked) {
                Toast.makeText(this, "Kill switch enabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchSplitTunneling.setOnCheckedChangeListener { _, isChecked ->
            savePreference(PREF_SPLIT_TUNNELING, isChecked)
            binding.layoutAppSelection.isEnabled = isChecked
        }

        // Protocol selection
        binding.radioGroupProtocol.setOnCheckedChangeListener { _, checkedId ->
            val protocol = when (checkedId) {
                R.id.radioOpenVPN -> PROTOCOL_OPENVPN
                R.id.radioWireGuard -> PROTOCOL_WIREGUARD
                else -> PROTOCOL_AUTOMATIC
            }
            savePreference(PREF_PROTOCOL, protocol)
        }
    }

    private fun loadSettings() {
        // Load saved preferences
        binding.switchAutoConnect.isChecked = preferences.getBoolean(PREF_AUTO_CONNECT, false)
        binding.switchKillSwitch.isChecked = preferences.getBoolean(PREF_KILL_SWITCH, false)
        binding.switchSplitTunneling.isChecked = preferences.getBoolean(PREF_SPLIT_TUNNELING, false)

        // Load protocol selection
        when (preferences.getString(PREF_PROTOCOL, PROTOCOL_AUTOMATIC)) {
            PROTOCOL_OPENVPN -> binding.radioOpenVPN.isChecked = true
            PROTOCOL_WIREGUARD -> binding.radioWireGuard.isChecked = true
            else -> binding.radioAutomatic.isChecked = true
        }
    }

    private fun savePreference(key: String, value: Boolean) {
        preferences.edit {
            putBoolean(key, value)
        }
    }

    private fun savePreference(key: String, value: String) {
        preferences.edit {
            putString(key, value)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val PREFS_NAME = "vpn_preferences"
        const val PREF_AUTO_CONNECT = "auto_connect"
        const val PREF_KILL_SWITCH = "kill_switch"
        const val PREF_SPLIT_TUNNELING = "split_tunneling"
        const val PREF_PROTOCOL = "protocol"

        const val PROTOCOL_AUTOMATIC = "automatic"
        const val PROTOCOL_OPENVPN = "openvpn"
        const val PROTOCOL_WIREGUARD = "wireguard"
    }
}
