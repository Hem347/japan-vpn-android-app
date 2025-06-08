package com.example.japanvpn.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.japanvpn.databinding.ViewServerItemBinding
import com.example.japanvpn.model.Server

class ServerListAdapter(
    private val onServerClick: (Server) -> Unit
) : ListAdapter<Server, ServerListAdapter.ServerViewHolder>(ServerDiffCallback()) {

    private var selectedServerId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val binding = ViewServerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = getItem(position)
        holder.bind(server, server.id == selectedServerId)
    }

    fun setSelectedServer(serverId: String) {
        val oldSelectedId = selectedServerId
        selectedServerId = serverId
        
        oldSelectedId?.let { old ->
            val oldPosition = currentList.indexOfFirst { it.id == old }
            if (oldPosition != -1) notifyItemChanged(oldPosition)
        }
        
        val newPosition = currentList.indexOfFirst { it.id == serverId }
        if (newPosition != -1) notifyItemChanged(newPosition)
    }

    inner class ServerViewHolder(
        private val binding: ViewServerItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onServerClick(getItem(position))
                }
            }
        }

        fun bind(server: Server, isSelected: Boolean) {
            binding.root.isSelected = isSelected
            
            binding.tvServerName.text = server.name
            binding.tvServerLocation.text = "${server.city}, ${server.country}"
            binding.tvProtocol.text = server.protocol.toString()
            
            // Format ping
            val pingText = when {
                server.ping < 0 -> "-- ms"
                server.ping < 50 -> "${server.ping}ms (Excellent)"
                server.ping < 100 -> "${server.ping}ms (Good)"
                else -> "${server.ping}ms (Poor)"
            }
            binding.tvPing.text = pingText

            // Format load
            val loadText = when {
                server.load < 50 -> "${server.load}% (Low)"
                server.load < 80 -> "${server.load}% (Medium)"
                else -> "${server.load}% (High)"
            }
            binding.tvLoad.text = loadText
        }
    }

    private class ServerDiffCallback : DiffUtil.ItemCallback<Server>() {
        override fun areItemsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Server, newItem: Server): Boolean {
            return oldItem == newItem
        }
    }
}
