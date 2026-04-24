package com.example.fitfusion.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitfusion.data.health.DailyHealthData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate

class HealthRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveDailyHealthData(data: DailyHealthData) {
        val uid = auth.currentUser?.uid ?: return
        val syncedAt = Instant.now().toString()

        val docData = hashMapOf(
            "date" to data.date,
            "steps" to data.steps,
            "stepCaloriesEstimated" to data.stepCaloriesEstimated,
            "averageHeartRate" to data.averageHeartRate,
            "source" to data.source,
            "syncedAt" to syncedAt,
        )
        val summaryData = hashMapOf(
            "date" to data.date,
            "steps" to data.steps,
            "stepCaloriesEstimated" to data.stepCaloriesEstimated,
            "averageHeartRate" to data.averageHeartRate,
            "healthSource" to data.source,
            "healthSyncedAt" to syncedAt,
            "updatedAt" to syncedAt,
        )

        val healthRef = firestore.collection("users")
            .document(uid)
            .collection("healthDaily")
            .document(data.date)  // usa la fecha como ID -> idempotente

        val summaryRef = firestore.collection("users")
            .document(uid)
            .collection("dailySummaries")
            .document(data.date)

        firestore.batch()
            .set(healthRef, docData)
            .set(summaryRef, summaryData, SetOptions.merge())
            .commit()
            .await()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getDailyHealthData(date: LocalDate): DailyHealthData? {
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("healthDaily")
            .document(date.toString())
            .get()
            .await()

        return snapshot.toDailyHealthDataOrNull(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun listenDailyHealthData(
        date: LocalDate,
        onData: (DailyHealthData?) -> Unit,
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onData(null)
            return null
        }

        return firestore.collection("users")
            .document(uid)
            .collection("healthDaily")
            .document(date.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onData(null)
                    return@addSnapshotListener
                }
                onData(snapshot.toDailyHealthDataOrNull(date))
            }
    }

    private fun DocumentSnapshot.toDailyHealthDataOrNull(date: LocalDate): DailyHealthData? {
        if (!exists()) return null
        return DailyHealthData(
            date = getString("date") ?: date.toString(),
            steps = getLong("steps") ?: 0L,
            stepCaloriesEstimated = getLong("stepCaloriesEstimated")?.toInt() ?: 0,
            averageHeartRate = getLong("averageHeartRate"),
            source = getString("source") ?: "health_connect",
        )
    }
}
