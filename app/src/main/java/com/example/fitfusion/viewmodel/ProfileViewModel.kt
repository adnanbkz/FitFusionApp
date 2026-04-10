package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val displayName: String = "Alex Rivera",
    val handle: String = "@alex_kinetic",
    val bio: String = "Elite marathon runner & plant-based nutrition coach. Helping athletes unlock 110% through data-driven performance. \uD83C\uDF3F⚡",
    val postCount: String = "POSTS",
    val followers: String = "2.8k FOLLOWERS",
    val following: String = "492 FOLLOWING",
    val weeklyChange: String = "+12%",
    val weeklyValues: List<Float> = listOf(0.3f, 0.4f, 0.5f, 1f, 0.6f, 0.35f, 0.45f),
    val selectedTab: Int = 0,
    val photoCount: Int = 5
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onTabSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun updateFromUser(userName: String?) {
        if (userName != null) {
            _uiState.value = _uiState.value.copy(
                displayName = userName,
                handle = "@${userName.lowercase()}_kinetic"
            )
        }
    }

    // TODO: Load profile, posts, stats from Firestore
}