package com.example.fitfusion.data.workout

import com.example.fitfusion.data.models.ExerciseCatalogItem
import com.example.fitfusion.data.models.LoggedWorkout
import com.example.fitfusion.data.models.WorkoutExercise
import com.example.fitfusion.data.models.WorkoutSet
import com.example.fitfusion.data.repository.WorkoutRepository
import com.example.fitfusion.util.MuscleTranslations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class ActiveSetEntry(
    val reps: Int = 10,
    val weightKg: Int = 0,
    val completed: Boolean = false,
)

data class ActiveExerciseEntry(
    val exerciseDocumentId: String,
    val exerciseSlug: String?,
    val name: String,
    val muscleGroup: String,
    val displayMuscleGroup: String,
    val sets: List<ActiveSetEntry>,
)

data class ActiveWorkoutSession(
    val id: String,
    val name: String,
    val startedAtMs: Long,
    val accumulatedSeconds: Long,
    val resumedAtMs: Long?,
    val isPaused: Boolean,
    val exercises: List<ActiveExerciseEntry>,
) {
    fun elapsedSeconds(nowMs: Long): Long {
        val running = if (!isPaused && resumedAtMs != null) {
            ((nowMs - resumedAtMs) / 1000L).coerceAtLeast(0L)
        } else 0L
        return accumulatedSeconds + running
    }

    val exerciseCount: Int get() = exercises.size
    val totalSets: Int get() = exercises.sumOf { it.sets.size }
}

object ActiveWorkoutManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null

    private val _session = MutableStateFlow<ActiveWorkoutSession?>(null)
    val session: StateFlow<ActiveWorkoutSession?> = _session.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    val isActive: Boolean get() = _session.value != null

    fun startSession(
        name: String,
        exercises: List<ExerciseCatalogItem>,
    ) {
        if (_session.value != null) return
        val now = System.currentTimeMillis()
        val resolvedName = name.ifBlank { buildAutoName(exercises) }
        _session.value = ActiveWorkoutSession(
            id                 = UUID.randomUUID().toString(),
            name               = resolvedName,
            startedAtMs        = now,
            accumulatedSeconds = 0L,
            resumedAtMs        = now,
            isPaused           = false,
            exercises          = exercises.map { it.toActiveEntry() },
        )
        _elapsedSeconds.value = 0L
        startTicker()
    }

    fun renameSession(name: String) {
        _session.update { current ->
            current?.copy(name = name)
        }
    }

    fun pause() {
        val now = System.currentTimeMillis()
        _session.update { current ->
            if (current == null || current.isPaused) return@update current
            val accumulated = current.elapsedSeconds(now)
            current.copy(
                accumulatedSeconds = accumulated,
                resumedAtMs        = null,
                isPaused           = true,
            )
        }
        tickerJob?.cancel()
        tickerJob = null
        _session.value?.let { _elapsedSeconds.value = it.accumulatedSeconds }
    }

    fun resume() {
        _session.update { current ->
            if (current == null || !current.isPaused) return@update current
            current.copy(
                resumedAtMs = System.currentTimeMillis(),
                isPaused    = false,
            )
        }
        startTicker()
    }

    fun addExercise(exercise: ExerciseCatalogItem) {
        _session.update { current ->
            if (current == null) return@update current
            if (current.exercises.any { it.exerciseDocumentId == exercise.documentId }) return@update current
            current.copy(exercises = current.exercises + exercise.toActiveEntry())
        }
    }

    fun removeExercise(exerciseDocumentId: String) {
        _session.update { current ->
            if (current == null) return@update current
            current.copy(exercises = current.exercises.filter { it.exerciseDocumentId != exerciseDocumentId })
        }
    }

    fun addSet(exerciseDocumentId: String) {
        _session.update { current ->
            current?.let { it.copy(exercises = it.exercises.map { ex ->
                if (ex.exerciseDocumentId == exerciseDocumentId) {
                    val newSet = ex.sets.lastOrNull()?.copy(completed = false) ?: ActiveSetEntry()
                    if (ex.sets.size >= 10) ex else ex.copy(sets = ex.sets + newSet)
                } else ex
            }) }
        }
    }

    fun removeSet(exerciseDocumentId: String, setIndex: Int) {
        _session.update { current ->
            current?.let { it.copy(exercises = it.exercises.map { ex ->
                if (ex.exerciseDocumentId == exerciseDocumentId &&
                    setIndex in ex.sets.indices &&
                    ex.sets.size > 1) {
                    ex.copy(sets = ex.sets.filterIndexed { idx, _ -> idx != setIndex })
                } else ex
            }) }
        }
    }

    fun updateSetReps(exerciseDocumentId: String, setIndex: Int, reps: Int) {
        updateSet(exerciseDocumentId, setIndex) { it.copy(reps = reps.coerceIn(1, 50)) }
    }

    fun updateSetWeight(exerciseDocumentId: String, setIndex: Int, weightKg: Int) {
        updateSet(exerciseDocumentId, setIndex) { it.copy(weightKg = weightKg.coerceIn(0, 300)) }
    }

    fun toggleSetCompleted(exerciseDocumentId: String, setIndex: Int) {
        updateSet(exerciseDocumentId, setIndex) { it.copy(completed = !it.completed) }
    }

    private fun updateSet(
        exerciseDocumentId: String,
        setIndex: Int,
        transform: (ActiveSetEntry) -> ActiveSetEntry,
    ) {
        _session.update { current ->
            current?.let { it.copy(exercises = it.exercises.map { ex ->
                if (ex.exerciseDocumentId == exerciseDocumentId && setIndex in ex.sets.indices) {
                    ex.copy(sets = ex.sets.mapIndexed { idx, set ->
                        if (idx == setIndex) transform(set) else set
                    })
                } else ex
            }) }
        }
    }

    suspend fun finishSession(
        title: String,
        description: String,
        mediaUrls: List<String>,
    ): LoggedWorkout? {
        val current = _session.value ?: return null
        val now = System.currentTimeMillis()
        val elapsedSeconds = current.elapsedSeconds(now)
        val durationMinutes = ((elapsedSeconds + 59L) / 60L).toInt().coerceAtLeast(1)
        val resolvedTitle = title.ifBlank { current.name }
        val workout = LoggedWorkout(
            id              = current.id,
            date            = LocalDate.now(),
            name            = resolvedTitle,
            durationMinutes = durationMinutes,
            kcalBurned      = (durationMinutes * 6.5f).toInt(),
            startedAtMs     = current.startedAtMs,
            endedAtMs       = now,
            createdAtMs     = now,
            exercises       = current.exercises.map { it.toWorkoutExercise() },
        )
        WorkoutRepository.addWorkout(workout, description = description, mediaUrls = mediaUrls)
        clear()
        return workout
    }

    fun cancelSession() {
        clear()
    }

    private fun clear() {
        tickerJob?.cancel()
        tickerJob = null
        _session.value = null
        _elapsedSeconds.value = 0L
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                val current = _session.value ?: break
                if (current.isPaused) break
                _elapsedSeconds.value = current.elapsedSeconds(System.currentTimeMillis())
                delay(1_000L)
            }
        }
    }

    private fun buildAutoName(exercises: List<ExerciseCatalogItem>): String {
        if (exercises.isEmpty()) return "Entrenamiento"
        val groups = exercises.map { MuscleTranslations.translate(it.filterMuscleGroup) }
            .distinct().take(2)
        return "Fuerza — ${groups.joinToString(" + ")}"
    }

    private fun ExerciseCatalogItem.toActiveEntry() = ActiveExerciseEntry(
        exerciseDocumentId = documentId,
        exerciseSlug       = exerciseId,
        name               = name,
        muscleGroup        = MuscleTranslations.translate(displayMuscleGroup),
        displayMuscleGroup = displayMuscleGroup,
        sets               = List(3) { ActiveSetEntry() },
    )

    private fun ActiveExerciseEntry.toWorkoutExercise() = WorkoutExercise(
        exerciseDocumentId = exerciseDocumentId,
        exerciseSlug       = exerciseSlug,
        name               = name,
        muscleGroup        = muscleGroup,
        sets               = sets.map { WorkoutSet(reps = it.reps, weightKg = it.weightKg.toFloat()) },
    )
}
