package com.example.fitfusion.data.repository

import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.models.WorkoutSet
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val USERS_COLLECTION = "users"
private const val WORKOUTS_COLLECTION = "workouts"

object WorkoutRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _workouts = MutableStateFlow<Map<LocalDate, List<LoggedWorkout>>>(emptyMap())
    val workouts: StateFlow<Map<LocalDate, List<LoggedWorkout>>> = _workouts.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        attachWorkoutListener(firebaseAuth.currentUser?.uid)
    }
    private var workoutListenerRegistration: ListenerRegistration? = null
    private var currentUid: String? = null
    private var authListenerRegistered = false

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        ensureInitialized()
    }

    fun ensureInitialized() {
        if (authListenerRegistered) return
        auth.addAuthStateListener(authListener)
        authListenerRegistered = true
        attachWorkoutListener(auth.currentUser?.uid)
    }

    fun getWorkoutsForDate(date: LocalDate): List<LoggedWorkout> =
        _workouts.value[date] ?: emptyList()

    suspend fun addWorkout(
        workout: LoggedWorkout,
        description: String = "",
        mediaUrls: List<String> = emptyList(),
    ) {
        saveWorkout(workout.copy(description = description, mediaUrls = mediaUrls))
    }

    suspend fun updateWorkout(workout: LoggedWorkout) {
        saveWorkout(workout)
    }

    suspend fun updateWorkoutMedia(workoutId: String, mediaUrls: List<String>) {
        ensureInitialized()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WORKOUTS_COLLECTION)
            .document(workoutId)
            .update("mediaUrls", mediaUrls)
            .await()
    }

    private suspend fun saveWorkout(workout: LoggedWorkout) {
        ensureInitialized()
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Inicia sesion para guardar entrenamientos.")

        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WORKOUTS_COLLECTION)
            .document(workout.id)
            .set(workout.toFirestoreMap())
            .await()

        pushWorkoutSummary(workout.date)
    }

    fun removeWorkout(id: String, date: LocalDate) {
        ensureInitialized()
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WORKOUTS_COLLECTION)
            .document(id)
            .delete()
        pushWorkoutSummary(date)
    }

    private fun pushWorkoutSummary(date: LocalDate) {
        val workouts = getWorkoutsForDate(date)
        CoroutineScope(Dispatchers.IO).launch {
            DailySummaryRepository.mergeWorkoutSummary(
                date          = date,
                workoutCount  = workouts.size,
                kcalBurned    = workouts.sumOf { it.kcalBurned },
                totalVolumeKg = workouts.sumOf { it.totalVolumeKg.toDouble() }.toFloat(),
            )
        }
    }

    private fun attachWorkoutListener(uid: String?) {
        if (uid == currentUid && workoutListenerRegistration != null) return

        workoutListenerRegistration?.remove()
        workoutListenerRegistration = null
        currentUid = uid

        if (uid.isNullOrBlank()) {
            _workouts.value = emptyMap()
            return
        }

        workoutListenerRegistration = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(WORKOUTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val parsedWorkouts = snapshot.documents
                    .mapNotNull { document -> document.toLoggedWorkoutOrNull() }
                    .sortedWith(
                        compareByDescending<LoggedWorkout> { it.date }
                            .thenByDescending { it.createdAtMs ?: 0L }
                    )

                _workouts.value = parsedWorkouts.groupBy { it.date }
            }
    }

    private fun LoggedWorkout.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "dateKey" to date.format(dateFormatter),
        "name" to name,
        "emoji" to emoji,
        "durationMinutes" to durationMinutes,
        "kcalBurned" to kcalBurned,
        "exerciseCount" to exerciseCount,
        "totalSets" to totalSets,
        "startedAtMs" to startedAtMs,
        "endedAtMs" to endedAtMs,
        "createdAtMs" to (createdAtMs ?: System.currentTimeMillis()),
        "description" to description,
        "mediaUrls" to mediaUrls,
        "exercises" to exercises.map { exercise ->
            mapOf(
                "exerciseDocumentId" to exercise.exerciseDocumentId,
                "exerciseSlug" to exercise.exerciseSlug,
                "name" to exercise.name,
                "muscleGroup" to exercise.muscleGroup,
                "sets" to exercise.sets.map { set ->
                    mapOf(
                        "reps" to set.reps,
                        "weightKg" to set.weightKg,
                    )
                }
            )
        }
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toLoggedWorkoutOrNull(): LoggedWorkout? {
        val dateKey = getString("dateKey") ?: return null
        val date = runCatching { LocalDate.parse(dateKey, dateFormatter) }.getOrNull() ?: return null

        val exercises = (get("exercises") as? List<*>)
            .orEmpty()
            .mapNotNull { rawExercise -> (rawExercise as? Map<*, *>)?.toWorkoutExerciseOrNull() }

        return LoggedWorkout(
            id = id,
            date = date,
            name = getString("name").orEmpty().ifBlank { "Entrenamiento" },
            emoji = getString("emoji").orEmpty().ifBlank { "🏋️" },
            durationMinutes = getLong("durationMinutes")?.toInt() ?: 0,
            kcalBurned = getLong("kcalBurned")?.toInt() ?: 0,
            startedAtMs = getLong("startedAtMs"),
            endedAtMs = getLong("endedAtMs"),
            createdAtMs = getLong("createdAtMs"),
            exercises = exercises,
            description = getString("description").orEmpty(),
            mediaUrls = (get("mediaUrls") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
        )
    }

    private fun Map<*, *>.toWorkoutExerciseOrNull(): WorkoutExercise? {
        val name = (this["name"] as? String).orEmpty().trim()
        if (name.isBlank()) return null

        val sets = (this["sets"] as? List<*>)
            .orEmpty()
            .mapNotNull { rawSet -> (rawSet as? Map<*, *>)?.toWorkoutSetOrNull() }

        return WorkoutExercise(
            exerciseDocumentId = (this["exerciseDocumentId"] as? String)?.trim()?.ifBlank { null },
            exerciseSlug = (this["exerciseSlug"] as? String)?.trim()?.ifBlank { null },
            name = name,
            muscleGroup = (this["muscleGroup"] as? String).orEmpty().ifBlank { "Other" },
            sets = sets,
        )
    }

    private fun Map<*, *>.toWorkoutSetOrNull(): WorkoutSet? {
        val reps = (this["reps"] as? Number)?.toInt() ?: return null
        val weightKg = (this["weightKg"] as? Number)?.toFloat() ?: 0f
        return WorkoutSet(reps = reps, weightKg = weightKg)
    }
}
