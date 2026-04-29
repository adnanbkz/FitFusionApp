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

data class CommentData(
    val author: String,
    val text: String,
    val time: String,
    val likes: Int,
    val isAuthorReply: Boolean = false
)

data class PostDetailUiState(
    val authorName: String = "FitFusion",
    val authorSubtitle: String = "Publicación",
    val title: String = "Cargando publicación",
    val description: String = "",
    val hashtags: String = "#FitFusion",
    val mediaUri: String? = null,
    val mediaLabel: String = "PUBLICACIÓN",
    val statOneValue: String = "—",
    val statOneLabel: String = "INFO",
    val statTwoValue: String = "—",
    val statTwoLabel: String = "TIEMPO",
    val statThreeValue: String = "—",
    val statThreeLabel: String = "TOTAL",
    val likeCount: String = "0",
    val commentCount: String = "0",
    val energyCount: String = "0",
    val metricOneLabel: String = "DETALLE",
    val metricOneValue: String = "—",
    val metricOneUnit: String = "",
    val metricTwoLabel: String = "CALORÍAS",
    val metricTwoValue: String = "—",
    val metricTwoUnit: String = "KCAL",
    val commentText: String = "",
    val comments: List<CommentData> = emptyList(),
    val isSendingComment: Boolean = false,
    val commentErrorMessage: String? = null,
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
        _uiState.value = PostDetailUiState(commentText = _uiState.value.commentText)
        attachCommentsListener(id)
        loadJob = viewModelScope.launch {
            FeedRepository.items.collect { items ->
                val item = items.firstOrNull {
                    when (it) {
                        is FeedItem.Workout -> it.post.id == id
                        is FeedItem.Nutrition -> it.post.id == id
                    }
                }
                if (item != null) {
                    val current = _uiState.value
                    _uiState.value = item.toDetailState(
                        commentText = current.commentText,
                        comments = current.comments,
                        isSendingComment = current.isSendingComment,
                        commentErrorMessage = current.commentErrorMessage,
                    )
                } else if (items.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        title = "Publicación no encontrada",
                        description = "Puede que se haya eliminado o que ya no esté disponible.",
                    )
                }
            }
        }
    }

    fun onCommentTextChange(value: String) {
        _uiState.value = _uiState.value.copy(
            commentText = value.take(MAX_COMMENT_LENGTH),
            commentErrorMessage = null,
        )
    }

    fun sendComment() {
        val text = _uiState.value.commentText.trim()
        val postId = loadedPostId
        if (text.isBlank() || postId.isNullOrBlank() || _uiState.value.isSendingComment) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingComment = true, commentErrorMessage = null) }
            try {
                PostInteractionRepository.addComment(postId, text)
                _uiState.update { it.copy(commentText = "", isSendingComment = false) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSendingComment = false,
                        commentErrorMessage = exception.localizedMessage ?: "No se pudo publicar el comentario",
                    )
                }
            }
        }
    }

    private fun attachCommentsListener(postId: String) {
        commentsListenerRegistration = PostInteractionRepository.listenComments(postId) { comments ->
            _uiState.update { current ->
                val mappedComments = comments.map { it.toCommentData(current.authorName) }
                current.copy(
                    comments = mappedComments,
                    commentCount = mappedComments.size.toString(),
                )
            }
        }
    }

    private fun FeedItem.toDetailState(
        commentText: String,
        comments: List<CommentData>,
        isSendingComment: Boolean,
        commentErrorMessage: String?,
    ): PostDetailUiState = when (this) {
        is FeedItem.Workout -> PostDetailUiState(
            authorName = post.author,
            authorSubtitle = "${post.workoutType} · ${post.timeAgo}",
            title = post.workoutName,
            description = if (post.exercises.isEmpty()) {
                "Entrenamiento compartido en FitFusion."
            } else {
                post.exercises.joinToString("\n") { "${it.name}: ${it.sets} series · ${it.reps} reps" }
            },
            hashtags = "#Workout  #FitFusion",
            mediaUri = post.videoUri,
            mediaLabel = "ENTRENO",
            statOneValue = post.exercises.size.toString(),
            statOneLabel = "EJERCICIOS",
            statTwoValue = "${post.durationMin}m",
            statTwoLabel = "TIEMPO",
            statThreeValue = if (post.totalWeightKg > 0.0) "${post.totalWeightKg.toInt()}kg" else post.exercises.sumOf { it.sets }.toString(),
            statThreeLabel = if (post.totalWeightKg > 0.0) "VOLUMEN" else "SERIES",
            likeCount = post.likes.toString(),
            commentCount = maxOf(post.comments, comments.size).toString(),
            energyCount = post.exercises.sumOf { it.sets }.toString(),
            metricOneLabel = "EJERCICIOS",
            metricOneValue = post.exercises.size.toString(),
            metricTwoLabel = "CALORÍAS QUEMADAS",
            metricTwoValue = if (post.kcal > 0) post.kcal.toString() else "—",
            metricTwoUnit = "KCAL",
            commentText = commentText,
            comments = comments.markAuthorReplies(post.author),
            isSendingComment = isSendingComment,
            commentErrorMessage = commentErrorMessage,
        )
        is FeedItem.Nutrition -> PostDetailUiState(
            authorName = post.author,
            authorSubtitle = "Nutrición · ${post.timeAgo}",
            title = post.title,
            description = post.description.ifBlank { "Receta compartida en FitFusion." },
            hashtags = "#Nutrition  #FitFusion",
            mediaUri = post.imageUrl,
            mediaLabel = "NUTRICIÓN",
            statOneValue = "${post.kcal}",
            statOneLabel = "KCAL",
            statTwoValue = if (post.cookTimeMinutes > 0) "${post.cookTimeMinutes}m" else "—",
            statTwoLabel = "COCINA",
            statThreeValue = "${post.proteinG}g",
            statThreeLabel = "PROT",
            likeCount = post.likes.toString(),
            commentCount = maxOf(post.comments, comments.size).toString(),
            energyCount = post.carbsG.toString(),
            metricOneLabel = "TIEMPO COCINA",
            metricOneValue = if (post.cookTimeMinutes > 0) post.cookTimeMinutes.toString() else "—",
            metricOneUnit = "MIN",
            metricTwoLabel = "CALORÍAS",
            metricTwoValue = post.kcal.toString(),
            metricTwoUnit = "KCAL",
            commentText = commentText,
            comments = comments.markAuthorReplies(post.author),
            isSendingComment = isSendingComment,
            commentErrorMessage = commentErrorMessage,
        )
    }

    private fun List<CommentData>.markAuthorReplies(postAuthorName: String): List<CommentData> =
        map { it.copy(isAuthorReply = it.author == postAuthorName) }

    private fun FeedComment.toCommentData(postAuthorName: String): CommentData =
        CommentData(
            author = authorName,
            text = text,
            time = formatCommentTimeAgo(createdAtMs),
            likes = likesCount,
            isAuthorReply = authorName == postAuthorName,
        )

    private fun formatCommentTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace ${minutes}min"
            hours < 24 -> "Hace ${hours}h"
            days < 7 -> "Hace ${days}d"
            else -> "Hace ${days / 7}sem"
        }
    }

    override fun onCleared() {
        commentsListenerRegistration?.remove()
        commentsListenerRegistration = null
        super.onCleared()
    }
}

private const val MAX_COMMENT_LENGTH = 500
