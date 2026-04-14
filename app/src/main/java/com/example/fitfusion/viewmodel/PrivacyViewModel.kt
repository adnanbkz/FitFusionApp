package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    val blockedUsers: List<String> = listOf("user_spammer", "old_account_123")
)

class PrivacyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

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

    fun unblockUser(username: String) =
        _uiState.update { it.copy(blockedUsers = it.blockedUsers - username) }
}