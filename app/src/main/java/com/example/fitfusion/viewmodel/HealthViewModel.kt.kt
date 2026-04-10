package com.example.fitfusion.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.health.HealthConnectManager
import com.example.fitfusion.data.health.HealthConnectSyncService
import com.example.fitfusion.data.repository.HealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HealthSyncState(
    val steps: Long = 0,
    val stepCalories: Int = 0,
    val avgHeartRate: Long? = null,
    val isSyncing: Boolean = false,
    val lastSyncError: String? = null,
    val isHealthConnectAvailable: Boolean = false,
    val hasPermissions: Boolean = false,
)

class HealthViewModel(
    private val healthManager: HealthConnectManager,
    private val healthRepository: HealthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HealthSyncState())
    val state = _state.asStateFlow()

    fun checkAvailability() {
        _state.value = _state.value.copy(
            isHealthConnectAvailable = healthManager.isAvailable()
        )
    }

    fun checkPermissions() {
        viewModelScope.launch {
            val has = healthManager.hasAllPermissions()
            _state.value = _state.value.copy(hasPermissions = has)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun syncToday() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, lastSyncError = null)
            try {
                val client = healthManager.getClient()
                val syncService = HealthConnectSyncService(client)
                val data = syncService.readDailyData()

                // Guardar en Firestore
                healthRepository.saveDailyHealthData(data)

                _state.value = _state.value.copy(
                    steps = data.steps,
                    stepCalories = data.stepCaloriesEstimated,
                    avgHeartRate = data.averageHeartRate,
                    isSyncing = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    lastSyncError = e.message,
                )
            }
        }
    }
}