package com.example.japanvpn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.japanvpn.MainViewModel
import com.example.japanvpn.adapter.ServerListAdapter
import com.example.japanvpn.databinding.FragmentServersBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServersFragment : Fragment() {
    private var _binding: FragmentServersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var serverAdapter: ServerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }

    private fun setupViews() {
        // Setup toolbar
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // Setup RecyclerView
        serverAdapter = ServerListAdapter { server ->
            viewModel.setSelectedServer(server)
            requireActivity().onBackPressed()
        }

        binding.serverList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serverAdapter
            setHasFixedSize(true)
        }

        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshServers()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe servers list
            viewModel.servers.collectLatest { servers ->
                binding.apply {
                    if (servers.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        serverList.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        serverList.visibility = View.VISIBLE
                        serverAdapter.submitList(servers)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe selected server
            viewModel.selectedServer.collectLatest { server ->
                server?.let { serverAdapter.setSelectedServer(it.id) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe loading state
            viewModel.isLoading.collectLatest { isLoading ->
                binding.apply {
                    swipeRefresh.isRefreshing = isLoading
                    loadingState.visibility = if (isLoading && serverAdapter.currentList.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ServersFragment()
    }
}
