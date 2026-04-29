package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitfusion.data.workout.ActiveWorkoutManager
import com.example.fitfusion.data.workout.ActiveWorkoutSession
import kotlinx.coroutines.flow.StateFlow

class ActiveWorkoutViewModel : ViewModel() {

    val session: StateFlow<ActiveWorkoutSession?> = ActiveWorkoutManager.session
    val elapsedSeconds: StateFlow<Long> = ActiveWorkoutManager.elapsedSeconds

    fun pause()  = ActiveWorkoutManager.pause()
    fun resume() = ActiveWorkoutManager.resume()
    fun cancel() = ActiveWorkoutManager.cancelSession()

    fun renameSession(name: String) = ActiveWorkoutManager.renameSession(name)
    fun removeExercise(exerciseDocumentId: String) =
        ActiveWorkoutManager.removeExercise(exerciseDocumentId)
    fun addSet(exerciseDocumentId: String) =
        ActiveWorkoutManager.addSet(exerciseDocumentId)
    fun removeSet(exerciseDocumentId: String, setIndex: Int) =
        ActiveWorkoutManager.removeSet(exerciseDocumentId, setIndex)
    fun updateSetReps(exerciseDocumentId: String, setIndex: Int, reps: Int) =
        ActiveWorkoutManager.updateSetReps(exerciseDocumentId, setIndex, reps)
    fun updateSetWeight(exerciseDocumentId: String, setIndex: Int, weightKg: Int) =
        ActiveWorkoutManager.updateSetWeight(exerciseDocumentId, setIndex, weightKg)
    fun toggleSetCompleted(exerciseDocumentId: String, setIndex: Int) =
        ActiveWorkoutManager.toggleSetCompleted(exerciseDocumentId, setIndex)
}

fun formatElapsed(totalSeconds: Long): String {
    val hours   = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}
