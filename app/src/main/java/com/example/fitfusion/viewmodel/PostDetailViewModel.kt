package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CommentData(
    val author: String,
    val text: String,
    val time: String,
    val likes: Int,
    val isAuthorReply: Boolean = false
)

data class PostDetailUiState(
    val authorName: String = "Elena Rodriguez",
    val authorSubtitle: String = "Pro Athlete • 2h ago",
    val title: String = "Morning Coastal Intervals",
    val description: String = "Nothing beats the salt air at 6 AM. Pushed the tempo on the final 3 miles. Feeling incredibly strong as marathon prep officially kicks into high gear. The new shoes are definitely making a difference in energy return! \uD83C\uDFC3\uD83D\uDCA8",
    val hashtags: String = "#MarathonTraining  #MorningRun\n#CoastalRun  #FitLife",
    val miles: String = "8.42",
    val time: String = "52:14",
    val pace: String = "6:12",
    val likeCount: String = "1.2k",
    val commentCount: String = "84",
    val energyCount: String = "42",
    val avgHeartRate: String = "158",
    val caloriesBurned: String = "842",
    val commentText: String = "",
    val comments: List<CommentData> = listOf(
        CommentData("Marcus Chen", "Insane pace for intervals! What shoes are those? I'm looking for a new pair for my long runs.", "12m ago", 12),
        CommentData("Sarah Miller", "The coastal path is gorgeous this time of year. Great work on that pace!", "45m ago", 3),
        CommentData("Elena Rodriguez", "Thanks Sarah! It was truly magic out there today.", "30m ago", 0, isAuthorReply = true)
    )
)

class PostDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    fun onCommentTextChange(value: String) {
        _uiState.value = _uiState.value.copy(commentText = value)
    }

    fun sendComment() {
        val text = _uiState.value.commentText
        if (text.isBlank()) return
        // TODO: Post comment to Firestore
        _uiState.value = _uiState.value.copy(commentText = "")
    }

    // TODO: Load post + comments from Firestore
}