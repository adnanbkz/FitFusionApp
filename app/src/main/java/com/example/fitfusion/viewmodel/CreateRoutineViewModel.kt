package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.ai.AiGenerateRoutineRequest
import com.example.fitfusion.data.ai.AiGenerateRoutineResponse
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.data.models.RoutineExercise
import com.example.fitfusion.data.repository.AiRepository
import com.example.fitfusion.data.repository.ExerciseRepository
import com.example.fitfusion.data.repository.RoutineRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CreateRoutineUiState(
    val name: String        = "",
    val description: String = "",
    val duration: String    = "",
    val isPublic: Boolean   = false,
    val exercises: List<RoutineExercise> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<ExerciseCatalogItem> = emptyList(),
    val isLoadingResults: Boolean = false,
    val editingExerciseIndex: Int? = null,
    val isSaving: Boolean   = false,
    val saveError: String?  = null,
    val showAiSheet: Boolean = false,
    val aiGoal: String       = "Ganar músculo",
    val aiLevel: String      = "Intermedio",
    val aiDaysPerWeek: Int   = 4,
    val aiSessionMinutes: Int = 60,
    val aiEquipment: Set<String> = setOf("Gimnasio completo"),
    val isGeneratingAi: Boolean = false,
    val aiResult: AiGenerateRoutineResponse? = null,
    val aiError: String?     = null,
) {
    val isValid: Boolean get() = name.isNotBlank() && exercises.isNotEmpty()
}

val ROUTINE_GOAL_OPTIONS = listOf("Ganar músculo", "Perder grasa", "Fuerza", "Resistencia", "Tonificar")
val ROUTINE_LEVEL_OPTIONS = listOf("Principiante", "Intermedio", "Avanzado")
val ROUTINE_EQUIPMENT_OPTIONS = listOf(
    "Gimnasio completo",
    "Mancuernas",
    "Barra y discos",
    "Bandas elásticas",
    "Sin equipo",
)

