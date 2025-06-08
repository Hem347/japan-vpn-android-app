package com.example.japanvpn.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    // Settings keys
    private object PreferencesKeys {
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val KILL_SWITCH = booleanPreferencesKey("kill_switch")
        val SPLIT_TUNNELING = booleanPreferencesKey("split_tunneling")
        val AUTO_START = booleanPreferencesKey("auto_start")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SELECTED_PROTOCOL = stringPreferencesKey("selected_protocol")
        val LAST_CONNECTED_SERVER_ID = stringPreferencesKey("last_connected_server_id")
    }

    // Auto Connect
    val autoConnect: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_CONNECT] ?: false
        }

    suspend fun setAutoConnect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_CONNECT] = enabled
        }
    }

    // Kill Switch
    val killSwitch: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.KILL_SWITCH] ?: false
        }

    suspend fun setKillSwitch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KILL_SWITCH] = enabled
        }
    }

    // Split Tunneling
    val splitTunneling: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SPLIT_TUNNELING] ?: false
        }

    suspend fun setSplitTunneling(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SPLIT_TUNNELING] = enabled
        }
    }

    // Auto Start
    val autoStart: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_START] ?: false
        }

    suspend fun setAutoStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_START] = enabled
        }
    }

    // Notifications
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Selected Protocol
    val selectedProtocol: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_PROTOCOL] ?: "AUTOMATIC"
        }

    suspend fun setSelectedProtocol(protocol: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PROTOCOL] = protocol
        }
    }

    // Last Connected Server
    val lastConnectedServerId: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_CONNECTED_SERVER_ID]
        }

    suspend fun setLastConnectedServerId(serverId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_CONNECTED_SERVER_ID] = serverId
        }
    }

    companion object {
        private const val TAG = "PreferencesManager"
    }
}
