package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.data.models.RoutineExercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RoutineRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth           = FirebaseAuth.getInstance(),
) {
    private fun mine() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("routines")
    }

    private fun community() = firestore.collection("routines")

    fun fetchMine(onSuccess: (List<Routine>) -> Unit, onError: (Exception) -> Unit) {
        mine()
            ?.orderBy("createdAt", Query.Direction.DESCENDING)
            ?.get()
            ?.addOnSuccessListener { snap -> onSuccess(snap.documents.mapNotNull { it.toRoutine() }) }
            ?.addOnFailureListener(onError)
            ?: onSuccess(emptyList())
    }

    fun fetchCommunity(onSuccess: (List<Routine>) -> Unit, onError: (Exception) -> Unit) {
        community()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snap -> onSuccess(snap.documents.mapNotNull { it.toRoutine() }) }
            .addOnFailureListener(onError)
    }

    fun save(
        routine: Routine,
        authorName: String?,
        onSuccess: (Routine) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) { onError(Exception("No autenticado")); return }
        val personal = mine()
        if (personal == null) { onError(Exception("No autenticado")); return }

        val finalRoutine = routine.copy(authorId = uid, authorName = authorName)
        personal.document(finalRoutine.id).set(finalRoutine.toMap())
            .addOnSuccessListener {
                if (finalRoutine.isPublic) {
                    community().document(finalRoutine.id).set(finalRoutine.toMap())
                        .addOnSuccessListener { onSuccess(finalRoutine) }
                        .addOnFailureListener(onError)
                } else {
                    onSuccess(finalRoutine)
                }
            }
            .addOnFailureListener(onError)
    }

    fun saveFromCommunity(routine: Routine, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val personal = mine()
        if (personal == null) { onError(Exception("No autenticado")); return }
        val copy = routine.copy(isPublic = false, createdAt = System.currentTimeMillis())
        personal.document(copy.id).set(copy.toMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun delete(routineId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        mine()
            ?.document(routineId)
            ?.delete()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener(onError)
            ?: onError(Exception("No autenticado"))
    }
}

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toRoutine(): Routine? {
    val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
    val rawExercises = get("exercises") as? List<Map<String, Any?>> ?: emptyList()
    val exercises = rawExercises.mapNotNull { RoutineExercise.fromMap(it) }
    return Routine(
        id                   = id,
        name                 = name,
        emoji                = getString("emoji") ?: "💪",
        description          = getString("description") ?: "",
        exercises            = exercises,
        estimatedDurationMin = getLong("estimatedDurationMin")?.toInt(),
        isPublic             = getBoolean("isPublic") ?: false,
        authorId             = getString("authorId"),
        authorName           = getString("authorName"),
        createdAt            = getLong("createdAt") ?: System.currentTimeMillis(),
    )
}
