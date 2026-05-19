package com.example.fitfusion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.Recipe
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.data.repository.RecipeRepository
import com.example.fitfusion.data.repository.UserProfile
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class SearchCategory(val label: String) {
    PROFILES("Perfiles"),
    WORKOUTS("Entrenos"),
    RECIPES("Recetas"),
}

data class UserSearchUiState(
    val query: String = "",
    val category: SearchCategory = SearchCategory.PROFILES,
    val profileResults: List<UserProfile> = emptyList(),
    val workoutResults: List<UserPost> = emptyList(),
    val recipeResults: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
) {
    val results: List<UserProfile> get() = profileResults
}

class UserSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository()
    private val recipeRepo = RecipeRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(UserSearchUiState())
    val uiState: StateFlow<UserSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var blockedUids: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            val uid = currentUid ?: return@launch
            blockedUids = runCatching { userRepo.getBlockedUsers(uid) }
                .getOrDefault(emptyList())
                .map { it.uid }
                .toSet()
        }
    }

    fun onCategoryChange(category: SearchCategory) {
        if (_uiState.value.category == category) return
        _uiState.update {
            it.copy(
                category       = category,
                profileResults = emptyList(),
                workoutResults = emptyList(),
                recipeResults  = emptyList(),
                hasSearched    = false,
                isLoading      = false,
                errorMessage   = null,
            )
        }
        val q = _uiState.value.query
        if (q.isNotBlank()) triggerSearch(q)
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value, errorMessage = null) }
        searchJob?.cancel()
        if (value.isBlank()) {
            _uiState.update {
                it.copy(
                    profileResults = emptyList(),
                    workoutResults = emptyList(),
                    recipeResults  = emptyList(),
                    isLoading      = false,
                    hasSearched    = false,
                )
            }
            return
        }
        triggerSearch(value)
    }

    private fun triggerSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (_uiState.value.category) {
                SearchCategory.PROFILES -> searchProfiles(query)
                SearchCategory.WORKOUTS -> searchWorkouts(query)
                SearchCategory.RECIPES  -> searchRecipes(query)
            }
        }
    }

    private suspend fun searchProfiles(query: String) {
        val result = runCatching { userRepo.searchUsers(query, currentUid) }
        result.onSuccess { all ->
            val filtered = all.filter { it.uid !in blockedUids }
            _uiState.update {
                it.copy(profileResults = filtered, isLoading = false, hasSearched = true, errorMessage = null)
            }
        }
        result.onFailure { e ->
            _uiState.update {
                it.copy(profileResults = emptyList(), isLoading = false, hasSearched = true, errorMessage = e.localizedMessage)
            }
        }
    }

    private suspend fun searchWorkouts(query: String) {
        val q = query.trim().lowercase()
        val result = runCatching {
            // whereEqualTo + orderBy en campos distintos exige índice compuesto:
            // lo evitamos omitiendo orderBy y ordenando client-side.
            firestore.collection("posts")
                .whereEqualTo("type", "workout")
                .limit(100)
                .get().await()
                .documents
                .mapNotNull { doc ->
                    val authorId = doc.getString("authorId") ?: return@mapNotNull null
                    if (authorId in blockedUids) return@mapNotNull null
                    val workoutName = doc.getString("workoutName").orEmpty()
                    val caption     = doc.getString("caption").orEmpty()
                    if (!workoutName.lowercase().contains(q) && !caption.lowercase().contains(q)) return@mapNotNull null
                    UserPost(
                        id                     = doc.id,
                        type                   = UserPostType.WORKOUT,
                        caption                = caption,
                        workoutName            = workoutName,
                        workoutDurationMinutes = doc.getLong("workoutDurationMinutes")?.toInt(),
                        workoutKcal            = doc.getLong("workoutKcal")?.toInt(),
                        workoutTotalWeightKg   = (doc.get("workoutTotalWeightKg") as? Number)?.toFloat(),
                        workoutMediaUrls       = (doc.get("workoutMediaUrls") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
                        timestamp              = doc.getLong("createdAtMs") ?: 0L,
                    )
                }
                .sortedByDescending { it.timestamp }
        }
        result.onSuccess { posts ->
            _uiState.update {
                it.copy(workoutResults = posts, isLoading = false, hasSearched = true, errorMessage = null)
            }
        }
        result.onFailure { e ->
            _uiState.update {
                it.copy(workoutResults = emptyList(), isLoading = false, hasSearched = true, errorMessage = e.localizedMessage)
            }
        }
    }

    private suspend fun searchRecipes(query: String) {
        val result = runCatching { recipeRepo.searchCommunity(query) }
        result.onSuccess { recipes ->
            _uiState.update {
                it.copy(recipeResults = recipes, isLoading = false, hasSearched = true, errorMessage = null)
            }
        }
        result.onFailure { e ->
            _uiState.update {
                it.copy(recipeResults = emptyList(), isLoading = false, hasSearched = true, errorMessage = e.localizedMessage)
            }
        }
    }
}
