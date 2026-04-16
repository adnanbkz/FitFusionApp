package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.data.repository.PostRepository
import com.example.fitfusion.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ProfileUiState(
    // Perfil
    val displayName: String = "Alex Rivera",
    val handle: String = "@alex_kinetic",
    val bio: String = "Elite marathon runner & plant-based nutrition coach. Helping athletes unlock 110% through data-driven performance. 🌿⚡",
    val followers: String = "2.8k FOLLOWERS",
    val following: String = "492 FOLLOWING",
    // Tab
    val selectedTab: Int = 0,
    // Posts
    val userPosts: List<UserPost> = emptyList(),
    // Form crear post
    val showCreatePostSheet: Boolean = false,
    val createPostType: UserPostType = UserPostType.WORKOUT,
    val selectedWorkout: LoggedWorkout? = null,
    val postCaption: String = "",
    val nutritionTitle: String = "",
    val nutritionDesc: String = "",
    val nutritionKcal: String = "",
    // Entrenamientos
    val currentWeekMinutes: List<Int>  = List(7) { 0 },
    val previousWeekMinutes: List<Int> = List(7) { 0 },
    val totalSessionsThisWeek: Int = 0,
    val totalMinutesThisWeek: Int = 0,
    val totalKcalThisWeek: Int = 0,
    val recentWorkouts: List<LoggedWorkout> = emptyList(),
) {
    val postCount: String get() = "${userPosts.size} POSTS"

    val weeklyChange: String get() {
        val cur  = currentWeekMinutes.sum()
        val prev = previousWeekMinutes.sum()
        return when {
            prev == 0 && cur > 0 -> "+100%"
            prev == 0            -> "—"
            else -> {
                val pct = ((cur - prev).toFloat() / prev * 100).toInt()
                if (pct >= 0) "+$pct%" else "$pct%"
            }
        }
    }

    val canPublish: Boolean get() = when (createPostType) {
        UserPostType.WORKOUT    -> selectedWorkout != null
        UserPostType.NUTRITION  -> nutritionTitle.isNotBlank()
    }
}

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            WorkoutRepository.workouts.collect { workoutMap ->
                val today     = LocalDate.now()
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val prevStart = weekStart.minusWeeks(1)

                val curMins  = minutesPerDay(workoutMap, weekStart)
                val prevMins = minutesPerDay(workoutMap, prevStart)

                val thisWeek = (0L..6L).flatMap { offset ->
                    workoutMap[weekStart.plusDays(offset)] ?: emptyList()
                }

                val recent = workoutMap.entries
                    .sortedByDescending { it.key }
                    .flatMap { it.value }
                    .take(6)

                _uiState.update { s ->
                    s.copy(
                        currentWeekMinutes   = curMins,
                        previousWeekMinutes  = prevMins,
                        totalSessionsThisWeek = thisWeek.size,
                        totalMinutesThisWeek  = thisWeek.sumOf { it.durationMinutes },
                        totalKcalThisWeek     = thisWeek.sumOf { it.kcalBurned },
                        recentWorkouts        = recent,
                    )
                }
            }
        }
        viewModelScope.launch {
            PostRepository.posts.collect { posts ->
                _uiState.update { it.copy(userPosts = posts) }
            }
        }
    }

    // ── Perfil ────────────────────────────────────────────────────────────────

    fun updateFromUser(userName: String?) {
        if (userName != null) {
            _uiState.update { it.copy(
                displayName = userName,
                handle      = "@${userName.lowercase()}_kinetic"
            ) }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    // ── Crear post ────────────────────────────────────────────────────────────

    fun showCreatePost() {
        _uiState.update { it.copy(showCreatePostSheet = true) }
    }

    fun dismissCreatePost() {
        _uiState.update { it.copy(
            showCreatePostSheet = false,
            selectedWorkout = null,
            postCaption     = "",
            nutritionTitle  = "",
            nutritionDesc   = "",
            nutritionKcal   = "",
        ) }
    }

    fun setCreatePostType(type: UserPostType) {
        _uiState.update { it.copy(createPostType = type) }
    }

    fun selectWorkout(workout: LoggedWorkout) {
        _uiState.update { it.copy(selectedWorkout = workout) }
    }

    fun onPostCaptionChange(value: String) {
        _uiState.update { it.copy(postCaption = value) }
    }

    fun onNutritionTitleChange(value: String) {
        _uiState.update { it.copy(nutritionTitle = value) }
    }

    fun onNutritionDescChange(value: String) {
        _uiState.update { it.copy(nutritionDesc = value) }
    }

    fun onNutritionKcalChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 5) {
            _uiState.update { it.copy(nutritionKcal = value) }
        }
    }

    fun publishPost() {
        val state = _uiState.value
        if (!state.canPublish) return

        val post = when (state.createPostType) {
            UserPostType.WORKOUT -> {
                val w = state.selectedWorkout ?: return
                UserPost(
                    type                   = UserPostType.WORKOUT,
                    caption                = state.postCaption.ifBlank { w.name },
                    workoutName            = w.name,
                    workoutEmoji           = w.emoji,
                    workoutDurationMinutes = w.durationMinutes,
                    workoutKcal            = w.kcalBurned,
                )
            }
            UserPostType.NUTRITION -> UserPost(
                type          = UserPostType.NUTRITION,
                caption       = state.nutritionTitle,
                nutritionKcal = state.nutritionKcal.toIntOrNull(),
            )
        }

        PostRepository.addPost(post)
        dismissCreatePost()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun minutesPerDay(
        map: Map<LocalDate, List<LoggedWorkout>>,
        weekStart: LocalDate,
    ): List<Int> = (0L..6L).map { offset ->
        val date = weekStart.plusDays(offset)
        map[date]?.sumOf { it.durationMinutes } ?: 0
    }
}
