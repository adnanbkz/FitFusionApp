package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: Int = 0,
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
        0    -> heightCm.toIntOrNull()?.let { it in 80..260 } == true
        1    -> weightKg.toFloatOrNull()?.let { it in 25f..300f } == true
        2    -> activityLevel.isNotBlank()
        3    -> birthDate.isNotBlank()
        4    -> goalType.isNotBlank()
        else -> false
    }
}

class OnboardingViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

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

    fun onBirthDateChange(value: String) {
        _uiState.update { it.copy(birthDate = value, errorMessage = null) }
    }

    fun onGoalTypeChange(value: String) {
        _uiState.update { it.copy(goalType = value, errorMessage = null) }
    }

    fun nextStep() {
        val s = _uiState.value
        if (!s.canAdvance) return
        if (s.step >= 4) finish() else _uiState.update { it.copy(step = it.step + 1) }
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
                userRepository.saveOnboardingData(
                    uid           = uid,
                    heightCm      = s.heightCm.toIntOrNull(),
                    weightKg      = s.weightKg.toFloatOrNull(),
                    activityLevel = s.activityLevel.ifBlank { null },
                    birthDate     = s.birthDate.ifBlank { null },
                    goalType      = s.goalType.ifBlank { null },
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
}
