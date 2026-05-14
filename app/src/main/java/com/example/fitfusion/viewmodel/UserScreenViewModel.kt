package com.example.fitfusion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.models.UserPost
import com.example.fitfusion.data.repository.UserProfile
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserScreenUiState(
    val profile: UserProfile? = null,
    val posts: List<UserPost> = emptyList(),
    val isLoading: Boolean = true,
    val isFollowing: Boolean = false,
    val errorMessage: String? = null,
)

class UserScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(UserScreenUiState())
    val uiState: StateFlow<UserScreenUiState> = _uiState.asStateFlow()

    private var profileListener: ListenerRegistration? = null
    private var postsListener: ListenerRegistration? = null

    fun load(uid: String) {
        android.util.Log.d("UserScreenVM", "load() called with uid='$uid'")
        if (uid.isBlank()) {
            android.util.Log.e("UserScreenVM", "load() called with blank uid — aborting")
            _uiState.update { it.copy(isLoading = false, errorMessage = "UID de usuario vacío") }
            return
        }
        if (_uiState.value.profile?.uid == uid) {
            android.util.Log.d("UserScreenVM", "profile already loaded for uid=$uid, skipping")
            return
        }

        profileListener?.remove()
        postsListener?.remove()

        profileListener = repo.listenUserProfile(
            uid = uid,
            onProfile = { profile ->
                android.util.Log.d("UserScreenVM", "onProfile received: ${profile?.uid} displayName=${profile?.displayName}")
                _uiState.update { it.copy(profile = profile, isLoading = false, errorMessage = null) }
            },
            onError = { e ->
                android.util.Log.e("UserScreenVM", "onError: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar el perfil",
                    )
                }
            },
        )

        postsListener = firestore.collection("posts")
            .whereEqualTo("authorId", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val posts = snapshot.documents
                    .mapNotNull { it.toUserPostOrNull() }
                    .sortedByDescending { it.timestamp }
                _uiState.update { it.copy(posts = posts) }
            }

        if (currentUid != null) {
            firestore.collection("users")
                .document(currentUid)
                .collection("following")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    _uiState.update { it.copy(isFollowing = doc.exists()) }
                }
        }
    }

    fun toggleFollow() {
        val targetUid = _uiState.value.profile?.uid ?: return
        val myUid = currentUid ?: return

        val isNowFollowing = !_uiState.value.isFollowing
        _uiState.update { it.copy(isFollowing = isNowFollowing) }

        val myFollowingRef = firestore.collection("users").document(myUid)
            .collection("following").document(targetUid)
        val theirFollowersRef = firestore.collection("users").document(targetUid)
            .collection("followers").document(myUid)

        if (isNowFollowing) {
            val ts = mapOf("followedAtMs" to System.currentTimeMillis())
            myFollowingRef.set(ts)
            theirFollowersRef.set(ts)
        } else {
            myFollowingRef.delete()
            theirFollowersRef.delete()
        }
    }

    override fun onCleared() {
        profileListener?.remove()
        postsListener?.remove()
        super.onCleared()
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toUserPostOrNull(): UserPost? {
    val typeStr = getString("type") ?: return null
    val type = when (typeStr) {
        "workout"   -> com.example.fitfusion.data.models.UserPostType.WORKOUT
        "nutrition" -> com.example.fitfusion.data.models.UserPostType.NUTRITION
        else        -> return null
    }
    return UserPost(
        id                       = id,
        type                     = type,
        caption                  = getString("caption").orEmpty(),
        workoutName              = getString("workoutName"),
        workoutDurationMinutes   = getLong("workoutDurationMinutes")?.toInt(),
        workoutKcal              = getLong("workoutKcal")?.toInt(),
        workoutVideoUri          = getString("workoutVideoUri"),
        workoutMediaUrls         = (get("workoutMediaUrls") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        workoutTotalWeightKg     = (get("workoutTotalWeightKg") as? Number)?.toFloat(),
        workoutExercises         = emptyList(),
        nutritionPhotoUri        = getString("nutritionPhotoUri"),
        nutritionKcal            = getLong("nutritionKcal")?.toInt(),
        nutritionIngredients     = getString("nutritionIngredients"),
        nutritionInstructions    = getString("nutritionInstructions"),
        nutritionCookTimeMinutes = getLong("nutritionCookTimeMinutes")?.toInt(),
        nutritionBestMoment      = getString("nutritionBestMoment"),
        timestamp                = getLong("createdAtMs") ?: 0L,
    )
}
