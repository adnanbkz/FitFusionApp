package com.example.fitfusion.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

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
}
