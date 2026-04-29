package com.example.fitfusion.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

data class FeedComment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val createdAtMs: Long,
    val likesCount: Int = 0,
)

object PostInteractionRepository {

    private const val POSTS_COLLECTION = "posts"
    private const val COMMENTS_COLLECTION = "comments"
    private const val MAX_COMMENT_LENGTH = 500

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository(firestore)

    fun listenComments(
        postId: String,
        onComments: (List<FeedComment>) -> Unit,
    ): ListenerRegistration =
        firestore.collection(POSTS_COLLECTION)
            .document(postId)
            .collection(COMMENTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                onComments(
                    snapshot.documents
                        .mapNotNull { it.toFeedCommentOrNull() }
                        .sortedByDescending { it.createdAtMs }
                )
            }

    suspend fun addComment(postId: String, text: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Inicia sesión para comentar.")
        val trimmedText = text.trim().take(MAX_COMMENT_LENGTH)
        if (trimmedText.isBlank()) return

        val authorName = runCatching {
            userRepository.getUserProfile(user.uid, user.email.orEmpty()).displayName
        }.getOrElse {
            user.displayName ?: user.email?.substringBefore("@") ?: "Usuario"
        }.ifBlank { "Usuario" }

        firestore.collection(POSTS_COLLECTION)
            .document(postId)
            .collection(COMMENTS_COLLECTION)
            .add(
                mapOf(
                    "authorId" to user.uid,
                    "authorName" to authorName,
                    "text" to trimmedText,
                    "createdAtMs" to System.currentTimeMillis(),
                    "likesCount" to 0,
                )
            )
            .await()
    }

    private fun DocumentSnapshot.toFeedCommentOrNull(): FeedComment? {
        val text = getString("text").orEmpty()
        if (text.isBlank()) return null
        return FeedComment(
            id = id,
            authorId = getString("authorId").orEmpty(),
            authorName = getString("authorName").orEmpty().ifBlank { "Usuario" },
            text = text,
            createdAtMs = getLong("createdAtMs") ?: 0L,
            likesCount = getLong("likesCount")?.toInt() ?: 0,
        )
    }
}
