package com.example.fitfusion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.UserProfile
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserSearchUiState(
    val query: String = "",
    val results: List<UserProfile> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
)

class UserSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(UserSearchUiState())
    val uiState: StateFlow<UserSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value, errorMessage = null) }
        searchJob?.cancel()
        if (value.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false, hasSearched = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching { repo.searchUsers(value, currentUid) }
            result.onSuccess { results ->
                _uiState.update { it.copy(results = results, isLoading = false, hasSearched = true, errorMessage = null) }
            }
            result.onFailure { e ->
                _uiState.update {
                    it.copy(
                        results = emptyList(),
                        isLoading = false,
                        hasSearched = true,
                        errorMessage = e.localizedMessage ?: "Error al buscar usuarios",
                    )
                }
            }
        }
    }
}
