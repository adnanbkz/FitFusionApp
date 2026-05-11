package com.example.fitfusion.data.repository

import com.example.fitfusion.viewmodel.ExerciseItem
import com.example.fitfusion.viewmodel.FeedItem
import com.example.fitfusion.viewmodel.NutritionPost
import com.example.fitfusion.viewmodel.WorkoutPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FeedRepository {

    private const val POSTS_COLLECTION = "posts"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _items = MutableStateFlow<List<FeedItem>>(emptyList())
    val items: StateFlow<List<FeedItem>> = _items.asStateFlow()

    private val _likedPosts = MutableStateFlow<List<FeedItem>>(emptyList())
    val likedPosts: StateFlow<List<FeedItem>> = _likedPosts.asStateFlow()

    private var baseItems: List<FeedItem> = emptyList()
    private val likedPostIds = mutableSetOf<String>()
    private val savedPostIds = mutableSetOf<String>()
    private val likeCountsByPostId = mutableMapOf<String, Int>()
    private val likeListenerRegistrations = mutableMapOf<String, ListenerRegistration>()
    private val commentCountsByPostId = mutableMapOf<String, Int>()
    private val commentListenerRegistrations = mutableMapOf<String, ListenerRegistration>()
    private var feedListenerRegistration: ListenerRegistration? = null
    private var savesListenerRegistration: ListenerRegistration? = null
    private var authListenerRegistered = false
    private var currentUid: String? = null

    init {
        ensureInitialized()
    }

    fun ensureInitialized() {
        if (authListenerRegistered) return
        auth.addAuthStateListener { firebaseAuth ->
            attachFeedListener(firebaseAuth.currentUser?.uid)
        }
        authListenerRegistered = true
        attachFeedListener(auth.currentUser?.uid)
    }

    fun toggleSave(itemId: String) {
        val uid = auth.currentUser?.uid ?: return
        val saveRef = firestore.collection("users")
            .document(uid)
            .collection("saves")
            .document(itemId)

        val wasSaved = itemId in savedPostIds
        if (wasSaved) {
            savedPostIds.remove(itemId)
        } else {
            savedPostIds.add(itemId)
        }
        emitItems()

        val task = if (wasSaved) {
            saveRef.delete()
        } else {
            saveRef.set(mapOf("savedAtMs" to System.currentTimeMillis()))
        }
        task.addOnFailureListener {
            if (wasSaved) savedPostIds.add(itemId) else savedPostIds.remove(itemId)
            emitItems()
        }
    }

    fun toggleLike(itemId: String) {
        val uid = auth.currentUser?.uid ?: return
        val likeRef = firestore.collection(POSTS_COLLECTION)
            .document(itemId)
            .collection("likes")
            .document(uid)

        val wasLiked = itemId in likedPostIds
        val previousCount = currentLikeCount(itemId)
        setOptimisticLikeState(itemId, liked = !wasLiked, previousCount = previousCount)

        val task = if (wasLiked) {
            likeRef.delete()
        } else {
            likeRef.set(
                mapOf(
                    "userId" to uid,
                    "createdAtMs" to System.currentTimeMillis(),
                )
            )
        }
        task.addOnFailureListener {
            setOptimisticLikeState(itemId, liked = wasLiked, previousCount = previousCount)
        }
        task.addOnCompleteListener { refreshLikedPosts() }
    }

    private fun attachFeedListener(uid: String?) {
        if (uid == currentUid && feedListenerRegistration != null) return

        feedListenerRegistration?.remove()
        feedListenerRegistration = null
        savesListenerRegistration?.remove()
        savesListenerRegistration = null
        clearInteractionListeners()
        savedPostIds.clear()
        currentUid = uid
        _likedPosts.value = emptyList()

        if (uid.isNullOrBlank()) {
            baseItems = emptyList()
            _items.value = emptyList()
            return
        }

        feedListenerRegistration = firestore.collection(POSTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                baseItems = snapshot.documents
                    .mapNotNull { document -> document.toFeedItemOrNull() }
                    .sortedByDescending { it.timestamp }
                val postIds = baseItems.map { it.postId }
                syncLikeListeners(uid, postIds)
                syncCommentCountListeners(postIds)
                emitItems()
            }
        savesListenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("saves")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                savedPostIds.clear()
                savedPostIds.addAll(snapshot.documents.map { it.id })
                emitItems()
            }
        refreshLikedPosts()
    }

    fun refreshLikedPosts() {
        val uid = auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val queryUid = uid
                val likedDocs = firestore.collectionGroup("likes")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()
                if (currentUid != queryUid) return@launch
                val likedPostIds = likedDocs.documents
                    .mapNotNull { it.reference.parent.parent?.id }
                    .distinct()
                if (likedPostIds.isEmpty()) {
                    _likedPosts.value = emptyList()
                    return@launch
                }
                val posts = likedPostIds.chunked(10).flatMap { batch ->
                    firestore.collection(POSTS_COLLECTION)
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch)
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it.toFeedItemOrNull() }
                }
                if (currentUid == queryUid) {
                    _likedPosts.value = posts
                        .sortedByDescending { it.timestamp }
                        .map { it.withPersistedInteractionState() }
                }
            } catch (_: Exception) { }
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFeedItemOrNull(): FeedItem? {
        val id = id
        val authorName = getString("authorName").orEmpty().ifBlank { "Usuario FitFusion" }
        val authorInitials = getString("authorInitials").orEmpty().ifBlank { authorName.toInitials() }
        val timestamp = getLong("createdAtMs") ?: 0L
        val likes = getLong("likesCount")?.toInt() ?: 0
        val comments = getLong("commentsCount")?.toInt() ?: 0

        return when (getString("type")) {
            "workout" -> FeedItem.Workout(
                WorkoutPost(
                    id = id,
                    author = authorName,
                    authorInitials = authorInitials,
                    workoutType = "Entreno",
                    workoutName = getString("workoutName").orEmpty().ifBlank {
                        getString("caption").orEmpty().ifBlank { "Entrenamiento" }
                    },
                    durationMin = getLong("workoutDurationMinutes")?.toInt() ?: 0,
                    kcal = getLong("workoutKcal")?.toInt() ?: 0,
                    totalWeightKg = (get("workoutTotalWeightKg") as? Number)?.toDouble() ?: 0.0,
                    exercises = readExerciseItems(),
                    videoUri = getString("workoutVideoUri"),
                    mediaUrls = (get("workoutMediaUrls") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
                    likes = likes,
                    comments = comments,
                    timestamp = timestamp,
                )
            )
            "nutrition" -> FeedItem.Nutrition(
                NutritionPost(
                    id = id,
                    author = authorName,
                    authorInitials = authorInitials,
                    imageUrl = getString("nutritionPhotoUri"),
                    title = getString("caption").orEmpty().ifBlank { "Receta FitFusion" },
                    description = getString("nutritionIngredients")
                        ?: getString("nutritionInstructions")
                        ?: getString("nutritionBestMoment").orEmpty(),
                    kcal = getLong("nutritionKcal")?.toInt() ?: 0,
                    proteinG = getLong("nutritionProteinG")?.toInt() ?: 0,
                    carbsG = getLong("nutritionCarbsG")?.toInt() ?: 0,
                    cookTimeMinutes = getLong("nutritionCookTimeMinutes")?.toInt() ?: 0,
                    likes = likes,
                    comments = comments,
                    timestamp = timestamp,
                )
            )
            else -> null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.readExerciseItems(): List<ExerciseItem> =
        (get("workoutExercises") as? List<*>)
            .orEmpty()
            .mapNotNull { raw ->
                val map = raw as? Map<*, *> ?: return@mapNotNull null
                ExerciseItem(
                    name = (map["name"] as? String).orEmpty().ifBlank { return@mapNotNull null },
                    sets = (map["sets"] as? Number)?.toInt() ?: 0,
                    reps = (map["reps"] as? Number)?.toInt() ?: 0,
                )
            }

    private fun syncLikeListeners(uid: String, postIds: List<String>) {
        val currentPostIds = postIds.toSet()
        val stalePostIds = likeListenerRegistrations.keys - currentPostIds
        stalePostIds.forEach { postId ->
            likeListenerRegistrations.remove(postId)?.remove()
            likeCountsByPostId.remove(postId)
            likedPostIds.remove(postId)
        }

        currentPostIds.forEach { postId ->
            if (likeListenerRegistrations.containsKey(postId)) return@forEach
            likeListenerRegistrations[postId] = firestore.collection(POSTS_COLLECTION)
                .document(postId)
                .collection("likes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    likeCountsByPostId[postId] = snapshot.size()
                    if (snapshot.documents.any { it.id == uid }) {
                        likedPostIds.add(postId)
                    } else {
                        likedPostIds.remove(postId)
                    }
                    emitItems()
                }
        }
    }

    private fun clearInteractionListeners() {
        likeListenerRegistrations.values.forEach { it.remove() }
        likeListenerRegistrations.clear()
        likeCountsByPostId.clear()
        likedPostIds.clear()
        clearCommentCountListeners()
    }

    private fun syncCommentCountListeners(postIds: List<String>) {
        val currentPostIds = postIds.toSet()
        val stalePostIds = commentListenerRegistrations.keys - currentPostIds
        stalePostIds.forEach { postId ->
            commentListenerRegistrations.remove(postId)?.remove()
            commentCountsByPostId.remove(postId)
        }

        currentPostIds.forEach { postId ->
            if (commentListenerRegistrations.containsKey(postId)) return@forEach
            commentListenerRegistrations[postId] = firestore.collection(POSTS_COLLECTION)
                .document(postId)
                .collection("comments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    commentCountsByPostId[postId] = snapshot.size()
                    emitItems()
                }
        }
    }

    private fun clearCommentCountListeners() {
        commentListenerRegistrations.values.forEach { it.remove() }
        commentListenerRegistrations.clear()
        commentCountsByPostId.clear()
    }

    private fun emitItems() {
        _items.value = baseItems.map { it.withPersistedInteractionState() }
    }

    private fun setOptimisticLikeState(itemId: String, liked: Boolean, previousCount: Int) {
        if (liked) {
            likedPostIds.add(itemId)
        } else {
            likedPostIds.remove(itemId)
        }
        likeCountsByPostId[itemId] = (previousCount + if (liked) 1 else -1).coerceAtLeast(0)
        emitItems()
    }

    private fun currentLikeCount(itemId: String): Int =
        likeCountsByPostId[itemId]
            ?: baseItems.firstOrNull { it.postId == itemId }?.likeCount
            ?: 0

    private val FeedItem.postId: String
        get() = when (this) {
            is FeedItem.Workout -> post.id
            is FeedItem.Nutrition -> post.id
        }

    private val FeedItem.likeCount: Int
        get() = when (this) {
            is FeedItem.Workout -> post.likes
            is FeedItem.Nutrition -> post.likes
        }

    private fun FeedItem.withPersistedInteractionState(): FeedItem = when (this) {
        is FeedItem.Workout -> copy(
            post = post.copy(
                isLiked = post.id in likedPostIds,
                isSaved = post.id in savedPostIds,
                likes = likeCountsByPostId[post.id] ?: post.likes,
                comments = commentCountsByPostId[post.id] ?: post.comments,
            )
        )
        is FeedItem.Nutrition -> copy(
            post = post.copy(
                isLiked = post.id in likedPostIds,
                isSaved = post.id in savedPostIds,
                likes = likeCountsByPostId[post.id] ?: post.likes,
                comments = commentCountsByPostId[post.id] ?: post.comments,
            )
        )
    }
}
