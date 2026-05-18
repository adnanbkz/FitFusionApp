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
import com.example.fitfusion.data.repository.UserRepository
import com.example.fitfusion.data.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ProfileUiState(
    val profilePhotoUri: Uri? = null,
    val displayName: String = "Usuario",
    val selectedWorkoutDay: LocalDate = LocalDate.now(),
    val selectedDayWorkouts: List<LoggedWorkout> = emptyList(),
    val handle: String = "@usuario",
    val bio: String = "Edita tu perfil para contar tus objetivos y progreso.",
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val goalType: String? = null,
    val activityLevel: String? = null,
    val followers: String = "0",
    val following: String = "0",
    val currentStreak: Int = 0,
    val selectedTab: Int = 0,
    val userPosts: List<UserPost> = emptyList(),
    val savedFeedItems: List<FeedItem> = emptyList(),
    val showCreatePostSheet: Boolean = false,
    val isPublishingPost: Boolean = false,
    val createPostErrorMessage: String? = null,
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
    val capturedVideoUri: Uri? = null,
    val currentWeekMinutes: List<Int>  = List(7) { 0 },
    val previousWeekMinutes: List<Int> = List(7) { 0 },
    val totalSessionsThisWeek: Int = 0,
    val totalMinutesThisWeek: Int = 0,
    val totalKcalThisWeek: Int = 0,
    val recentWorkouts: List<LoggedWorkout> = emptyList(),
    val likedFeedItems: List<FeedItem> = emptyList(),
    val isLoadingLikedPosts: Boolean = false,
) {
    val postCount: String get() = userPosts.size.toString()

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
        UserPostType.WORKOUT    -> !isPublishingPost && selectedWorkout != null
        UserPostType.NUTRITION  -> !isPublishingPost && nutritionTitle.isNotBlank()
    }
}

