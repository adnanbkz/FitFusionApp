package com.example.fitfusion.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DataStorageUiState(
    // Device storage
    val deviceTotalBytes: Long = 0L,
    val deviceFreeBytes: Long = 0L,
    // App storage
    val appDataBytes: Long = 0L,
    val appCacheBytes: Long = 0L,
    // Firestore summary
    val healthDayCount: Int = 0,
    // States
    val isLoading: Boolean = true,
    val isClearingCache: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
) {
    val deviceUsedBytes: Long get() = deviceTotalBytes - deviceFreeBytes
    val appTotalBytes: Long get() = appDataBytes + appCacheBytes
}

class DataStorageViewModel(private val app: Application) : AndroidViewModel(app) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DataStorageUiState())
    val uiState: StateFlow<DataStorageUiState> = _uiState.asStateFlow()

    // One-shot event: Screen observes this to launch the share chooser
    private val _shareEvent = MutableSharedFlow<Intent>(replay = 0)
    val shareEvent = _shareEvent.asSharedFlow()

    init {
        loadStorageInfo()
    }

    // ── Storage info ─────────────────────────────────────────────────

    fun loadStorageInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val (total, free) = withContext(Dispatchers.IO) { readDeviceStorage() }
            val (dataBytes, cacheBytes) = withContext(Dispatchers.IO) { readAppStorage() }
            val healthCount = readHealthDayCount()

            _uiState.update {
                it.copy(
                    deviceTotalBytes = total,
                    deviceFreeBytes = free,
                    appDataBytes = dataBytes,
                    appCacheBytes = cacheBytes,
                    healthDayCount = healthCount,
                    isLoading = false,
                )
            }
        }
    }

    private fun readDeviceStorage(): Pair<Long, Long> {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
            Pair(total, free)
        } catch (e: Exception) {
            Pair(0L, 0L)
        }
    }

    private fun readAppStorage(): Pair<Long, Long> {
        val dataBytes = dirSizeBytes(app.filesDir)
        val cacheBytes = dirSizeBytes(app.cacheDir) +
            (app.externalCacheDir?.let { dirSizeBytes(it) } ?: 0L)
        return Pair(dataBytes, cacheBytes)
    }

    private fun dirSizeBytes(dir: File): Long =
        dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    private suspend fun readHealthDayCount(): Int {
        val uid = auth.currentUser?.uid ?: return 0
        return try {
            val snap = firestore.collection("users").document(uid)
                .collection("healthDaily").get().await()
            snap.size()
        } catch (e: Exception) {
            0
        }
    }

    // ── Cache ─────────────────────────────────────────────────────────

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingCache = true) }
            withContext(Dispatchers.IO) {
                app.cacheDir.deleteRecursively()
                app.externalCacheDir?.deleteRecursively()
                // Recreate so the app keeps working
                app.cacheDir.mkdirs()
            }
            val (dataBytes, cacheBytes) = withContext(Dispatchers.IO) { readAppStorage() }
            _uiState.update {
                it.copy(
                    isClearingCache = false,
                    appDataBytes = dataBytes,
                    appCacheBytes = cacheBytes,
                )
            }
        }
    }

    // ── Export ────────────────────────────────────────────────────────

    fun exportData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccess = false) }
            try {
                val json = buildExportJson(uid)
                val dateTag = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_SUBJECT, "FitFusion — mis datos $dateTag")
                    putExtra(Intent.EXTRA_TEXT, json)
                }
                _uiState.update { it.copy(isExporting = false, exportSuccess = true) }
                _shareEvent.emit(Intent.createChooser(intent, "Exportar mis datos"))
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isExporting = false, deleteError = e.localizedMessage)
                }
            }
        }
    }

    private suspend fun buildExportJson(uid: String): String {
        val root = JSONObject()
        val dateTag = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        root.put("exportDate", dateTag)
        root.put("userId", uid)

        // User profile
        val profileSnap = firestore.collection("users").document(uid).get().await()
        root.put("profile", JSONObject(profileSnap.data ?: emptyMap<String, Any>()))

        // Health daily
        val healthSnap = firestore.collection("users").document(uid)
            .collection("healthDaily").get().await()
        val healthArray = JSONArray()
        healthSnap.documents.forEach { doc ->
            healthArray.put(JSONObject(doc.data ?: emptyMap<String, Any>()))
        }
        root.put("healthDaily", healthArray)

        return root.toString(2)
    }

    fun dismissExportSuccess() = _uiState.update { it.copy(exportSuccess = false) }

    // ── Delete data ───────────────────────────────────────────────────

    fun showDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun dismissDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun deleteHealthData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, showDeleteConfirm = false, deleteError = null) }
            try {
                // Batch delete all healthDaily documents
                val snap = firestore.collection("users").document(uid)
                    .collection("healthDaily").get().await()

                val batch = firestore.batch()
                snap.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()

                _uiState.update {
                    it.copy(isDeleting = false, healthDayCount = 0)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isDeleting = false, deleteError = e.localizedMessage)
                }
            }
        }
    }

    fun dismissDeleteError() = _uiState.update { it.copy(deleteError = null) }
}

// ── Formatting helper ─────────────────────────────────────────────────────────

fun Long.formatBytes(): String = when {
    this >= 1_073_741_824L -> "%.1f GB".format(this / 1_073_741_824.0)
    this >= 1_048_576L     -> "%.0f MB".format(this / 1_048_576.0)
    this >= 1_024L         -> "%.0f KB".format(this / 1_024.0)
    else                   -> "$this B"
}
