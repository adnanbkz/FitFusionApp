package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.WeeklyRoutinePlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.DayOfWeek

class WeeklyPlanRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth           = FirebaseAuth.getInstance(),
) {
    private fun mine() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("weekly_plans")
    }

    private fun community() = firestore.collection("weekly_plans")

    fun fetchMine(onSuccess: (List<WeeklyRoutinePlan>) -> Unit, onError: (Exception) -> Unit) {
        mine()
            ?.orderBy("createdAt", Query.Direction.DESCENDING)
            ?.get()
            ?.addOnSuccessListener { snap -> onSuccess(snap.documents.mapNotNull { it.toPlan() }) }
            ?.addOnFailureListener(onError)
            ?: onSuccess(emptyList())
    }

    fun fetchCommunity(onSuccess: (List<WeeklyRoutinePlan>) -> Unit, onError: (Exception) -> Unit) {
        community()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snap -> onSuccess(snap.documents.mapNotNull { it.toPlan() }) }
            .addOnFailureListener(onError)
    }

    fun save(
        plan: WeeklyRoutinePlan,
        authorName: String?,
        onSuccess: (WeeklyRoutinePlan) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) { onError(Exception("No autenticado")); return }
        val personal = mine()
        if (personal == null) { onError(Exception("No autenticado")); return }

        val finalPlan = plan.copy(authorId = uid, authorName = authorName)
        personal.document(finalPlan.id).set(finalPlan.toMap())
            .addOnSuccessListener {
                if (finalPlan.isPublic) {
                    community().document(finalPlan.id).set(finalPlan.toMap())
                        .addOnSuccessListener { onSuccess(finalPlan) }
                        .addOnFailureListener(onError)
                } else {
                    onSuccess(finalPlan)
                }
            }
            .addOnFailureListener(onError)
    }

    fun saveFromCommunity(plan: WeeklyRoutinePlan, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val personal = mine()
        if (personal == null) { onError(Exception("No autenticado")); return }
        val copy = plan.copy(isPublic = false, createdAt = System.currentTimeMillis())
        personal.document(copy.id).set(copy.toMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun delete(planId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        mine()
            ?.document(planId)
            ?.delete()
            ?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener(onError)
            ?: onError(Exception("No autenticado"))
    }
}

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.toPlan(): WeeklyRoutinePlan? {
    val name = getString("name")?.takeIf { it.isNotBlank() } ?: return null
    val rawDays  = get("days")            as? Map<String, String?> ?: emptyMap()
    val rawNames = get("dayRoutineNames") as? Map<String, String?> ?: emptyMap()
    val days  = rawDays.mapNotNull  { (k, v) -> runCatching { DayOfWeek.valueOf(k) }.getOrNull()?.let { it to v } }.toMap()
    val names = rawNames.mapNotNull { (k, v) -> runCatching { DayOfWeek.valueOf(k) }.getOrNull()?.let { it to v } }.toMap()
    return WeeklyRoutinePlan(
        id              = id,
        name            = name,
        emoji           = getString("emoji") ?: "📅",
        days            = days,
        dayRoutineNames = names,
        isPublic        = getBoolean("isPublic") ?: false,
        authorId        = getString("authorId"),
        authorName      = getString("authorName"),
        createdAt       = getLong("createdAt") ?: System.currentTimeMillis(),
    )
}
