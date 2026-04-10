package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val pushNotifications: Boolean = true,
    val healthSync: Boolean = false
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onPushNotificationsChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotifications = enabled)
    }

    fun onHealthSyncChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(healthSync = enabled)
        // TODO: Enable/disable Health Connect sync
    }
}