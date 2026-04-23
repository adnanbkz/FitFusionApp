package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.models.WorkoutSet
import com.example.fitfusion.data.repository.AlgoliaExerciseSearchRepository
import com.example.fitfusion.data.repository.AlgoliaSearchPage
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
    val sets: List<EditableSetConfig> = List(3) { EditableSetConfig() },
)

data class EditableSetConfig(
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
    val isRemoteSearchMode: Boolean = false,
    // ── Modo registro ──────────────────────────────────────────────────────
    val isLogMode: Boolean = false,
    val selectedExercises: List<ExerciseCatalogItem> = emptyList(),
    val exerciseConfigs: Map<String, ExerciseConfig> = emptyMap(),
    val showSessionSheet: Boolean = false,
    val isSavingSession: Boolean = false,
    val sessionErrorMessage: String? = null,
    val sessionName: String = "",
    val sessionDurationMinutes: Int = 45,
    val isStopwatchRunning: Boolean = false,
    val stopwatchAccumulatedSeconds: Long = 0L,
    val stopwatchElapsedSeconds: Long = 0L,
    val stopwatchStartedAtMs: Long? = null,
    val workoutStartedAtMs: Long? = null,
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
                    isRemoteSearchMode ||
                    nameFilter.isBlank() ||
                    exercise.nameLower.contains(nameFilter)
                categoryMatch && nameMatch
            }
        }

    val isStopwatchUsed: Boolean
        get() = stopwatchElapsedSeconds > 0L || isStopwatchRunning || workoutStartedAtMs != null

    val resolvedDurationMinutes: Int
        get() = if (isStopwatchUsed) {
            ((stopwatchElapsedSeconds + 59L) / 60L).toInt().coerceAtLeast(1)
        } else {
            sessionDurationMinutes
        }

    val formattedStopwatch: String
        get() {
            val totalSeconds = stopwatchElapsedSeconds
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                "%02d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%02d:%02d".format(minutes, seconds)
            }
        }

    val kcalEstimate: Int get() = (resolvedDurationMinutes * 6.5f).toInt()
}

class AddWorkoutViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(),
    private val algoliaSearchRepository: AlgoliaExerciseSearchRepository =
        AlgoliaExerciseSearchRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWorkoutUiState())
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var stopwatchJob: Job? = null
    private var lastDocument: DocumentSnapshot? = null
    private var nextAlgoliaPage: Int = 0
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
                state.exerciseConfigs + (exercise.documentId to defaultExerciseConfig())
            }
            val autoName = if (state.sessionName.isBlank()) buildSessionName(newSelected) else state.sessionName
            state.copy(
                selectedExercises = newSelected,
                exerciseConfigs = newConfigs,
                sessionName = autoName,
                sessionErrorMessage = null,
            )
        }
    }

    fun showSessionSheet() {
        _uiState.update { it.copy(showSessionSheet = true, sessionErrorMessage = null) }
    }

    fun dismissSessionSheet() {
        _uiState.update { it.copy(showSessionSheet = false, sessionErrorMessage = null) }
    }

    fun updateSessionName(name: String) {
        _uiState.update { it.copy(sessionName = name, sessionErrorMessage = null) }
    }

    fun incrementDuration() {
        _uiState.update {
            it.copy(
                sessionDurationMinutes = (it.sessionDurationMinutes + 5).coerceAtMost(240),
                sessionErrorMessage = null,
            )
        }
    }

    fun decrementDuration() {
        _uiState.update {
            it.copy(
                sessionDurationMinutes = (it.sessionDurationMinutes - 5).coerceAtLeast(5),
                sessionErrorMessage = null,
            )
        }
    }

    fun startStopwatch() {
        val currentState = _uiState.value
        if (currentState.isStopwatchRunning) return

        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                isStopwatchRunning = true,
                stopwatchStartedAtMs = now,
                workoutStartedAtMs = it.workoutStartedAtMs ?: now,
                sessionErrorMessage = null,
            )
        }
        startStopwatchTicker()
    }

    fun pauseStopwatch() {
        val now = System.currentTimeMillis()
        stopwatchJob?.cancel()
        stopwatchJob = null
        _uiState.update { state ->
            val elapsedSeconds = currentStopwatchElapsedSeconds(state, now)
            state.copy(
                isStopwatchRunning = false,
                stopwatchAccumulatedSeconds = elapsedSeconds,
                stopwatchElapsedSeconds = elapsedSeconds,
                stopwatchStartedAtMs = null,
                sessionErrorMessage = null,
            )
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        stopwatchJob = null
        _uiState.update {
            it.copy(
                isStopwatchRunning = false,
                stopwatchAccumulatedSeconds = 0L,
                stopwatchElapsedSeconds = 0L,
                stopwatchStartedAtMs = null,
                workoutStartedAtMs = null,
                sessionErrorMessage = null,
            )
        }
    }

    fun addSet(documentId: String) {
        updateExerciseConfig(documentId) { config ->
            if (config.sets.size >= 10) config
            else config.copy(sets = config.sets + (config.sets.lastOrNull() ?: EditableSetConfig()))
        }
    }

    fun removeSet(documentId: String, setIndex: Int) {
        updateExerciseConfig(documentId) { config ->
            if (config.sets.size <= 1 || setIndex !in config.sets.indices) config
            else config.copy(sets = config.sets.filterIndexed { index, _ -> index != setIndex })
        }
    }

    fun updateSetReps(documentId: String, setIndex: Int, reps: Int) {
        updateExerciseConfig(documentId) { config ->
            config.updateSet(setIndex) { it.copy(reps = reps.coerceIn(1, 50)) }
        }
    }

    fun updateSetWeight(documentId: String, setIndex: Int, weightKg: Int) {
        updateExerciseConfig(documentId) { config ->
            config.updateSet(setIndex) { it.copy(weightKg = weightKg.coerceIn(0, 300)) }
        }
    }

    fun saveSession(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.selectedExercises.isEmpty() || state.isSavingSession) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSession = true, sessionErrorMessage = null) }
            try {
                val endedAtMs = System.currentTimeMillis()
                val elapsedSeconds = currentStopwatchElapsedSeconds(state, endedAtMs)
                val resolvedDurationMinutes = if (state.isStopwatchUsed) {
                    ((elapsedSeconds + 59L) / 60L).toInt().coerceAtLeast(1)
                } else {
                    state.sessionDurationMinutes
                }
                val startedAtMs = if (state.isStopwatchUsed) {
                    state.workoutStartedAtMs ?: (endedAtMs - elapsedSeconds * 1000L)
                } else {
                    endedAtMs - resolvedDurationMinutes * 60_000L
                }
                val resolvedEndedAtMs = if (state.isStopwatchUsed) {
                    startedAtMs + elapsedSeconds * 1000L
                } else {
                    endedAtMs
                }
                val exercises = state.selectedExercises.map { exercise ->
                    val cfg = state.exerciseConfigs[exercise.documentId] ?: defaultExerciseConfig()
                    WorkoutExercise(
                        exerciseDocumentId = exercise.documentId,
                        exerciseSlug = exercise.exerciseId,
                        name = exercise.name,
                        muscleGroup = MuscleTranslations.translate(exercise.displayMuscleGroup),
                        sets = cfg.sets.map { set ->
                            WorkoutSet(reps = set.reps, weightKg = set.weightKg.toFloat())
                        }
                    )
                }

                WorkoutRepository.addWorkout(
                    LoggedWorkout(
                        date = LocalDate.now(),
                        name = state.sessionName.ifBlank { "Entrenamiento" },
                        durationMinutes = resolvedDurationMinutes,
                        kcalBurned = (resolvedDurationMinutes * 6.5f).toInt(),
                        startedAtMs = startedAtMs,
                        endedAtMs = resolvedEndedAtMs,
                        createdAtMs = endedAtMs,
                        exercises = exercises,
                    )
                )

                stopwatchJob?.cancel()
                stopwatchJob = null
                _uiState.update {
                    it.copy(
                        showSessionSheet = false,
                        isSavingSession = false,
                        sessionErrorMessage = null,
                        isStopwatchRunning = false,
                        stopwatchAccumulatedSeconds = 0L,
                        stopwatchElapsedSeconds = 0L,
                        stopwatchStartedAtMs = null,
                        workoutStartedAtMs = null,
                    )
                }
                onDone()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingSession = false,
                        sessionErrorMessage = exception.localizedMessage
                            ?: "No se pudo guardar el entrenamiento",
                    )
                }
            }
        }
    }

    private fun buildSessionName(exercises: List<ExerciseCatalogItem>): String {
        if (exercises.isEmpty()) return ""
        val groups = exercises.map { MuscleTranslations.translate(it.filterMuscleGroup) }
            .distinct().take(2)
        return "Fuerza — ${groups.joinToString(" + ")}"
    }

    private fun startStopwatchTicker() {
        stopwatchJob?.cancel()
        stopwatchJob = viewModelScope.launch {
            while (true) {
                _uiState.update { state ->
                    if (!state.isStopwatchRunning) return@update state
                    val elapsedSeconds = currentStopwatchElapsedSeconds(state, System.currentTimeMillis())
                    state.copy(stopwatchElapsedSeconds = elapsedSeconds)
                }
                delay(1_000L)
            }
        }
    }

    private fun currentStopwatchElapsedSeconds(
        state: AddWorkoutUiState,
        nowMs: Long,
    ): Long {
        val runningSeconds = if (state.isStopwatchRunning && state.stopwatchStartedAtMs != null) {
            ((nowMs - state.stopwatchStartedAtMs) / 1000L).coerceAtLeast(0L)
        } else {
            0L
        }
        return state.stopwatchAccumulatedSeconds + runningSeconds
    }

    private fun updateExerciseConfig(
        documentId: String,
        transform: (ExerciseConfig) -> ExerciseConfig,
    ) {
        _uiState.update { currentState ->
            val currentConfig = currentState.exerciseConfigs[documentId] ?: defaultExerciseConfig()
            currentState.copy(
                exerciseConfigs = currentState.exerciseConfigs + (documentId to transform(currentConfig)),
                sessionErrorMessage = null,
            )
        }
    }

    private fun defaultExerciseConfig(): ExerciseConfig = ExerciseConfig()

    private fun ExerciseConfig.updateSet(
        setIndex: Int,
        transform: (EditableSetConfig) -> EditableSetConfig,
    ): ExerciseConfig {
        if (setIndex !in sets.indices) return this
        return copy(
            sets = sets.mapIndexed { index, currentSet ->
                if (index == setIndex) transform(currentSet) else currentSet
            }
        )
    }

    override fun onCleared() {
        stopwatchJob?.cancel()
        stopwatchJob = null
        super.onCleared()
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
        nextAlgoliaPage = 0
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                isLoadingMore = false,
                errorMessage = null,
                exercises = emptyList(),
                hasMore = false,
                isRemoteSearchMode = false,
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
        } else if (currentState.searchQuery.trim().isNotBlank() && algoliaSearchRepository.isConfigured) {
            val pageToLoad = if (append) nextAlgoliaPage else 0
            viewModelScope.launch {
                runCatching {
                    algoliaSearchRepository.searchExercises(
                        query = currentState.searchQuery.trim(),
                        page = pageToLoad,
                        hitsPerPage = EXERCISE_PAGE_SIZE,
                    )
                }.onSuccess { algoliaPage ->
                    if (requestId != latestRequestId) return@onSuccess
                    if (algoliaPage.documentIds.isEmpty()) {
                        handleAlgoliaPageResult(algoliaPage, emptyList(), append)
                        return@onSuccess
                    }
                    exerciseRepository.fetchExercisesByDocumentIds(
                        documentIds = algoliaPage.documentIds,
                        onSuccess = { exercises ->
                            if (requestId != latestRequestId) return@fetchExercisesByDocumentIds
                            handleAlgoliaPageResult(algoliaPage, exercises, append)
                        },
                        onError = ::onError,
                    )
                }.onFailure { throwable ->
                    onError(throwable as? Exception ?: Exception(throwable))
                }
            }
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
                isRemoteSearchMode = false,
                selectedMuscleGroup = if (currentState.selectedMuscleGroup in availableGroups)
                    currentState.selectedMuscleGroup else ALL_MUSCLE_GROUPS,
            )
        }
    }

    private fun handleAlgoliaPageResult(
        page: AlgoliaSearchPage,
        exercises: List<ExerciseCatalogItem>,
        append: Boolean,
    ) {
        nextAlgoliaPage = page.page + 1
        _uiState.update { currentState ->
            val mergedExercises = if (append) {
                (currentState.exercises + exercises).distinctBy { it.documentId }
            } else {
                exercises
            }
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
                isRemoteSearchMode = true,
                selectedMuscleGroup = if (currentState.selectedMuscleGroup in availableGroups)
                    currentState.selectedMuscleGroup else ALL_MUSCLE_GROUPS,
            )
        }
    }
}
