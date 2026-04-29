package com.example.fitfusion.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.WorkoutRepository
import com.example.fitfusion.data.workout.ActiveWorkoutManager
import com.example.fitfusion.data.workout.ActiveWorkoutSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class WorkoutFinishUiState(
    val title: String = "",
    val description: String = "",
    val mediaUris: List<Uri> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedWorkoutId: String? = null,
    val session: ActiveWorkoutSession? = null,
    val elapsedSeconds: Long = 0L,
)

class WorkoutFinishViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = FirebaseStorage.getInstance()
    private val auth    = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(WorkoutFinishUiState())
    val uiState: StateFlow<WorkoutFinishUiState> = _uiState.asStateFlow()

    init {
        val current = ActiveWorkoutManager.session.value
        _uiState.update {
            it.copy(
                title          = current?.name.orEmpty(),
                session        = current,
                elapsedSeconds = current?.elapsedSeconds(System.currentTimeMillis()) ?: 0L,
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun onMediaPicked(uris: List<Uri>) {
        _uiState.update { current ->
            val combined = (current.mediaUris + uris).distinct().take(5)
            current.copy(mediaUris = combined, errorMessage = null)
        }
    }

    fun removeMedia(uri: Uri) {
        _uiState.update { it.copy(mediaUris = it.mediaUris.filter { item -> item != uri }) }
    }

    fun save(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving || state.session == null) return
        val sessionId = state.session.id
        val title = state.title.trim().ifBlank { state.session.name }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val saved = runCatching {
                ActiveWorkoutManager.finishSession(
                    title       = title,
                    description = state.description.trim(),
                    mediaUrls   = emptyList(),
                )
            }
            saved.fold(
                onSuccess = {
                    if (state.mediaUris.isNotEmpty()) {
                        runCatching { uploadMedia(sessionId, state.mediaUris) }
                            .onSuccess { urls ->
                                runCatching { WorkoutRepository.updateWorkoutMedia(sessionId, urls) }
                            }
                    }
                    _uiState.update { it.copy(isSaving = false, savedWorkoutId = sessionId) }
                    onDone()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.localizedMessage ?: "No se pudo guardar el entrenamiento",
                        )
                    }
                },
            )
        }
    }

    private suspend fun uploadMedia(workoutId: String, uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        val ctx = getApplication<Application>().applicationContext
        uris.mapIndexedNotNull { index, uri ->
            runCatching {
                val mime = ctx.contentResolver.getType(uri).orEmpty()
                val ext = when {
                    mime.startsWith("video/") -> "mp4"
                    mime.startsWith("image/") -> "jpg"
                    else -> "bin"
                }
                val ref = storage.reference
                    .child("users/$uid/workouts/$workoutId/media/${System.currentTimeMillis()}_$index.$ext")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }.getOrNull()
        }
    }
}
