package com.example.fitfusion.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

data class OnboardingUiState(
    val step: Int = 0,
    val username: String = "",
    val photoUri: Uri? = null,
    val heightCm: String = "",
    val weightKg: String = "",
    val activityLevel: String = "",
    val birthDate: String = "",
    val goalType: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val finished: Boolean = false,
) {
    val canAdvance: Boolean get() = when (step) {
        0    -> username.length >= 3 && username.matches(Regex("[a-z0-9._]+"))
        1    -> true
        2    -> heightCm.toIntOrNull()?.let { it in 80..260 } == true
        3    -> weightKg.toFloatOrNull()?.let { it in 25f..300f } == true
        4    -> activityLevel.isNotBlank()
        5    -> birthDate.length == 10
        6    -> goalType.isNotBlank()
        else -> false
    }
}

class OnboardingViewModel(
    app: Application,
) : AndroidViewModel(app) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository: UserRepository = UserRepository()
    private val storage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        val filtered = value
            .lowercase(Locale.getDefault())
            .filter { it in 'a'..'z' || it in '0'..'9' || it == '.' || it == '_' }
            .take(20)
        _uiState.update { it.copy(username = filtered, errorMessage = null) }
    }

    fun onPhotoPicked(uri: Uri) {
        runCatching {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        _uiState.update { it.copy(photoUri = uri, errorMessage = null) }
    }

    fun onHeightChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 3) {
            _uiState.update { it.copy(heightCm = value, errorMessage = null) }
        }
    }

    fun onWeightChange(value: String) {
        if (value.matches(Regex("^\\d{0,3}([.]\\d{0,1})?$"))) {
            _uiState.update { it.copy(weightKg = value, errorMessage = null) }
        }
    }

    fun onActivityLevelChange(value: String) {
        _uiState.update { it.copy(activityLevel = value, errorMessage = null) }
    }

    /**
     * Auto-formatea la fecha mientras se escribe: el usuario teclea solo dígitos
     * y las barras "/" se insertan solas → DD/MM/AAAA. Así no depende de que el
     * usuario ponga las barras a mano.
     */
    fun onBirthDateChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(8)
        val formatted = buildString {
            digits.forEachIndexed { index, digit ->
                if (index == 2 || index == 4) append('/')
                append(digit)
            }
        }
        _uiState.update { it.copy(birthDate = formatted, errorMessage = null) }
    }

    fun onGoalTypeChange(value: String) {
        _uiState.update { it.copy(goalType = value, errorMessage = null) }
    }

    fun nextStep() {
        val s = _uiState.value
        if (!s.canAdvance) return
        if (s.step >= 6) finish() else _uiState.update { it.copy(step = it.step + 1) }
    }

    fun previousStep() {
        _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    private fun finish() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "No hay sesión activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val s = _uiState.value
                val photoUrl = s.photoUri?.let { uploadProfilePhoto(uid, it) }
                val displayName = auth.currentUser?.displayName.orEmpty()
                val normalizedUsername = UserRepository.normalizeUsername(s.username.trim(), displayName)
                userRepository.saveOnboardingData(
                    uid           = uid,
                    heightCm      = s.heightCm.toIntOrNull(),
                    weightKg      = s.weightKg.toFloatOrNull(),
                    activityLevel = s.activityLevel.ifBlank { null },
                    birthDate     = s.birthDate.ifBlank { null },
                    goalType      = s.goalType.ifBlank { null },
                    username      = normalizedUsername,
                    photoUrl      = photoUrl,
                )
                _uiState.update { it.copy(isSaving = false, finished = true) }
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

    private suspend fun uploadProfilePhoto(uid: String, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val ref = storage.reference.child("users/$uid/profile/avatar.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        }.onFailure { e ->
            android.util.Log.e("OnboardingVM", "Error subiendo foto de perfil: ${e.message}", e)
        }.getOrNull()
    }
}
