package com.example.japanvpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.japanvpn.databinding.ActivityMainBinding
import com.example.japanvpn.ui.HomeFragment
import com.example.japanvpn.ui.ServersFragment
import com.example.japanvpn.ui.SettingsFragment
import com.example.japanvpn.vpn.VpnState

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.connectVpn()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            showFragment(HomeFragment.newInstance())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showFragment(HomeFragment.newInstance())
                    true
                }
                R.id.nav_servers -> {
                    showFragment(ServersFragment.newInstance())
                    true
                }
                R.id.nav_settings -> {
                    showFragment(SettingsFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun checkVpnPermission() {
        when (viewModel.vpnState.value) {
            is VpnState.Connected, is VpnState.Connecting -> {
                viewModel.disconnectVpn()
            }
            else -> {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    viewModel.connectVpn()
                }
            }
        }
    }
}
