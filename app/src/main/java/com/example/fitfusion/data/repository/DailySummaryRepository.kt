package com.example.fitfusion.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

data class DailySummary(
    val date: LocalDate,
    val kcalConsumed: Int = 0,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0,
    val workoutCount: Int = 0,
    val kcalBurned: Int = 0,
    val totalVolumeKg: Float = 0f,
    val steps: Long = 0L,
    val stepCaloriesEstimated: Int = 0,
    val averageHeartRate: Int = 0,
)

object DailySummaryRepository {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun mergeFoodSummary(
        date: LocalDate,
        kcalConsumed: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .collection("dailySummaries").document(date.toString())
            .set(
                mapOf(
                    "date"         to date.toString(),
                    "kcalConsumed" to kcalConsumed,
                    "proteinG"     to proteinG,
                    "carbsG"       to carbsG,
                    "fatG"         to fatG,
                    "updatedAt"    to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    suspend fun mergeWorkoutSummary(
        date: LocalDate,
        workoutCount: Int,
        kcalBurned: Int,
        totalVolumeKg: Float,
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .collection("dailySummaries").document(date.toString())
            .set(
                mapOf(
                    "date"          to date.toString(),
                    "workoutCount"  to workoutCount,
                    "kcalBurned"    to kcalBurned,
                    "totalVolumeKg" to totalVolumeKg.toDouble(),
                    "updatedAt"     to System.currentTimeMillis(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    fun listenDay(
        date: LocalDate,
        onUpdate: (DailySummary) -> Unit,
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onUpdate(DailySummary(date))
            return null
        }
        return firestore.collection("users").document(uid)
            .collection("dailySummaries").document(date.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(DailySummary(date))
                    return@addSnapshotListener
                }
                onUpdate(snapshot.toDailySummary(date))
            }
    }

    fun listenWeek(
        weekStart: LocalDate,
        onUpdate: (Map<LocalDate, DailySummary>) -> Unit,
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onUpdate(emptyMap())
            return null
        }
        val weekEnd = weekStart.plusDays(6)
        return firestore.collection("users").document(uid)
            .collection("dailySummaries")
            .whereGreaterThanOrEqualTo("date", weekStart.toString())
            .whereLessThanOrEqualTo("date", weekEnd.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyMap())
                    return@addSnapshotListener
                }
                val map = snapshot.documents.mapNotNull { doc ->
                    val dateStr = doc.getString("date") ?: return@mapNotNull null
                    val parsed = runCatching { LocalDate.parse(dateStr) }.getOrNull()
                        ?: return@mapNotNull null
                    parsed to doc.toDailySummary(parsed)
                }.toMap()
                onUpdate(map)
            }
    }

    private fun DocumentSnapshot.toDailySummary(date: LocalDate): DailySummary {
        if (!exists()) return DailySummary(date)
        return DailySummary(
            date                  = date,
            kcalConsumed          = (getLong("kcalConsumed") ?: 0L).toInt(),
            proteinG              = (getLong("proteinG") ?: 0L).toInt(),
            carbsG                = (getLong("carbsG") ?: 0L).toInt(),
            fatG                  = (getLong("fatG") ?: 0L).toInt(),
            workoutCount          = (getLong("workoutCount") ?: 0L).toInt(),
            kcalBurned            = (getLong("kcalBurned") ?: 0L).toInt(),
            totalVolumeKg         = (getDouble("totalVolumeKg") ?: 0.0).toFloat(),
            steps                 = getLong("steps") ?: 0L,
            stepCaloriesEstimated = (getLong("stepCaloriesEstimated") ?: 0L).toInt(),
            averageHeartRate      = (getLong("averageHeartRate") ?: 0L).toInt(),
        )
    }
}
