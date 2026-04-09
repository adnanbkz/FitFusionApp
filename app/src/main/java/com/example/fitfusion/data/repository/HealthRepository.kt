package com.exemple.fitfusion.app.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.exemple.fitfusion.app.data.health.DailyHealthData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant

class HealthRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveDailyHealthData(data: DailyHealthData) {
        val uid = auth.currentUser?.uid ?: return

        val docData = hashMapOf(
            "date" to data.date,
            "steps" to data.steps,
            "stepCaloriesEstimated" to data.stepCaloriesEstimated,
            "averageHeartRate" to data.averageHeartRate,
            "source" to data.source,
            "syncedAt" to Instant.now().toString(),
        )

        // Escribe en users/{uid}/healthDaily/{yyyy-MM-dd}
        firestore.collection("users")
            .document(uid)
            .collection("healthDaily")
            .document(data.date)  // usa la fecha como ID → idempotente
            .set(docData)
    }
}