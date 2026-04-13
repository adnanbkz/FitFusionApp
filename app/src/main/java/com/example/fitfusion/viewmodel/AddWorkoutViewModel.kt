package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.repository.ExercisePage
import com.example.fitfusion.data.repository.ExerciseRepository
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
) {
    val availableMuscleGroups: List<String>
        get() = buildList {
            add(ALL_MUSCLE_GROUPS)
            addAll(
                exercises.map { it.displayMuscleGroup }
                    .distinct()
                    .sorted()
            )
        }

    val filteredExercises: List<ExerciseCatalogItem>
        get() = exercises.filter { exercise ->
            selectedMuscleGroup == ALL_MUSCLE_GROUPS ||
                exercise.displayMuscleGroup == selectedMuscleGroup
        }
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
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMore) {
            return
        }
        _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
        fetchPage(append = true)
    }

    private fun fetchPage(append: Boolean) {
        val requestId = ++latestRequestId
        val activeQuery = _uiState.value.searchQuery.trim()

        exerciseRepository.fetchExercisePage(
            searchQuery = activeQuery,
            pageSize = EXERCISE_PAGE_SIZE,
            lastDocument = if (append) lastDocument else null,
            onSuccess = { page ->
                if (requestId != latestRequestId) return@fetchExercisePage
                handlePageResult(page, append)
            },
            onError = { exception ->
                if (requestId != latestRequestId) return@fetchExercisePage
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = exception.localizedMessage
                            ?: "No se pudieron cargar los ejercicios desde Firestore",
                    )
                }
            }
        )
    }

    private fun handlePageResult(
        page: ExercisePage,
        append: Boolean,
    ) {
        lastDocument = page.lastDocument
        _uiState.update { currentState ->
            val mergedExercises = if (append) {
                currentState.exercises + page.exercises
            } else {
                page.exercises
            }
            val availableGroups = buildList {
                add(ALL_MUSCLE_GROUPS)
                addAll(mergedExercises.map { it.displayMuscleGroup }.distinct().sorted())
            }

            currentState.copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = null,
                exercises = mergedExercises,
                hasMore = page.hasMore,
                selectedMuscleGroup = if (currentState.selectedMuscleGroup in availableGroups) {
                    currentState.selectedMuscleGroup
                } else {
                    ALL_MUSCLE_GROUPS
                },
            )
        }
    }
}
