package com.example.fitfusion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.UserProfile
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FollowListUiState(
    val isLoading: Boolean = true,
    val profiles: List<UserProfile> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Alimenta [com.example.fitfusion.ui.screens.PantallaFollowList]: lee los uids
 * de la subcolección `followers` o `following` de un usuario y resuelve cada uno
 * a su [UserProfile]. Las cargas de perfil van en paralelo.
 */
class FollowListViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val repo = UserRepository()

    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState: StateFlow<FollowListUiState> = _uiState.asStateFlow()

    private var loadedKey: String? = null

    fun load(uid: String, mode: String) {
        val key = "$uid#$mode"
        if (loadedKey == key) return
        loadedKey = key

        if (uid.isBlank()) {
            _uiState.value = FollowListUiState(isLoading = false, errorMessage = "Usuario no válido")
            return
        }
        // mode: "followers" | "following"
        val subcollection = if (mode == "following") "following" else "followers"
        _uiState.value = FollowListUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val ids = firestore.collection("users").document(uid)
                    .collection(subcollection)
                    .limit(200)
                    .get()
                    .await()
                    .documents.map { it.id }

                val profiles = coroutineScope {
                    ids.map { id ->
                        async { runCatching { repo.getUserProfile(id) }.getOrNull() }
                    }.awaitAll().filterNotNull()
                }.sortedBy { it.displayName.lowercase() }

                _uiState.update { it.copy(isLoading = false, profiles = profiles) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "No se pudo cargar la lista")
                }
            }
        }
    }
}
