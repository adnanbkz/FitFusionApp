package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.repository.AlgoliaExerciseSearchRepository
import com.example.fitfusion.data.repository.AlgoliaSearchPage
import com.example.fitfusion.data.repository.ExercisePage
import com.example.fitfusion.data.repository.ExerciseRepository
import com.example.fitfusion.data.workout.ActiveWorkoutManager
import com.example.fitfusion.util.MuscleTranslations
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ALL_MUSCLE_GROUPS = "Todos"
private const val EXERCISE_PAGE_SIZE = 40
private const val SEARCH_DEBOUNCE_MS = 350L

data class AddWorkoutUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedMuscleGroup: String = ALL_MUSCLE_GROUPS,
    val exercises: List<ExerciseCatalogItem> = emptyList(),
    val hasMore: Boolean = false,
    val isRemoteSearchMode: Boolean = false,
    val isLogMode: Boolean = false,
    val selectedExercises: List<ExerciseCatalogItem> = emptyList(),
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

    val proposedSessionName: String
        get() {
            if (selectedExercises.isEmpty()) return ""
            val groups = selectedExercises.map { MuscleTranslations.translate(it.filterMuscleGroup) }
                .distinct().take(2)
            return "Fuerza — ${groups.joinToString(" + ")}"
        }
}

class AddWorkoutViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(),
    private val algoliaSearchRepository: AlgoliaExerciseSearchRepository =
        AlgoliaExerciseSearchRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWorkoutUiState())
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var lastDocument: DocumentSnapshot? = null
    private var nextAlgoliaPage: Int = 0
    private var latestRequestId: Long = 0

    init {
        refreshExercises()
    }

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
            state.copy(selectedExercises = newSelected)
        }
    }

    fun startSession(): Boolean {
        val state = _uiState.value
        if (state.selectedExercises.isEmpty()) return false
        ActiveWorkoutManager.startSession(
            name      = state.proposedSessionName,
            exercises = state.selectedExercises,
        )
        _uiState.update { it.copy(selectedExercises = emptyList()) }
        return true
    }

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
