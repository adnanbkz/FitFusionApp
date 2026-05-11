package com.example.fitfusion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.example.fitfusion.data.repository.PostRepository
import com.example.fitfusion.data.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutPostUiState(
    val workout: LoggedWorkout? = null,
    val caption: String = "",
    val isPublishing: Boolean = false,
    val errorMessage: String? = null,
    val published: Boolean = false,
)

class WorkoutPostViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(WorkoutPostUiState())
    val uiState: StateFlow<WorkoutPostUiState> = _uiState.asStateFlow()

    fun loadWorkout(workoutId: String) {
        if (_uiState.value.workout?.id == workoutId) return
        val workout = WorkoutRepository.workouts.value.values.flatten().firstOrNull { it.id == workoutId }
        _uiState.update { it.copy(workout = workout, caption = workout?.name ?: "") }
    }

    fun onCaptionChange(value: String) {
        _uiState.update { it.copy(caption = value, errorMessage = null) }
    }

    fun publish() {
        val state = _uiState.value
        val workout = state.workout ?: return
        if (state.isPublishing) return
        _uiState.update { it.copy(isPublishing = true, errorMessage = null) }

        val authorName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: auth.currentUser?.email?.substringBefore("@")
            ?: "Usuario FitFusion"

        val post = UserPost(
            type                   = UserPostType.WORKOUT,
            caption                = state.caption.trim().ifBlank { workout.name },
            workoutName            = workout.name,
            workoutDurationMinutes = workout.durationMinutes,
            workoutKcal            = workout.kcalBurned,
            workoutMediaUrls       = workout.mediaUrls,
            workoutTotalWeightKg   = workout.totalVolumeKg,
            workoutExercises       = workout.exercises,
        )

        viewModelScope.launch {
            try {
                PostRepository.addPost(post, authorName = authorName)
                _uiState.update { it.copy(isPublishing = false, published = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        errorMessage = e.localizedMessage ?: "No se pudo publicar el entrenamiento",
                    )
                }
            }
        }
    }
}
