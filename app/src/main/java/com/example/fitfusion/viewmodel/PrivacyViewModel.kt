package com.example.fitfusion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitfusion.data.repository.BlockedUser
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VisibilityOption(val label: String) {
    PUBLIC("Público"),
    FRIENDS("Solo amigos"),
    ONLY_ME("Solo yo")
}

data class PrivacyUiState(
    val profileVisibility: VisibilityOption = VisibilityOption.PUBLIC,
    val activityVisibility: VisibilityOption = VisibilityOption.FRIENDS,
    val whoCanMessage: VisibilityOption = VisibilityOption.FRIENDS,
    val showInSearch: Boolean = true,
    val allowTagging: Boolean = true,
    val shareLocation: Boolean = false,
    val blockedUsers: List<BlockedUser> = emptyList(),
    val isLoadingBlocked: Boolean = false,
)

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        loadBlockedUsers()
    }

    private fun loadBlockedUsers() {
        val uid = myUid ?: return
        _uiState.update { it.copy(isLoadingBlocked = true) }
        viewModelScope.launch {
            val blocked = runCatching { repo.getBlockedUsers(uid) }.getOrDefault(emptyList())
            _uiState.update { it.copy(blockedUsers = blocked, isLoadingBlocked = false) }
        }
    }

    fun unblockUser(targetUid: String) {
        val uid = myUid ?: return
        _uiState.update { it.copy(blockedUsers = it.blockedUsers.filter { b -> b.uid != targetUid }) }
        viewModelScope.launch {
            runCatching { repo.unblockUser(uid, targetUid) }
        }
    }

    fun onProfileVisibilityChange(option: VisibilityOption) =
        _uiState.update { it.copy(profileVisibility = option) }

    fun onActivityVisibilityChange(option: VisibilityOption) =
        _uiState.update { it.copy(activityVisibility = option) }

    fun onWhoCanMessageChange(option: VisibilityOption) =
        _uiState.update { it.copy(whoCanMessage = option) }

    fun onShowInSearchChange(value: Boolean) =
        _uiState.update { it.copy(showInSearch = value) }

    fun onAllowTaggingChange(value: Boolean) =
        _uiState.update { it.copy(allowTagging = value) }

    fun onShareLocationChange(value: Boolean) =
        _uiState.update { it.copy(shareLocation = value) }
}
