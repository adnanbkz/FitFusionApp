package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.FeedComment
import com.example.fitfusion.data.repository.FeedRepository
import com.example.fitfusion.data.repository.PostInteractionRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostDetailUiState(
    val authorName: String = "FitFusion",
    val authorSubtitle: String = "",
    val authorPhotoUrl: String? = null,
    val authorId: String = "",
    val title: String = "Cargando publicación",
    val description: String = "",
    val mediaUri: String? = null,
    val statOneValue: String = "—",
    val statOneLabel: String = "INFO",
    val statTwoValue: String = "—",
    val statTwoLabel: String = "TIEMPO",
    val statThreeValue: String = "—",
    val statThreeLabel: String = "TOTAL",
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val likeCount: String = "0",
    val commentCount: String = "0",
    val isWorkout: Boolean = false,
    val exercises: List<ExerciseItem> = emptyList(),
    val mediaUrls: List<String> = emptyList(),
    val kcal: Int = 0,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val cookTimeMinutes: Int = 0,
    val previewComments: List<FeedComment> = emptyList(),
)

class PostDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var loadedPostId: String? = null
    private var commentsListenerRegistration: ListenerRegistration? = null

    fun loadPost(postId: String?) {
        val id = postId?.takeIf { it.isNotBlank() } ?: run {
            loadJob?.cancel()
            commentsListenerRegistration?.remove()
            loadedPostId = null
            _uiState.value = PostDetailUiState(
                title = "Selecciona una publicación",
                description = "Abre un post desde el feed para ver su detalle.",
            )
            return
        }
        if (id == loadedPostId) return
        loadedPostId = id
        loadJob?.cancel()
        commentsListenerRegistration?.remove()
        _uiState.value = PostDetailUiState()
        attachCommentsListener(id)
        loadJob = viewModelScope.launch {
            val initialItem = FeedRepository.items.value.findPost(id)
                ?: runCatching { FeedRepository.getPostById(id) }.getOrNull()
            if (initialItem != null) {
                updatePost(initialItem)
            } else if (FeedRepository.items.value.isEmpty()) {
                _uiState.update {
                    it.copy(
                        title = "Publicación no encontrada",
                        description = "No se pudo cargar este post.",
                    )
                }
            }
            FeedRepository.items.collect { items ->
                val item = items.findPost(id)
                if (item != null) {
                    updatePost(item)
                } else if (items.isNotEmpty()) {
                    _uiState.update { it.copy(title = "Publicación no encontrada") }
                }
            }
        }
    }

    fun toggleLike() {
        val id = loadedPostId ?: return
        FeedRepository.toggleLike(id)
    }

    fun toggleSave() {
        val id = loadedPostId ?: return
        FeedRepository.toggleSave(id)
    }

    private fun attachCommentsListener(postId: String) {
        commentsListenerRegistration = PostInteractionRepository.listenComments(postId) { comments ->
            _uiState.update {
                it.copy(
                    commentCount = comments.size.toString(),
                    previewComments = comments.take(3),
                )
            }
        }
    }

    private fun updatePost(item: FeedItem) {
        val currentCommentCount = _uiState.value.commentCount
        val currentPreview = _uiState.value.previewComments
        _uiState.value = item.toDetailState(currentCommentCount, currentPreview)
    }

    private fun List<FeedItem>.findPost(id: String): FeedItem? = firstOrNull {
        when (it) {
            is FeedItem.Workout   -> it.post.id == id
            is FeedItem.Nutrition -> it.post.id == id
        }
    }

    private fun FeedItem.toDetailState(
        commentCount: String,
        previewComments: List<FeedComment>,
    ): PostDetailUiState = when (this) {
        is FeedItem.Workout -> PostDetailUiState(
            authorName      = post.author,
            authorSubtitle  = post.timeAgo,
            authorPhotoUrl  = post.authorPhotoUrl,
            authorId        = post.authorId,
            title           = post.workoutName,
            mediaUri        = post.mediaUrls.firstOrNull() ?: post.videoUri,
            mediaUrls       = post.mediaUrls,
            statOneValue    = post.exercises.size.toString(),
            statOneLabel    = "EJERCICIOS",
            statTwoValue    = "${post.durationMin}m",
            statTwoLabel    = "TIEMPO",
            statThreeValue  = if (post.totalWeightKg > 0.0) "${post.totalWeightKg.toInt()}kg"
                              else post.exercises.sumOf { it.sets }.toString(),
            statThreeLabel  = if (post.totalWeightKg > 0.0) "VOLUMEN" else "SERIES",
            isLiked         = post.isLiked,
            isSaved         = post.isSaved,
            likeCount       = post.likes.toString(),
            commentCount    = maxOf(post.comments, commentCount.toIntOrNull() ?: 0).toString(),
            isWorkout       = true,
            exercises       = post.exercises,
            kcal            = post.kcal,
            previewComments = previewComments,
        )
        is FeedItem.Nutrition -> PostDetailUiState(
            authorName      = post.author,
            authorSubtitle  = post.timeAgo,
            authorPhotoUrl  = post.authorPhotoUrl,
            authorId        = post.authorId,
            title           = post.title,
            description     = post.description.ifBlank { "Receta compartida en FitFusion." },
            mediaUri        = post.imageUrl,
            mediaUrls       = listOfNotNull(post.imageUrl),
            statOneValue    = "${post.kcal}",
            statOneLabel    = "KCAL",
            statTwoValue    = if (post.cookTimeMinutes > 0) "${post.cookTimeMinutes}m" else "—",
            statTwoLabel    = "COCINA",
            statThreeValue  = "${post.proteinG}g",
            statThreeLabel  = "PROT",
            isLiked         = post.isLiked,
            isSaved         = post.isSaved,
            likeCount       = post.likes.toString(),
            commentCount    = maxOf(post.comments, commentCount.toIntOrNull() ?: 0).toString(),
            isWorkout       = false,
            kcal            = post.kcal,
            proteinG        = post.proteinG,
            carbsG          = post.carbsG,
            cookTimeMinutes = post.cookTimeMinutes,
            previewComments = previewComments,
        )
    }

    override fun onCleared() {
        commentsListenerRegistration?.remove()
        commentsListenerRegistration = null
        super.onCleared()
    }
}
