package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.models.UserPostType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object PostRepository {

    private const val POSTS_COLLECTION = "posts"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _posts = MutableStateFlow<List<UserPost>>(emptyList())
    val posts: StateFlow<List<UserPost>> = _posts.asStateFlow()

    private var postsListenerRegistration: ListenerRegistration? = null
    private var authListenerRegistered = false
    private var currentUid: String? = null

    init {
        ensureInitialized()
    }

    fun ensureInitialized() {
        if (authListenerRegistered) return
        auth.addAuthStateListener { firebaseAuth ->
            attachUserPostsListener(firebaseAuth.currentUser?.uid)
        }
        authListenerRegistered = true
        attachUserPostsListener(auth.currentUser?.uid)
    }

    suspend fun addPost(post: UserPost, authorName: String) {
        ensureInitialized()
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Inicia sesión para publicar.")

        val docRef = firestore.collection(POSTS_COLLECTION).document(post.id)
        docRef.set(post.toFirestoreMap(uid, authorName)).await()
    }

    private fun attachUserPostsListener(uid: String?) {
        if (uid == currentUid && postsListenerRegistration != null) return

        postsListenerRegistration?.remove()
        postsListenerRegistration = null
        currentUid = uid

        if (uid.isNullOrBlank()) {
            _posts.value = emptyList()
            return
        }

        postsListenerRegistration = firestore.collection(POSTS_COLLECTION)
            .whereEqualTo("authorId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _posts.value = snapshot.documents
                    .mapNotNull { document -> document.toUserPostOrNull() }
                    .sortedByDescending { it.timestamp }
            }
    }

    private fun UserPost.toFirestoreMap(authorId: String, authorName: String): Map<String, Any?> =
        mapOf(
            "type" to when (type) {
                UserPostType.WORKOUT -> "workout"
                UserPostType.NUTRITION -> "nutrition"
            },
            "authorId" to authorId,
            "authorName" to authorName,
            "authorInitials" to authorName.toInitials(),
            "caption" to caption.trim().take(MAX_POST_CAPTION_LENGTH),
            "createdAtMs" to timestamp,
            "likesCount" to 0,
            "commentsCount" to 0,
            "workoutName" to workoutName,
            "workoutEmoji" to workoutEmoji,
            "workoutDurationMinutes" to workoutDurationMinutes,
            "workoutKcal" to workoutKcal,
            "workoutVideoUri" to workoutVideoUri,
            "workoutTotalWeightKg" to workoutTotalWeightKg,
            "workoutExercises" to workoutExercises.map { exercise ->
                mapOf(
                    "name" to exercise.name,
                    "sets" to exercise.sets.size,
                    "reps" to exercise.sets.sumOf { it.reps },
                )
            },
            "nutritionPhotoUri" to nutritionPhotoUri,
            "nutritionKcal" to nutritionKcal,
            "nutritionIngredients" to nutritionIngredients,
            "nutritionInstructions" to nutritionInstructions,
            "nutritionCookTimeMinutes" to nutritionCookTimeMinutes,
            "nutritionBestMoment" to nutritionBestMoment,
        )

    private fun com.google.firebase.firestore.DocumentSnapshot.toUserPostOrNull(): UserPost? {
        val type = when (getString("type")) {
            "workout" -> UserPostType.WORKOUT
            "nutrition" -> UserPostType.NUTRITION
            else -> return null
        }
        return UserPost(
            id = id,
            type = type,
            caption = getString("caption").orEmpty(),
            workoutName = getString("workoutName"),
            workoutEmoji = getString("workoutEmoji"),
            workoutDurationMinutes = getLong("workoutDurationMinutes")?.toInt(),
            workoutKcal = getLong("workoutKcal")?.toInt(),
            workoutVideoUri = getString("workoutVideoUri"),
            workoutTotalWeightKg = (get("workoutTotalWeightKg") as? Number)?.toFloat(),
            workoutExercises = emptyList(),
            nutritionPhotoUri = getString("nutritionPhotoUri"),
            nutritionKcal = getLong("nutritionKcal")?.toInt(),
            nutritionIngredients = getString("nutritionIngredients"),
            nutritionInstructions = getString("nutritionInstructions"),
            nutritionCookTimeMinutes = getLong("nutritionCookTimeMinutes")?.toInt(),
            nutritionBestMoment = getString("nutritionBestMoment"),
            timestamp = getLong("createdAtMs") ?: 0L,
        )
    }
}

internal fun String.toInitials(): String =
    trim().split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "U" }

private const val MAX_POST_CAPTION_LENGTH = 200
