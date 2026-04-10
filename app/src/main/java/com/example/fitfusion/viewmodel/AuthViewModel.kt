package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null
)

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun onLoginEmailChange(value: String) {
        _loginState.value = _loginState.value.copy(email = value, errorMessage = null)
    }

    fun onLoginPasswordChange(value: String) {
        _loginState.value = _loginState.value.copy(password = value, errorMessage = null)
    }

    fun attemptLogin(onSuccess: (String) -> Unit) {
        val state = _loginState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _loginState.value = state.copy(errorMessage = "Please enter email and password")
            return
        }
        // TODO: Replace with Firebase Auth sign-in
        onSuccess(state.email.substringBefore("@"))
    }

    // ── Sign Up ──

    private val _signUpState = MutableStateFlow(SignUpUiState())
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    fun onSignUpNameChange(value: String) {
        _signUpState.value = _signUpState.value.copy(displayName = value, errorMessage = null)
    }

    fun onSignUpEmailChange(value: String) {
        _signUpState.value = _signUpState.value.copy(email = value, errorMessage = null)
    }

    fun onSignUpPasswordChange(value: String) {
        _signUpState.value = _signUpState.value.copy(password = value, errorMessage = null)
    }

    fun onSignUpConfirmPasswordChange(value: String) {
        _signUpState.value = _signUpState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun attemptSignUp(onSuccess: (String) -> Unit) {
        val state = _signUpState.value
        when {
            state.displayName.isBlank() || state.email.isBlank() ||
                    state.password.isBlank() || state.confirmPassword.isBlank() -> {
                _signUpState.value = state.copy(errorMessage = "Please fill in all fields")
            }
            state.password != state.confirmPassword -> {
                _signUpState.value = state.copy(errorMessage = "Passwords do not match")
            }
            state.password.length < 6 -> {
                _signUpState.value = state.copy(errorMessage = "Password must be at least 6 characters")
            }
            else -> {
                // TODO: Replace with Firebase Auth sign-up
                onSuccess(state.displayName)
            }
        }
    }

    fun clearLoginError() { _loginState.value = _loginState.value.copy(errorMessage = null) }
    fun clearSignUpError() { _signUpState.value = _signUpState.value.copy(errorMessage = null) }
}