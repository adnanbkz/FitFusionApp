package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.models.WorkoutSet
import com.example.fitfusion.data.repository.ExercisePage
import com.example.fitfusion.data.repository.ExerciseRepository
import com.example.fitfusion.data.repository.WorkoutRepository
import com.example.fitfusion.util.MuscleTranslations
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val ALL_MUSCLE_GROUPS = "Todos"
private const val EXERCISE_PAGE_SIZE = 40
private const val SEARCH_DEBOUNCE_MS = 350L

data class ExerciseConfig(
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Int = 0,
)

data class AddWorkoutUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedMuscleGroup: String = ALL_MUSCLE_GROUPS,
    val exercises: List<ExerciseCatalogItem> = emptyList(),
    val hasMore: Boolean = false,
    // ── Modo registro ──────────────────────────────────────────────────────
    val isLogMode: Boolean = false,
    val selectedExercises: List<ExerciseCatalogItem> = emptyList(),
    val exerciseConfigs: Map<String, ExerciseConfig> = emptyMap(),
    val showSessionSheet: Boolean = false,
    val sessionName: String = "",
    val sessionDurationMinutes: Int = 45,
) {
    val availableMuscleGroups: List<String>
        get() = buildList {
            add(ALL_MUSCLE_GROUPS)
            addAll(
                exercises.map { it.filterMuscleGroup }
                    .distinct()
                    .sorted()
            )
        }

    val activeCategoryKey: String?
        get() {
            if (selectedMuscleGroup != ALL_MUSCLE_GROUPS) return selectedMuscleGroup
            val normalized = searchQuery.trim().lowercase()
            if (normalized.isBlank()) return null
            return availableMuscleGroups
                .filter { it != ALL_MUSCLE_GROUPS }
                .firstOrNull { group ->
                    MuscleTranslations.translate(group).lowercase().contains(normalized)
                }
        }

    val filteredExercises: List<ExerciseCatalogItem>
        get() {
            val nameFilter = searchQuery.trim().lowercase()
            val isCategorySearch = activeCategoryKey != null &&
                selectedMuscleGroup == ALL_MUSCLE_GROUPS
            return exercises.filter { exercise ->
                val categoryMatch = selectedMuscleGroup == ALL_MUSCLE_GROUPS ||
                    exercise.filterMuscleGroup == selectedMuscleGroup
                val nameMatch = isCategorySearch ||
                    nameFilter.isBlank() ||
                    exercise.nameLower.contains(nameFilter)
                categoryMatch && nameMatch
            }
        }

    val kcalEstimate: Int get() = (sessionDurationMinutes * 6.5f).toInt()
}

class AddWorkoutViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWorkoutUiState())
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var lastDocument: DocumentSnapshot? = null
    private var latestRequestId: Long = 0

    init {
        refreshExercises()
    }

    // ── Modo registro ──────────────────────────────────────────────────────────

    fun setLogMode(enabled: Boolean) {
        _uiState.update { it.copy(isLogMode = enabled) }
    }

    fun toggleExercise(exercise: ExerciseCatalogItem) {
        _uiState.update { state ->
            val isAlreadySelected = state.selectedExercises.any { it.documentId == exercise.documentId }
            val newSelected = if (isAlreadySelected) {
                state.selectedExercises.filter { it.documentId != exercise.documentId }
            } else {
                state.selectedExercises + exercise
            }
            val newConfigs = if (isAlreadySelected) {
                state.exerciseConfigs - exercise.documentId
            } else {
                state.exerciseConfigs + (exercise.documentId to ExerciseConfig())
            }
            val autoName = if (state.sessionName.isBlank()) buildSessionName(newSelected) else state.sessionName
            state.copy(
                selectedExercises = newSelected,
                exerciseConfigs   = newConfigs,
                sessionName       = autoName,
            )
        }
    }

    fun showSessionSheet() {
        _uiState.update { it.copy(showSessionSheet = true) }
    }

    fun dismissSessionSheet() {
        _uiState.update { it.copy(showSessionSheet = false) }
    }

    fun updateSessionName(name: String) {
        _uiState.update { it.copy(sessionName = name) }
    }

    fun incrementDuration() {
        _uiState.update { it.copy(sessionDurationMinutes = (it.sessionDurationMinutes + 5).coerceAtMost(240)) }
    }

    fun decrementDuration() {
        _uiState.update { it.copy(sessionDurationMinutes = (it.sessionDurationMinutes - 5).coerceAtLeast(5)) }
    }

    fun updateExerciseConfig(documentId: String, config: ExerciseConfig) {
        _uiState.update { it.copy(exerciseConfigs = it.exerciseConfigs + (documentId to config)) }
    }

    fun saveSession(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.selectedExercises.isEmpty()) return

        val exercises = state.selectedExercises.map { exercise ->
            val cfg = state.exerciseConfigs[exercise.documentId] ?: ExerciseConfig()
            WorkoutExercise(
                name         = exercise.name,
                muscleGroup  = MuscleTranslations.translate(exercise.displayMuscleGroup),
                sets         = List(cfg.sets) { WorkoutSet(reps = cfg.reps, weightKg = cfg.weightKg.toFloat()) }
            )
        }

        WorkoutRepository.addWorkout(
            LoggedWorkout(
                date            = LocalDate.now(),
                name            = state.sessionName.ifBlank { "Entrenamiento" },
                durationMinutes = state.sessionDurationMinutes,
                kcalBurned      = state.kcalEstimate,
                exercises       = exercises,
            )
        )
        onDone()
    }

    private fun buildSessionName(exercises: List<ExerciseCatalogItem>): String {
        if (exercises.isEmpty()) return ""
        val groups = exercises.map { MuscleTranslations.translate(it.filterMuscleGroup) }
            .distinct().take(2)
        return "Fuerza — ${groups.joinToString(" + ")}"
    }

    // ── Carga de ejercicios (sin cambios) ──────────────────────────────────────

    fun onSearchQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            refreshExercises()
        }
    }

    fun onMuscleGroupSelected(value: String) {
        _uiState.update { it.copy(selectedMuscleGroup = value) }
        refreshExercises()
    }

    fun clearSearchQuery() {
        searchJob?.cancel()
        _uiState.update { it.copy(searchQuery = "") }
        refreshExercises()
    }

    fun refreshExercises() {
        lastDocument = null
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                isLoadingMore = false,
                errorMessage = null,
                exercises = emptyList(),
                hasMore = false,
                selectedMuscleGroup = if (
                    currentState.selectedMuscleGroup in currentState.availableMuscleGroups
                ) currentState.selectedMuscleGroup else ALL_MUSCLE_GROUPS,
            )
        }
        fetchPage(append = false)
    }

    fun loadMoreExercises() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMore) return
        _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
        fetchPage(append = true)
    }

    private fun fetchPage(append: Boolean) {
        val requestId = ++latestRequestId
        val currentState = _uiState.value
        val categoryKey = currentState.activeCategoryKey

        fun onError(exception: Exception) {
            if (requestId != latestRequestId) return
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    errorMessage = exception.localizedMessage
                        ?: "No se pudieron cargar los ejercicios desde Firestore",
                )
            }
        }

        if (categoryKey != null && !append) {
            exerciseRepository.fetchExercisesForCategory(
                muscleGroupKey = categoryKey,
                pageSize = EXERCISE_PAGE_SIZE * 5,
                onSuccess = { page ->
                    if (requestId != latestRequestId) return@fetchExercisesForCategory
                    handlePageResult(page, append = false)
                },
                onError = ::onError,
            )
        } else {
            exerciseRepository.fetchExercisePage(
                searchQuery = if (categoryKey == null) currentState.searchQuery.trim() else "",
                pageSize = EXERCISE_PAGE_SIZE,
                lastDocument = if (append) lastDocument else null,
                onSuccess = { page ->
                    if (requestId != latestRequestId) return@fetchExercisePage
                    handlePageResult(page, append)
                },
                onError = ::onError,
            )
        }
    }

    private fun handlePageResult(page: ExercisePage, append: Boolean) {
        lastDocument = page.lastDocument
        _uiState.update { currentState ->
            val mergedExercises = if (append) currentState.exercises + page.exercises
                                  else page.exercises
            val availableGroups = buildList {
                add(ALL_MUSCLE_GROUPS)
                addAll(mergedExercises.map { it.filterMuscleGroup }.distinct().sorted())
            }
            currentState.copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = null,
                exercises = mergedExercises,
                hasMore = page.hasMore,
                selectedMuscleGroup = if (currentState.selectedMuscleGroup in availableGroups)
                    currentState.selectedMuscleGroup else ALL_MUSCLE_GROUPS,
            )
        }
    }
}
