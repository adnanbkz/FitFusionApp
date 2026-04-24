package com.example.fitfusion.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.health.DailyHealthData
import com.example.fitfusion.data.health.HealthConnectManager
import com.example.fitfusion.data.health.HealthConnectSyncService
import com.example.fitfusion.data.repository.HealthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class HealthConnectStatus { AVAILABLE, NEEDS_UPDATE, UNAVAILABLE }

data class SettingsUiState(
    val pushNotifications: Boolean = true,
    val healthSyncEnabled: Boolean = false,
    // Health Connect
    val hcStatus: HealthConnectStatus = HealthConnectStatus.UNAVAILABLE,
    val hasPermissions: Boolean = false,
    val isSyncing: Boolean = false,
    val syncData: DailyHealthData? = null,
    val lastSyncTime: String? = null,
    val syncError: String? = null,
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val healthManager = HealthConnectManager(app)

    private val healthRepository = HealthRepository(
        FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance(),
    )

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            healthSyncEnabled = prefs.getBoolean(KEY_HEALTH_SYNC_ENABLED, false)
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshHealthStatus()
    }

    /** Re-checks HC availability + permissions — call on screen resume. */
    fun refreshHealthStatus() {
        val status = when {
            healthManager.isAvailable() -> HealthConnectStatus.AVAILABLE
            healthManager.needsUpdate() -> HealthConnectStatus.NEEDS_UPDATE
            else -> HealthConnectStatus.UNAVAILABLE
        }
        _uiState.update { it.copy(hcStatus = status) }

        if (status == HealthConnectStatus.AVAILABLE) {
            viewModelScope.launch {
                val hasPerms = healthManager.hasAllPermissions()
                _uiState.update { it.copy(hasPermissions = hasPerms) }
                if (hasPerms && _uiState.value.healthSyncEnabled && _uiState.value.syncData == null) {
                    syncNow()
                }
            }
        }
    }

    fun onPushNotificationsChange(enabled: Boolean) {
        _uiState.update { it.copy(pushNotifications = enabled) }
    }

    fun onHealthSyncToggle(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HEALTH_SYNC_ENABLED, enabled).apply()
        _uiState.update { it.copy(healthSyncEnabled = enabled, syncError = null) }
        if (enabled && _uiState.value.hcStatus == HealthConnectStatus.AVAILABLE
            && _uiState.value.hasPermissions
        ) {
            syncNow()
        }
    }

    fun onHealthPermissionsResult(grantedPermissions: Set<String>) {
        val hasPermissions = grantedPermissions.containsAll(healthManager.permissions)
        _uiState.update {
            it.copy(
                hasPermissions = hasPermissions,
                syncError = if (hasPermissions) {
                    null
                } else {
                    "Faltan permisos de Health Connect. Puedes reintentarlo o gestionarlos desde ajustes."
                },
            )
        }
        if (hasPermissions && _uiState.value.healthSyncEnabled) {
            syncNow()
        }
    }

    fun syncNow() {
        if (_uiState.value.isSyncing) return
        if (_uiState.value.hcStatus != HealthConnectStatus.AVAILABLE) return
        if (!_uiState.value.hasPermissions) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncError = null) }
            try {
                val client = healthManager.getClient()
                val syncService = HealthConnectSyncService(client)
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    syncService.readDailyData()
                } else {
                    // minSdk = 26 = O, this branch is never reached
                    null
                }
                if (data != null) {
                    healthRepository.saveDailyHealthData(data)
                    val time = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    } else ""
                    _uiState.update { it.copy(isSyncing = false, syncData = data, lastSyncTime = time) }
                } else {
                    _uiState.update { it.copy(isSyncing = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncError = e.localizedMessage ?: "Error desconocido durante la sincronización",
                    )
                }
            }
        }
    }

    fun dismissSyncError() {
        _uiState.update { it.copy(syncError = null) }
    }
}

private const val PREFS_NAME = "settings_prefs"
private const val KEY_HEALTH_SYNC_ENABLED = "health_sync_enabled"
