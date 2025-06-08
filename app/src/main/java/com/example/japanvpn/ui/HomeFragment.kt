package com.example.japanvpn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.japanvpn.MainViewModel
import com.example.japanvpn.R
import com.example.japanvpn.databinding.FragmentHomeBinding
import com.example.japanvpn.vpn.VpnState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Server card click listener
        binding.cardServer.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ServersFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        // Connect button click listener
        binding.btnConnect.setOnClickListener {
            when (viewModel.vpnState.value) {
                is VpnState.Connected -> viewModel.disconnectVpn()
                is VpnState.Disconnected -> viewModel.connectVpn()
                else -> { /* Ignore clicks while connecting/disconnecting */ }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe VPN state
            viewModel.vpnState.collectLatest { state ->
                updateVpnState(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe selected server
            viewModel.selectedServer.collectLatest { server ->
                binding.tvSelectedServer.text = server?.name ?: getString(R.string.select_server)
                binding.tvServerLocation.text = server?.let { "${it.city}, ${it.country}" } ?: ""
                binding.tvServerProtocol.text = server?.protocol?.toString() ?: ""
                binding.tvServerProtocol.visibility = if (server != null) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe connection statistics
            viewModel.connectionStats.collectLatest { stats ->
                binding.connectionStats.updateStats(stats)
            }
        }
    }

    private fun updateVpnState(state: VpnState) {
        binding.apply {
            when (state) {
                is VpnState.Connected -> {
                    ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                    ivConnectionStatus.setColorFilter(resources.getColor(R.color.connected, null))
                    tvConnectionStatus.text = getString(R.string.status_connected)
                    tvConnectionStatus.setTextColor(resources.getColor(R.color.connected, null))
                    btnConnect.text = getString(R.string.disconnect)
                    btnConnect.setBackgroundColor(resources.getColor(R.color.disconnected, null))
                    connectionStats.visibility = View.VISIBLE
                }
                is VpnState.Connecting -> {
                    ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                    ivConnectionStatus.setColorFilter(resources.getColor(R.color.connecting, null))
                    tvConnectionStatus.text = getString(R.string.status_connecting)
                    tvConnectionStatus.setTextColor(resources.getColor(R.color.connecting, null))
                    btnConnect.text = getString(R.string.connecting)
                    btnConnect.setBackgroundColor(resources.getColor(R.color.connecting, null))
                    connectionStats.visibility = View.GONE
                }
                is VpnState.Disconnected -> {
                    ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                    ivConnectionStatus.setColorFilter(resources.getColor(R.color.disconnected, null))
                    tvConnectionStatus.text = getString(R.string.status_disconnected)
                    tvConnectionStatus.setTextColor(resources.getColor(R.color.disconnected, null))
                    btnConnect.text = getString(R.string.connect)
                    btnConnect.setBackgroundColor(resources.getColor(R.color.colorPrimary, null))
                    connectionStats.visibility = View.GONE
                    connectionStats.resetStats()
                }
                is VpnState.Error -> {
                    ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                    ivConnectionStatus.setColorFilter(resources.getColor(R.color.disconnected, null))
                    tvConnectionStatus.text = state.message
                    tvConnectionStatus.setTextColor(resources.getColor(R.color.disconnected, null))
                    btnConnect.text = getString(R.string.retry)
                    btnConnect.setBackgroundColor(resources.getColor(R.color.colorPrimary, null))
                    connectionStats.visibility = View.GONE
                    connectionStats.resetStats()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
