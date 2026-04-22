package com.example.fitfusion.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.data.repository.FeedRepository
import com.example.fitfusion.data.repository.PostRepository
import com.example.fitfusion.data.repository.UserProfileStore
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
    val profilePhotoUri: Uri? = null,
    val displayName: String = "Alex Rivera",
    val selectedWorkoutDay: LocalDate = LocalDate.now(),
    val selectedDayWorkouts: List<LoggedWorkout> = emptyList(),
    val handle: String = "@alex_fitfusion",
    val bio: String = "Elite marathon runner & plant-based nutrition coach. Helping athletes unlock 110% through data-driven performance.",
    val followers: String = "2.8k",
    val following: String = "492",
    val currentStreak: Int = 0,
    val selectedTab: Int = 0,
    val userPosts: List<UserPost> = emptyList(),
    val showCreatePostSheet: Boolean = false,
    val createPostType: UserPostType = UserPostType.WORKOUT,
    val selectedWorkout: LoggedWorkout? = null,
    val postCaption: String = "",
    val nutritionPhotoUri: Uri? = null,
    val nutritionTitle: String = "",
    val nutritionIngredients: String = "",
    val nutritionInstructions: String = "",
    val nutritionCookTime: String = "",
    val nutritionKcal: String = "",
    val nutritionBestMoment: String = "",
    val currentWeekMinutes: List<Int>  = List(7) { 0 },
    val previousWeekMinutes: List<Int> = List(7) { 0 },
    val totalSessionsThisWeek: Int = 0,
    val totalMinutesThisWeek: Int = 0,
    val totalKcalThisWeek: Int = 0,
    val recentWorkouts: List<LoggedWorkout> = emptyList(),
    val showSearchBar: Boolean = false,
    val searchQuery: String = "",
) {
    val postCount: String get() = userPosts.size.toString()

    val filteredPosts: List<UserPost> get() =
        if (searchQuery.isBlank()) userPosts
        else userPosts.filter { p ->
            val q = searchQuery.trim().lowercase()
            p.caption.lowercase().contains(q) ||
            p.workoutName?.lowercase()?.contains(q) == true ||
            p.workoutEmoji?.contains(q) == true
        }

    val filteredDayWorkouts: List<LoggedWorkout> get() =
        if (searchQuery.isBlank()) selectedDayWorkouts
        else selectedDayWorkouts.filter { it.name.lowercase().contains(searchQuery.trim().lowercase()) }

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

private const val PREFS_NAME = "profile_prefs"
private const val KEY_PHOTO_URI = "profile_photo_uri"

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)

    private var workoutsByDay: Map<LocalDate, List<LoggedWorkout>> = emptyMap()

    init {
        UserProfileStore.ensureInitialized(application)
    }

    private val _uiState = MutableStateFlow(ProfileUiState(
        profilePhotoUri = prefs.getString(KEY_PHOTO_URI, null)?.let { Uri.parse(it) },
        selectedDayWorkouts = WorkoutRepository.getWorkoutsForDate(LocalDate.now())
    ))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun selectWorkoutDay(date: LocalDate) {
        _uiState.update { it.copy(
            selectedWorkoutDay   = date,
            selectedDayWorkouts  = workoutsByDay[date] ?: emptyList()
        ) }
    }

    fun removeWorkoutFromDay(id: String, date: LocalDate) {
        WorkoutRepository.removeWorkout(id, date)
    }

    fun updateProfilePhoto(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }
        prefs.edit { putString(KEY_PHOTO_URI, uri.toString()) }
        UserProfileStore.updatePhotoUri(getApplication(), uri)
        _uiState.update { it.copy(profilePhotoUri = uri) }
    }

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

                var streak = 0
                var cursor  = today
                while ((workoutMap[cursor] ?: emptyList()).isNotEmpty()) {
                    streak++
                    cursor = cursor.minusDays(1)
                }

                workoutsByDay = workoutMap

                _uiState.update { s ->
                    s.copy(
                        currentWeekMinutes    = curMins,
                        previousWeekMinutes   = prevMins,
                        totalSessionsThisWeek = thisWeek.size,
                        totalMinutesThisWeek  = thisWeek.sumOf { it.durationMinutes },
                        totalKcalThisWeek     = thisWeek.sumOf { it.kcalBurned },
                        recentWorkouts        = recent,
                        currentStreak         = streak,
                        selectedDayWorkouts   = workoutMap[s.selectedWorkoutDay] ?: emptyList(),
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

    fun updateFromUser(userName: String?) {
        if (userName != null) {
            _uiState.update { it.copy(
                displayName = userName,
                handle      = "@${userName.lowercase()}_fitfusion"
            ) }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleSearchBar() {
        _uiState.update { it.copy(
            showSearchBar = !it.showSearchBar,
            searchQuery   = if (it.showSearchBar) "" else it.searchQuery
        ) }
    }

    fun onSearchQueryChange(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun showCreatePost() {
        _uiState.update { it.copy(showCreatePostSheet = true) }
    }

    fun dismissCreatePost() {
        _uiState.update { it.copy(
            showCreatePostSheet  = false,
            selectedWorkout      = null,
            postCaption          = "",
            nutritionPhotoUri    = null,
            nutritionTitle       = "",
            nutritionIngredients = "",
            nutritionInstructions = "",
            nutritionCookTime    = "",
            nutritionKcal        = "",
            nutritionBestMoment  = "",
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

    fun onNutritionPhotoChange(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }
        _uiState.update { it.copy(nutritionPhotoUri = uri) }
    }

    fun onNutritionTitleChange(value: String) {
        _uiState.update { it.copy(nutritionTitle = value) }
    }

    fun onNutritionIngredientsChange(value: String) {
        _uiState.update { it.copy(nutritionIngredients = value) }
    }

    fun onNutritionInstructionsChange(value: String) {
        _uiState.update { it.copy(nutritionInstructions = value) }
    }

    fun onNutritionCookTimeChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 3) {
            _uiState.update { it.copy(nutritionCookTime = value) }
        }
    }

    fun onNutritionKcalChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 5) {
            _uiState.update { it.copy(nutritionKcal = value) }
        }
    }

    fun onNutritionBestMomentChange(value: String) {
        _uiState.update { it.copy(nutritionBestMoment = value) }
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
                type                    = UserPostType.NUTRITION,
                caption                 = state.nutritionTitle,
                nutritionPhotoUri       = state.nutritionPhotoUri?.toString(),
                nutritionKcal           = state.nutritionKcal.toIntOrNull(),
                nutritionIngredients    = state.nutritionIngredients.ifBlank { null },
                nutritionInstructions   = state.nutritionInstructions.ifBlank { null },
                nutritionCookTimeMinutes = state.nutritionCookTime.toIntOrNull(),
                nutritionBestMoment     = state.nutritionBestMoment.ifBlank { null },
            )
        }

        PostRepository.addPost(post)
        FeedRepository.addItem(post.toFeedItem(authorName = state.displayName))
        dismissCreatePost()
    }

    private fun minutesPerDay(
        map: Map<LocalDate, List<LoggedWorkout>>,
        weekStart: LocalDate,
    ): List<Int> = (0L..6L).map { offset ->
        val date = weekStart.plusDays(offset)
        map[date]?.sumOf { it.durationMinutes } ?: 0
    }
}
