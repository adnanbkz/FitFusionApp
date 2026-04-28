package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.Routine
import com.example.fitfusion.data.models.ScheduledRoutine
import com.example.fitfusion.data.models.WeeklyRoutinePlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class ScheduleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth           = FirebaseAuth.getInstance(),
) {
    private fun collection() = auth.currentUser?.uid?.let {
        firestore.collection("users").document(it).collection("schedule")
    }

    fun fetchForDate(
        date: LocalDate,
        onSuccess: (ScheduledRoutine?) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val col = collection()
        if (col == null) { onSuccess(null); return }
        col.document(date.toString()).get()
            .addOnSuccessListener { doc -> onSuccess(doc.toScheduledRoutine()) }
            .addOnFailureListener(onError)
    }

    fun fetchForWeek(
        weekStart: LocalDate,
        onSuccess: (Map<LocalDate, ScheduledRoutine>) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val col = collection()
        if (col == null) { onSuccess(emptyMap()); return }
        val ids = (0..6).map { weekStart.plusDays(it.toLong()).toString() }
        col.whereIn("date", ids).get()
            .addOnSuccessListener { snap ->
                val items = snap.documents.mapNotNull { it.toScheduledRoutine() }
                onSuccess(items.associateBy { it.date })
            }
            .addOnFailureListener(onError)
    }

    fun assignRoutine(
        date: LocalDate,
        routine: Routine,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val col = collection()
        if (col == null) { onError(Exception("No autenticado")); return }
        val scheduled = ScheduledRoutine(
            date         = date,
            routineId    = routine.id,
            routineName  = routine.name,
            emoji        = routine.emoji,
        )
        col.document(date.toString()).set(scheduled.toMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun clearDate(date: LocalDate, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val col = collection()
        if (col == null) { onError(Exception("No autenticado")); return }
        col.document(date.toString()).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun applyWeeklyPlan(
        plan: WeeklyRoutinePlan,
        weekStart: LocalDate,
        routineLookup: Map<String, Routine>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val col = collection()
        if (col == null) { onError(Exception("No autenticado")); return }
        val monday = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val batch  = firestore.batch()

        DayOfWeek.entries.forEach { dow ->
            val date = monday.plusDays((dow.value - 1).toLong())
            val docRef = col.document(date.toString())
            val routineId = plan.days[dow]
            if (routineId != null) {
                val cachedName = plan.dayRoutineNames[dow]
                val routine    = routineLookup[routineId]
                val scheduled  = ScheduledRoutine(
                    date        = date,
                    routineId   = routineId,
                    routineName = routine?.name ?: cachedName ?: "Rutina",
                    emoji       = routine?.emoji ?: "💪",
                )
                batch.set(docRef, scheduled.toMap())
            } else {
                batch.delete(docRef)
            }
        }

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }
}

private fun DocumentSnapshot.toScheduledRoutine(): ScheduledRoutine? {
    if (!exists()) return null
    val dateStr     = getString("date")        ?: return null
    val routineId   = getString("routineId")   ?: return null
    val routineName = getString("routineName") ?: return null
    val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return null
    return ScheduledRoutine(
        date        = date,
        routineId   = routineId,
        routineName = routineName,
        emoji       = getString("emoji") ?: "💪",
    )
}
