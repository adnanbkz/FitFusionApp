package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DataStorageUiState(
    val cacheSizeMb: Float = 128.4f,
    val totalStorageMb: Float = 512f,
    val usedStorageMb: Float = 234.7f,
    val isClearingCache: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

class DataStorageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DataStorageUiState())
    val uiState: StateFlow<DataStorageUiState> = _uiState.asStateFlow()

    fun clearCache() {
        _uiState.update { it.copy(isClearingCache = true) }
        // TODO: limpiar caché real
        _uiState.update { it.copy(isClearingCache = false, cacheSizeMb = 0f) }
    }

    fun exportData() {
        _uiState.update { it.copy(isExporting = true, exportSuccess = false) }
        // TODO: exportar datos del usuario
        _uiState.update { it.copy(isExporting = false, exportSuccess = true) }
    }

    fun showDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun deleteWorkoutData() {
        _uiState.update { it.copy(showDeleteConfirm = false, usedStorageMb = it.usedStorageMb * 0.4f) }
        // TODO: borrar datos de entrenamiento
    }

    fun dismissExportSuccess() = _uiState.update { it.copy(exportSuccess = false) }
}