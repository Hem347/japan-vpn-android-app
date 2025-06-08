package com.example.japanvpn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.japanvpn.MainActivity
import com.example.japanvpn.MainViewModel
import com.example.japanvpn.R
import com.example.japanvpn.databinding.FragmentHomeBinding
import com.example.japanvpn.utils.VpnUtils
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
        observeState()
    }

    private fun setupViews() {
        // Connect button click handler
        binding.btnConnect.setOnClickListener {
            (activity as? MainActivity)?.checkVpnPermission()
        }

        // Server selection click handler
        binding.cardServer.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ServersFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe VPN state
            viewModel.vpnState.collectLatest { state ->
                updateUiForState(state)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe selected server
            viewModel.selectedServer.collectLatest { server ->
                server?.let {
                    binding.tvSelectedServer.text = it.name
                    binding.tvServerLocation.text = "${it.city}, ${it.country}"
                    binding.tvServerProtocol.text = it.protocol.toString()
                }
            }
        }
    }

    private fun updateUiForState(state: VpnState) {
        when (state) {
            is VpnState.Connected -> {
                binding.tvConnectionStatus.text = "Connected"
                binding.tvConnectionStatus.setTextColor(resources.getColor(R.color.connected, null))
                binding.ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                binding.btnConnect.text = "Disconnect"
                binding.btnConnect.setBackgroundColor(resources.getColor(R.color.disconnected, null))

                // Update connection stats
                binding.connectionStats.updateStats(
                    bytesIn = state.bytesIn,
                    bytesOut = state.bytesOut,
                    duration = state.duration
                )
            }
            is VpnState.Connecting -> {
                binding.tvConnectionStatus.text = "Connecting..."
                binding.tvConnectionStatus.setTextColor(resources.getColor(R.color.connecting, null))
                binding.ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                binding.btnConnect.text = "Cancel"
                binding.btnConnect.setBackgroundColor(resources.getColor(R.color.connecting, null))
            }
            is VpnState.Disconnected -> {
                binding.tvConnectionStatus.text = "Not Connected"
                binding.tvConnectionStatus.setTextColor(resources.getColor(R.color.disconnected, null))
                binding.ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                binding.btnConnect.text = "Connect"
                binding.btnConnect.setBackgroundColor(resources.getColor(R.color.connected, null))

                // Reset connection stats
                binding.connectionStats.updateStats(
                    bytesIn = 0,
                    bytesOut = 0,
                    duration = 0
                )
            }
            is VpnState.Error -> {
                binding.tvConnectionStatus.text = "Connection Error"
                binding.tvConnectionStatus.setTextColor(resources.getColor(R.color.error, null))
                binding.ivConnectionStatus.setImageResource(R.drawable.ic_vpn_lock)
                binding.btnConnect.text = "Retry"
                binding.btnConnect.setBackgroundColor(resources.getColor(R.color.error, null))
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
