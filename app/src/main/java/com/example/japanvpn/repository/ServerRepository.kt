package com.example.japanvpn.repository

import com.example.japanvpn.model.Server
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class ServerRepository {
    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: Flow<List<Server>> = _servers.asStateFlow()

    init {
        // Initialize with test servers
        _servers.value = Server.createTestServers()
    }

    suspend fun refreshServers() {
        // Simulate network delay
        delay(1000)

        // Update server stats
        _servers.value = _servers.value.map { server ->
            server.copy(
                ping = Random.nextInt(20, 150),
                load = Random.nextInt(10, 90)
            )
        }.sortedBy { it.ping }
    }

    fun getServerById(id: String): Server? {
        return _servers.value.find { it.id == id }
    }

    suspend fun updateServerStatus(serverId: String, isActive: Boolean) {
        _servers.value = _servers.value.map { server ->
            if (server.id == serverId) {
                server.copy(isActive = isActive)
            } else {
                server.copy(isActive = false)
            }
        }
    }

    suspend fun measureServerLatencies() {
        // Simulate measuring server latencies
        delay(500)
        
        _servers.value = _servers.value.map { server ->
            server.copy(ping = Random.nextInt(20, 150))
        }.sortedBy { it.ping }
    }

    suspend fun updateServerLoads() {
        // Simulate updating server loads
        delay(500)
        
        _servers.value = _servers.value.map { server ->
            server.copy(load = Random.nextInt(10, 90))
        }
    }

    companion object {
        private const val TAG = "ServerRepository"
    }
}
