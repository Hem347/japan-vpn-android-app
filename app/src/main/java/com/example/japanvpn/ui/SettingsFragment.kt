package com.example.japanvpn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.japanvpn.MainViewModel
import com.example.japanvpn.databinding.FragmentSettingsBinding
import com.example.japanvpn.model.Server
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeSettings()
    }

    private fun setupViews() {
        // Auto-connect setting
        binding.switchAutoConnect.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoConnect(isChecked)
        }

        // Kill switch setting
        binding.switchKillSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setKillSwitch(isChecked)
        }

        // Split tunneling setting
        binding.switchSplitTunneling.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSplitTunneling(isChecked)
        }

        // Auto-start setting
        binding.switchAutoStart.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoStart(isChecked)
        }

        // Notifications setting
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }

        // Protocol selection
        binding.radioGroupProtocol.setOnCheckedChangeListener { _, checkedId ->
            val protocol = when (checkedId) {
                binding.radioUdp.id -> Server.Protocol.UDP.name
                binding.radioTcp.id -> Server.Protocol.TCP.name
                else -> Server.Protocol.UDP.name
            }
            viewModel.setSelectedProtocol(protocol)
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Auto-connect
            viewModel.autoConnect.collectLatest { enabled ->
                binding.switchAutoConnect.isChecked = enabled
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Kill switch
            viewModel.killSwitch.collectLatest { enabled ->
                binding.switchKillSwitch.isChecked = enabled
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Split tunneling
            viewModel.splitTunneling.collectLatest { enabled ->
                binding.switchSplitTunneling.isChecked = enabled
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Auto-start
            viewModel.autoStart.collectLatest { enabled ->
                binding.switchAutoStart.isChecked = enabled
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Notifications
            viewModel.notificationsEnabled.collectLatest { enabled ->
                binding.switchNotifications.isChecked = enabled
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Protocol
            viewModel.selectedProtocol.collectLatest { protocol ->
                when (protocol) {
                    Server.Protocol.UDP.name -> binding.radioUdp.isChecked = true
                    Server.Protocol.TCP.name -> binding.radioTcp.isChecked = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
