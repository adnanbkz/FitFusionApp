package com.example.fitfusion.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.Locale

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val username: String,
    val bio: String = "",
    val photoUrl: String? = null,
    val heightCm: Int? = null,
    val weightKg: Float? = null,
    val goalType: String? = null,
    val activityLevel: String? = null,
    val birthDate: String? = null,
    val isOnboarded: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
)

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun createUserProfile(
        uid: String,
        email: String,
        displayName: String,
        photoUrl: String? = null,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val userDocument = hashMapOf<String, Any?>(
            "email" to email,
            "displayName" to displayName,
            "displayNameLower" to displayName.lowercase(Locale.getDefault()),
            "username" to defaultUsername(displayName),
            "bio" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "photoUrl" to photoUrl,
            "heightCm" to null,
            "weightKg" to null,
            "goalType" to null,
            "activityLevel" to null,
            "birthDate" to null,
            "isOnboarded" to false,
            "followersCount" to 0,
            "followingCount" to 0,
        )

        firestore.collection("users")
            .document(uid)
            .set(userDocument)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onError(exception) }
    }

    suspend fun getUserProfile(uid: String, fallbackEmail: String = ""): UserProfile {
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.toUserProfile(uid, fallbackEmail)
    }

    fun listenUserProfile(
        uid: String,
        fallbackEmail: String = "",
        onProfile: (UserProfile?) -> Unit,
        onError: ((Exception) -> Unit)? = null,
    ): ListenerRegistration {
        android.util.Log.d("UserRepository", "listenUserProfile setting up listener for uid='$uid'")
        return firestore.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("UserRepository", "listenUserProfile error uid=$uid", error)
                    onError?.invoke(error)
                    return@addSnapshotListener
                }
                android.util.Log.d("UserRepository", "snapshot received: exists=${snapshot?.exists()} uid=$uid")
                onProfile(snapshot?.toUserProfile(uid, fallbackEmail))
            }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val data = mutableMapOf<String, Any?>(
            "email" to profile.email,
            "displayName" to profile.displayName,
            "displayNameLower" to profile.displayName.lowercase(Locale.getDefault()),
            "username" to normalizeUsername(profile.username, profile.displayName),
            "bio" to profile.bio,
            "photoUrl" to profile.photoUrl,
            "heightCm" to profile.heightCm,
            "weightKg" to profile.weightKg,
            "goalType" to profile.goalType,
            "activityLevel" to profile.activityLevel,
            "updatedAt" to FieldValue.serverTimestamp(),
        )
        firestore.collection("users")
            .document(profile.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun searchUsers(query: String, currentUid: String? = null): List<UserProfile> {
        val q = query.trim().lowercase(Locale.getDefault()).ifBlank { return emptyList() }
        val end = q + ""

        val usernameResults = runCatching {
            firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", "@$q")
                .whereLessThanOrEqualTo("username", "@$end")
                .limit(10)
                .get().await().documents
        }.getOrDefault(emptyList())

        val displayNameResults = runCatching {
            firestore.collection("users")
                .whereGreaterThanOrEqualTo("displayNameLower", q)
                .whereLessThanOrEqualTo("displayNameLower", end)
                .limit(10)
                .get().await().documents
        }.getOrDefault(emptyList())

        if (usernameResults.isEmpty() && displayNameResults.isEmpty()) {
            firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", "@$q")
                .whereLessThanOrEqualTo("username", "@$end")
                .limit(1)
                .get().await()
        }

        val seen = mutableSetOf<String>()
        return (usernameResults + displayNameResults)
            .mapNotNull { doc ->
                val uid = doc.id
                if (uid == currentUid) return@mapNotNull null
                if (!seen.add(uid)) return@mapNotNull null
                doc.toUserProfile(uid, "")
            }
            .take(10)
    }

    suspend fun markOnboarded(uid: String) {
        firestore.collection("users")
            .document(uid)
            .set(mapOf("isOnboarded" to true, "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
    }

    suspend fun saveOnboardingData(
        uid: String,
        heightCm: Int?,
        weightKg: Float?,
        activityLevel: String?,
        birthDate: String?,
        goalType: String?,
        username: String? = null,
        photoUrl: String? = null,
    ) {
        val data = mutableMapOf<String, Any?>(
            "heightCm"       to heightCm,
            "weightKg"       to weightKg,
            "activityLevel"  to activityLevel,
            "birthDate"      to birthDate,
            "goalType"       to goalType,
            "isOnboarded"    to true,
            "updatedAt"      to FieldValue.serverTimestamp(),
        )
        username?.let { data["username"] = it }
        photoUrl?.let { data["photoUrl"] = it }
        firestore.collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .await()
    }

    private fun DocumentSnapshot.toUserProfile(uid: String, fallbackEmail: String): UserProfile {
        val displayName = getString("displayName").orEmpty().ifBlank {
            fallbackEmail.substringBefore("@").ifBlank { "Usuario" }
        }
        return UserProfile(
            uid = uid,
            email = getString("email").orEmpty().ifBlank { fallbackEmail },
            displayName = displayName,
            username = getString("username").orEmpty().ifBlank { defaultUsername(displayName) },
            bio = getString("bio").orEmpty(),
            photoUrl = getString("photoUrl"),
            heightCm = getLong("heightCm")?.toInt(),
            weightKg = (get("weightKg") as? Number)?.toFloat(),
            goalType = getString("goalType"),
            activityLevel = getString("activityLevel"),
            birthDate = getString("birthDate"),
            isOnboarded = getBoolean("isOnboarded") ?: false,
            followersCount = getLong("followersCount")?.toInt() ?: 0,
            followingCount = getLong("followingCount")?.toInt() ?: 0,
        )
    }

    companion object {
        fun defaultUsername(displayName: String): String {
            val base = displayName
                .lowercase(Locale.getDefault())
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifBlank { "usuario" }
            return "@$base"
        }

        fun normalizeUsername(username: String, displayName: String): String {
            val raw = username.trim().removePrefix("@")
                .lowercase(Locale.getDefault())
                .replace(Regex("[^a-z0-9._]+"), "_")
                .trim('.', '_')
                .ifBlank { defaultUsername(displayName).removePrefix("@") }
            return "@$raw"
        }
    }
}