@OptIn(FlowPreview::class)
class CreateRoutineViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(),
    private val routineRepository: RoutineRepository   = RoutineRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRoutineUiState())
    val uiState: StateFlow<CreateRoutineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState
                .map { it.searchQuery }
                .distinctUntilChanged()
                .debounce(300)
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.update { it.copy(searchResults = emptyList(), isLoadingResults = false) }
                        return@collect
                    }
                    _uiState.update { it.copy(isLoadingResults = true) }
                    exerciseRepository.fetchExercisePage(
                        searchQuery = query,
                        pageSize    = 20,
                        onSuccess   = { page ->
                            _uiState.update { it.copy(searchResults = page.exercises, isLoadingResults = false) }
                        },
                        onError     = {
                            _uiState.update { it.copy(searchResults = emptyList(), isLoadingResults = false) }
                        }
                    )
                }
        }
    }

    fun onNameChange(v: String)        = _uiState.update { it.copy(name = v, saveError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onDurationChange(v: String)    = _uiState.update { it.copy(duration = v.filter(Char::isDigit).take(4)) }
    fun onPublicToggle(v: Boolean)     = _uiState.update { it.copy(isPublic = v) }
    fun onSearchQueryChange(v: String) = _uiState.update { it.copy(searchQuery = v) }
    fun clearSearch()                  = _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }

    fun addExercise(item: ExerciseCatalogItem) {
        val exercise = RoutineExercise(
            exerciseId   = item.documentId,
            exerciseName = item.name,
            muscleGroup  = item.muscleGroup ?: item.primeMoverMuscle ?: "",
        )
        _uiState.update { it.copy(
            exercises     = it.exercises + exercise,
            searchQuery   = "",
            searchResults = emptyList(),
        ) }
    }

    fun removeExercise(index: Int) {
        _uiState.update { it.copy(
            exercises = it.exercises.toMutableList().also { l -> if (index in l.indices) l.removeAt(index) }
        ) }
    }

    fun openEditExercise(index: Int) = _uiState.update { it.copy(editingExerciseIndex = index) }
    fun closeEditExercise()          = _uiState.update { it.copy(editingExerciseIndex = null) }

    fun updateExerciseField(
        index: Int,
        sets: Int? = null,
        reps: Int? = null,
        weight: Float? = null,
        rest: Int? = null,
        notes: String? = null,
    ) {
        _uiState.update { state ->
            val list = state.exercises.toMutableList()
            if (index !in list.indices) return@update state
            val curr = list[index]
            list[index] = curr.copy(
                targetSets     = sets   ?: curr.targetSets,
                targetReps     = reps   ?: curr.targetReps,
                targetWeightKg = weight ?: curr.targetWeightKg,
                restSeconds    = rest   ?: curr.restSeconds,
                notes          = notes  ?: curr.notes,
            )
            state.copy(exercises = list)
        }
    }

    fun openAiSheet()    = _uiState.update { it.copy(showAiSheet = true, aiError = null) }
    fun dismissAiSheet() = _uiState.update { it.copy(showAiSheet = false) }
    fun onAiGoalChange(value: String)  = _uiState.update { it.copy(aiGoal = value) }
    fun onAiLevelChange(value: String) = _uiState.update { it.copy(aiLevel = value) }
    fun onAiDaysChange(value: Int)     = _uiState.update { it.copy(aiDaysPerWeek = value.coerceIn(1, 7)) }
    fun onAiSessionChange(value: Int)  = _uiState.update { it.copy(aiSessionMinutes = value.coerceIn(15, 180)) }
    fun toggleAiEquipment(value: String) {
        _uiState.update { state ->
            val updated = state.aiEquipment.toMutableSet().apply {
                if (contains(value)) remove(value) else add(value)
            }
            state.copy(aiEquipment = updated)
        }
    }

    fun generateRoutineWithAi() {
        val state = _uiState.value
        if (state.isGeneratingAi) return
        _uiState.update { it.copy(isGeneratingAi = true, aiError = null) }
        viewModelScope.launch {
            val request = AiGenerateRoutineRequest(
                goalType        = state.aiGoal,
                level           = state.aiLevel,
                daysPerWeek     = state.aiDaysPerWeek,
                sessionMinutes  = state.aiSessionMinutes,
                equipment       = state.aiEquipment.toList(),
            )
            AiRepository.generateRoutine(request)
                .onSuccess { res ->
                    _uiState.update {
                        it.copy(isGeneratingAi = false, aiResult = res, showAiSheet = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isGeneratingAi = false, aiError = e.message ?: "Error consultando la IA")
                    }
                }
        }
    }

    fun applyAiRoutine(replace: Boolean) {
        val res = _uiState.value.aiResult ?: return
        val generated = res.exercises.map { dto ->
            RoutineExercise(
                exerciseId     = "ai-${UUID.randomUUID()}",
                exerciseName   = dto.exerciseName,
                muscleGroup    = dto.muscleGroup,
                targetSets     = dto.targetSets,
                targetReps     = dto.targetReps,
                targetWeightKg = dto.targetWeightKg ?: 0f,
                restSeconds    = dto.restSeconds,
                notes          = dto.notes ?: "",
            )
        }
        _uiState.update { state ->
            state.copy(
                exercises = if (replace) generated else state.exercises + generated,
                name = if (replace || state.name.isBlank()) res.name else state.name,
                description = if (replace || state.description.isBlank()) res.description else state.description,
                duration = if (replace || state.duration.isBlank()) res.estimatedDurationMin.toString() else state.duration,
                aiResult = null,
            )
        }
    }

    fun dismissAiResult() = _uiState.update { it.copy(aiResult = null) }
    fun dismissAiError()  = _uiState.update { it.copy(aiError = null) }

    fun saveRoutine(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        val routine = Routine(
            name                 = state.name.trim(),
            description          = state.description.trim(),
            exercises            = state.exercises,
            estimatedDurationMin = state.duration.toIntOrNull(),
            isPublic             = state.isPublic,
        )
        val authorName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: auth.currentUser?.email?.substringBefore("@")

        routineRepository.save(
            routine    = routine,
            authorName = authorName,
            onSuccess  = {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            },
            onError    = { e ->
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Error al guardar") }
            }
        )
    }
}