private const val PREFS_NAME = "profile_prefs"
private const val KEY_PHOTO_URI = "profile_photo_uri"

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userRepository = UserRepository()
    private var profileListenerRegistration: ListenerRegistration? = null
    private var followersListenerRegistration: ListenerRegistration? = null
    private var followingListenerRegistration: ListenerRegistration? = null

    private var workoutsByDay: Map<LocalDate, List<LoggedWorkout>> = emptyMap()

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

    fun updateWorkout(
        workout: LoggedWorkout,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                WorkoutRepository.updateWorkout(workout)
                onSuccess()
            } catch (exception: Exception) {
                onError(exception.localizedMessage ?: "No se pudo actualizar el entrenamiento")
            }
        }
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
        UserProfileStore.ensureInitialized(application)
        attachUserProfileListener()
        attachFollowCountListeners()

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
                        selectedWorkout       = s.selectedWorkout?.let { sel ->
                            recent.firstOrNull { it.id == sel.id } ?: sel
                        },
                    )
                }
            }
        }
        viewModelScope.launch {
            PostRepository.posts.collect { posts ->
                _uiState.update { it.copy(userPosts = posts) }
            }
        }
        viewModelScope.launch {
            FeedRepository.ensureInitialized()
            FeedRepository.likedPosts.collect { liked ->
                _uiState.update { it.copy(likedFeedItems = liked, isLoadingLikedPosts = false) }
            }
        }
        viewModelScope.launch {
            FeedRepository.items.collect { items ->
                _uiState.update { state ->
                    state.copy(savedFeedItems = items.filter { item ->
                        when (item) {
                            is FeedItem.Workout   -> item.post.isSaved
                            is FeedItem.Nutrition -> item.post.isSaved
                        }
                    })
                }
            }
        }
        _uiState.update { it.copy(isLoadingLikedPosts = true) }
    }

    fun updateFromUser(userName: String?) {
        if (userName != null) {
            _uiState.update { it.copy(displayName = userName) }
        }
    }

    private fun attachUserProfileListener() {
        val user = auth.currentUser ?: return
        profileListenerRegistration?.remove()
        profileListenerRegistration = userRepository.listenUserProfile(
            uid = user.uid,
            fallbackEmail = user.email.orEmpty(),
            onProfile = { profile ->
                if (profile == null) return@listenUserProfile
                _uiState.update {
                    it.copy(
                        displayName = profile.displayName,
                        handle = profile.username,
                        bio = profile.bio.ifBlank { "Edita tu perfil para contar tus objetivos y progreso." },
                        heightCm = profile.heightCm,
                        weightKg = profile.weightKg,
                        goalType = profile.goalType,
                        activityLevel = profile.activityLevel,
                    )
                }
            },
        )
    }

    /**
     * Contadores de seguidores/seguidos en vivo: cuenta el tamaño de las
     * subcolecciones followers/following. El doc de usuario tiene
     * followersCount/followingCount pero nadie los mantiene al día.
     */
    private fun attachFollowCountListeners() {
        val uid = auth.currentUser?.uid ?: return
        followersListenerRegistration?.remove()
        followingListenerRegistration?.remove()
        followersListenerRegistration = firestore.collection("users").document(uid)
            .collection("followers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.update { it.copy(followers = compactCount(snapshot.size())) }
                }
            }
        followingListenerRegistration = firestore.collection("users").document(uid)
            .collection("following")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _uiState.update { it.copy(following = compactCount(snapshot.size())) }
                }
            }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun showCreatePost() {
        _uiState.update { it.copy(showCreatePostSheet = true) }
    }

    fun showCreatePostWithMedia(uri: Uri, isVideo: Boolean) {
        _uiState.update {
            if (isVideo) {
                it.copy(
                    showCreatePostSheet = true,
                    createPostType      = UserPostType.WORKOUT,
                    capturedVideoUri    = uri,
                )
            } else {
                it.copy(
                    showCreatePostSheet = true,
                    createPostType      = UserPostType.NUTRITION,
                    nutritionPhotoUri   = uri,
                )
            }
        }
    }

    fun clearCapturedVideo() {
        _uiState.update { it.copy(capturedVideoUri = null) }
    }

    fun dismissCreatePost() {
        _uiState.update { it.copy(
            showCreatePostSheet  = false,
            isPublishingPost     = false,
            createPostErrorMessage = null,
            selectedWorkout      = null,
            postCaption          = "",
            nutritionPhotoUri    = null,
            nutritionTitle       = "",
            nutritionIngredients = "",
            nutritionInstructions = "",
            nutritionCookTime    = "",
            nutritionKcal        = "",
            nutritionBestMoment  = "",
            capturedVideoUri     = null,
        ) }
    }

    fun setCreatePostType(type: UserPostType) {
        _uiState.update { it.copy(createPostType = type, createPostErrorMessage = null) }
    }

    fun selectWorkout(workout: LoggedWorkout) {
        _uiState.update { it.copy(selectedWorkout = workout) }
    }

    fun onPostCaptionChange(value: String) {
        _uiState.update { it.copy(postCaption = value, createPostErrorMessage = null) }
    }

    fun onNutritionPhotoChange(uri: Uri) {
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) { }
        _uiState.update { it.copy(nutritionPhotoUri = uri, createPostErrorMessage = null) }
    }

    fun onNutritionTitleChange(value: String) {
        _uiState.update { it.copy(nutritionTitle = value, createPostErrorMessage = null) }
    }

    fun onNutritionIngredientsChange(value: String) {
        _uiState.update { it.copy(nutritionIngredients = value, createPostErrorMessage = null) }
    }

    fun onNutritionInstructionsChange(value: String) {
        _uiState.update { it.copy(nutritionInstructions = value, createPostErrorMessage = null) }
    }

    fun onNutritionCookTimeChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 3) {
            _uiState.update { it.copy(nutritionCookTime = value, createPostErrorMessage = null) }
        }
    }

    fun onNutritionKcalChange(value: String) {
        if (value.all { it.isDigit() } && value.length <= 5) {
            _uiState.update { it.copy(nutritionKcal = value, createPostErrorMessage = null) }
        }
    }

    fun onNutritionBestMomentChange(value: String) {
        _uiState.update { it.copy(nutritionBestMoment = value, createPostErrorMessage = null) }
    }

    fun publishPost() {
        val state = _uiState.value
        if (!state.canPublish || state.isPublishingPost) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishingPost = true, createPostErrorMessage = null) }
            try {
                val uid = auth.currentUser?.uid
                    ?: throw IllegalStateException("Inicia sesión para publicar.")

                val post = when (state.createPostType) {
                    UserPostType.WORKOUT -> {
                        val w = state.selectedWorkout
                            ?: throw IllegalStateException("Selecciona un entrenamiento.")
                        val videoUrl = state.capturedVideoUri?.let { uploadPostMedia(it, uid, isVideo = true) }
                        UserPost(
                            type                   = UserPostType.WORKOUT,
                            caption                = state.postCaption.ifBlank { w.name },
                            workoutName            = w.name,
                            workoutDurationMinutes = w.durationMinutes,
                            workoutKcal            = w.kcalBurned,
                            workoutVideoUri        = videoUrl,
                            workoutMediaUrls       = w.mediaUrls,
                            workoutTotalWeightKg   = w.totalVolumeKg,
                            workoutExercises       = w.exercises,
                        )
                    }
                    UserPostType.NUTRITION -> {
                        val photoUrl = state.nutritionPhotoUri?.let { uploadPostMedia(it, uid, isVideo = false) }
                        UserPost(
                            type                     = UserPostType.NUTRITION,
                            caption                  = state.nutritionTitle,
                            nutritionPhotoUri        = photoUrl,
                            nutritionKcal            = state.nutritionKcal.toIntOrNull(),
                            nutritionIngredients     = state.nutritionIngredients.ifBlank { null },
                            nutritionInstructions    = state.nutritionInstructions.ifBlank { null },
                            nutritionCookTimeMinutes = state.nutritionCookTime.toIntOrNull(),
                            nutritionBestMoment      = state.nutritionBestMoment.ifBlank { null },
                        )
                    }
                }

                PostRepository.addPost(post, authorName = state.displayName)
                dismissCreatePost()
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isPublishingPost = false,
                        createPostErrorMessage = exception.localizedMessage ?: "No se pudo publicar",
                    )
                }
            }
        }
    }

    /**
     * Sube una imagen/vídeo local (content:// del picker o file:// de la cámara) a
     * Firebase Storage y devuelve la URL de descarga remota. Los posts deben guardar
     * URLs https, nunca URIs locales: una URI local no es accesible desde otros
     * dispositivos. El llamante indica el tipo con [isVideo]; no se infiere del MIME
     * porque las URIs file:// de la cámara no lo resuelven de forma fiable.
     */
    private suspend fun uploadPostMedia(uri: Uri, uid: String, isVideo: Boolean): String =
        withContext(Dispatchers.IO) {
            val ext = if (isVideo) "mp4" else "jpg"
            val ref = storage.reference
                .child("users/$uid/posts/${System.currentTimeMillis()}.$ext")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        }

    private fun minutesPerDay(
        map: Map<LocalDate, List<LoggedWorkout>>,
        weekStart: LocalDate,
    ): List<Int> = (0L..6L).map { offset ->
        val date = weekStart.plusDays(offset)
        map[date]?.sumOf { it.durationMinutes } ?: 0
    }

    private fun compactCount(value: Int): String =
        when {
            value >= 10_000 -> "${value / 1_000}k"
            value >= 1_000 -> "${value / 1_000}.${(value % 1_000) / 100}k"
            else -> value.toString()
        }

    override fun onCleared() {
        profileListenerRegistration?.remove()
        profileListenerRegistration = null
        followersListenerRegistration?.remove()
        followingListenerRegistration?.remove()
        super.onCleared()
    }
}
