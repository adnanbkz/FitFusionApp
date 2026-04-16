package com.example.fitfusion.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Músculos disponibles para seleccionar (claves en inglés/latín, se traducen en la UI). */
val MUSCLE_OPTIONS: List<String> = listOf(
    // Pecho
    "pectoralis major", "pectoralis minor", "serratus anterior",
    // Espalda
    "latissimus dorsi", "trapezius", "rhomboids", "erector spinae",
    "teres major", "teres minor", "infraspinatus", "supraspinatus",
    // Hombros
    "anterior deltoid", "lateral deltoid", "posterior deltoid",
    // Brazos
    "biceps brachii", "triceps brachii", "brachialis", "brachioradialis",
    "wrist flexors", "wrist extensors",
    // Core
    "rectus abdominis", "obliques", "transversus abdominis",
    // Piernas
    "quadriceps", "hamstrings", "adductors", "abductors",
    "hip flexors", "sartorius", "tensor fasciae latae",
    // Glúteos
    "gluteus maximus", "gluteus medius", "gluteus minimus",
    // Pantorrillas
    "gastrocnemius", "soleus", "tibialis anterior",
)

/** Opciones de equipamiento predefinidas. */
val EQUIPMENT_OPTIONS: List<String> = listOf(
    "Bodyweight", "Dumbbells", "Barbell", "Cables",
    "Machine", "Kettlebell", "Resistance Band", "Pull-up Bar", "Bench", "Other",
)

data class CreateExerciseUiState(
    val name: String = "",
    val photoUri: Uri? = null,
    val primeMoverMuscle: String? = null,
    val secondaryMuscle: String? = null,
    val tertiaryMuscle: String? = null,
    val showSecondaryMuscle: Boolean = false,
    val showTertiaryMuscle: Boolean = false,
    val selectedEquipment: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
) {
    val isValid: Boolean
        get() = name.isNotBlank() && primeMoverMuscle != null
}

class CreateExerciseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateExerciseUiState())
    val uiState: StateFlow<CreateExerciseUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, saveError = null) }
    }

    fun onPhotoSelected(uri: Uri?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun onPrimeMoverSelected(muscle: String?) {
        _uiState.update { it.copy(primeMoverMuscle = muscle) }
    }

    fun onSecondaryMuscleSelected(muscle: String?) {
        _uiState.update { it.copy(secondaryMuscle = muscle) }
    }

    fun onTertiaryMuscleSelected(muscle: String?) {
        _uiState.update { it.copy(tertiaryMuscle = muscle) }
    }

    fun onShowSecondaryMuscle() {
        _uiState.update { it.copy(showSecondaryMuscle = true) }
    }

    fun onShowTertiaryMuscle() {
        _uiState.update { it.copy(showTertiaryMuscle = true) }
    }

    fun onEquipmentToggle(equipment: String) {
        _uiState.update { current ->
            val updated = if (equipment in current.selectedEquipment) {
                current.selectedEquipment - equipment
            } else {
                current.selectedEquipment + equipment
            }
            current.copy(selectedEquipment = updated)
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        // TODO: integrar con Firestore
        onSuccess()
    }
}
