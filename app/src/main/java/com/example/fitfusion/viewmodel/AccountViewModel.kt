package com.example.fitfusion.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.UserProfile
import com.example.fitfusion.data.repository.UserProfileStore
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class AccountUiState(
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val goalType: String = "",
    val activityLevel: String = "",
    val photoUrl: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AccountViewModel(
    application: Application,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository(),
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, saveSuccess = false) }
    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value, saveSuccess = false) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value, saveSuccess = false) }
    fun onHeightCmChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 3) {
            _uiState.update { it.copy(heightCm = value, saveSuccess = false) }
        }
    }
    fun onWeightKgChange(value: String) {
        if (value.matches(Regex("^\\d{0,3}([.]\\d{0,1})?$"))) {
            _uiState.update { it.copy(weightKg = value, saveSuccess = false) }
        }
    }
    fun onGoalTypeChange(value: String) = _uiState.update { it.copy(goalType = value, saveSuccess = false) }
    fun onActivityLevelChange(value: String) = _uiState.update { it.copy(activityLevel = value, saveSuccess = false) }
    fun onCurrentPasswordChange(value: String) = _uiState.update { it.copy(currentPassword = value) }
    fun onNewPasswordChange(value: String) = _uiState.update { it.copy(newPassword = value) }
    fun onConfirmNewPasswordChange(value: String) = _uiState.update { it.copy(confirmNewPassword = value) }

    fun onPhotoChange(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }
        UserProfileStore.updatePhotoUri(getApplication(), uri)
        _uiState.update { it.copy(photoUrl = uri.toString(), saveSuccess = false) }
    }

    fun saveProfile() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(errorMessage = "Inicia sesión para editar tu perfil") }
            return
        }

        val state = _uiState.value
        val displayName = state.displayName.trim()
        if (displayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, errorMessage = null) }
            try {
                val photoUrl = state.photoUrl.ifBlank { user.photoUrl?.toString() }
                val profile = UserProfile(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    displayName = displayName,
                    username = UserRepository.normalizeUsername(state.username, displayName),
                    bio = state.bio.trim(),
                    photoUrl = photoUrl,
                    heightCm = state.heightCm.toIntOrNull(),
                    weightKg = state.weightKg.toFloatOrNull(),
                    goalType = state.goalType.trim().ifBlank { null },
                    activityLevel = state.activityLevel.trim().ifBlank { null },
                )
                userRepository.updateUserProfile(profile)
                user.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                ).await()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        displayName = profile.displayName,
                        username = profile.username,
                        bio = profile.bio,
                        heightCm = profile.heightCm?.toString().orEmpty(),
                        weightKg = profile.weightKg?.let { w ->
                            if (w % 1f == 0f) w.toInt().toString() else "%.1f".format(Locale.US, w)
                        }.orEmpty(),
                        goalType = profile.goalType.orEmpty(),
                        activityLevel = profile.activityLevel.orEmpty(),
                        photoUrl = profile.photoUrl.orEmpty(),
                        errorMessage = null,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.localizedMessage ?: "No se pudo guardar el perfil",
                    )
                }
            }
        }
    }

    fun changePassword() {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Inicia sesión para cambiar la contraseña") }
            return
        }

        val state = _uiState.value
        when {
            state.currentPassword.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Introduce tu contraseña actual") }
                return
            }
            state.newPassword != state.confirmNewPassword -> {
                _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
                return
            }
            state.newPassword.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveSuccess = false, errorMessage = null) }
            try {
                user.reauthenticate(
                    EmailAuthProvider.getCredential(email, state.currentPassword)
                ).await()
                user.updatePassword(state.newPassword).await()
                _uiState.update {
                    it.copy(
                        currentPassword = "",
                        newPassword = "",
                        confirmNewPassword = "",
                        isSaving = false,
                        saveSuccess = true,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.localizedMessage ?: "No se pudo actualizar la contraseña",
                    )
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null, saveSuccess = false) }

    private fun loadProfile() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No hay sesión activa") }
            return
        }

        viewModelScope.launch {
            try {
                val profile = userRepository.getUserProfile(user.uid, user.email.orEmpty())
                val localPhotoUri = UserProfileStore.photoUri.value?.toString().orEmpty()
                _uiState.update {
                    it.copy(
                        displayName = profile.displayName,
                        username = profile.username,
                        email = profile.email,
                        bio = profile.bio,
                        heightCm = profile.heightCm?.toString().orEmpty(),
                        weightKg = profile.weightKg?.let { weight ->
                            if (weight % 1f == 0f) weight.toInt().toString() else "%.1f".format(Locale.US, weight)
                        }.orEmpty(),
                        goalType = profile.goalType.orEmpty(),
                        activityLevel = profile.activityLevel.orEmpty(),
                        photoUrl = localPhotoUri.ifBlank { profile.photoUrl.orEmpty() },
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage ?: "No se pudo cargar el perfil",
                    )
                }
            }
        }
    }
}
