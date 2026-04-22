package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AccountUiState(
    val displayName: String = "Alex Rivera",
    val username: String = "@alex.fit",
    val email: String = "alex.fit@fitfusion.app",
    val bio: String = "Entusiasta del fitness. 🏃 Correr • Fuerza • Nutrición",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AccountViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value) }
    fun onCurrentPasswordChange(value: String) = _uiState.update { it.copy(currentPassword = value) }
    fun onNewPasswordChange(value: String) = _uiState.update { it.copy(newPassword = value) }
    fun onConfirmNewPasswordChange(value: String) = _uiState.update { it.copy(confirmNewPassword = value) }

    fun saveProfile() {
        _uiState.update { it.copy(isSaving = true, saveSuccess = false, errorMessage = null) }
        // TODO: llamada a API
        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
    }

    fun changePassword() {
        val s = _uiState.value
        if (s.newPassword != s.confirmNewPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }
        if (s.newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        // TODO: llamada a API
        _uiState.update { it.copy(currentPassword = "", newPassword = "", confirmNewPassword = "", saveSuccess = true) }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null, saveSuccess = false) }
}