package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.data.models.RoutineExercise
import com.example.fitfusion.data.repository.ExerciseRepository
import com.example.fitfusion.data.repository.RoutineRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CreateRoutineUiState(
    val name: String        = "",
    val emoji: String       = "💪",
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
) {
    val isValid: Boolean get() = name.isNotBlank() && exercises.isNotEmpty()
}

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
    fun onEmojiChange(v: String)       = _uiState.update { it.copy(emoji = v.takeLast(2).ifBlank { "💪" }) }
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
            emoji        = emojiForMuscleGroup(item.muscleGroup ?: item.primeMoverMuscle),
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

    fun saveRoutine(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        val routine = Routine(
            name                 = state.name.trim(),
            emoji                = state.emoji,
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

private fun emojiForMuscleGroup(group: String?): String = when (group?.lowercase()) {
    "chest", "pecho"             -> "💪"
    "back", "espalda"            -> "🏋️"
    "legs", "quadriceps", "piernas","quads" -> "🦵"
    "shoulders", "hombros"       -> "🤸"
    "arms", "biceps", "triceps"  -> "💪"
    "core", "abs", "abdominales" -> "🔥"
    "glutes", "gluteos"          -> "🍑"
    "cardio"                     -> "🏃"
    else                         -> "💪"
}
