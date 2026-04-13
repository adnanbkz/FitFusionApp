package com.example.fitfusion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitfusion.data.repository.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import java.lang.Exception
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SignUpUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

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
        if (state.isLoading) return

        val email = state.email.trim()
        if (state.email.isBlank() || state.password.isBlank()) {
            _loginState.value = state.copy(errorMessage = "Please enter email and password")
            return
        }

        _loginState.value = state.copy(
            email = email,
            isLoading = true,
            errorMessage = null
        )

        auth.signInWithEmailAndPassword(email, state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginState.value = LoginUiState(email = email)
                    onSuccess(resolveDisplayName())
                } else {
                    _loginState.value = state.copy(
                        email = email,
                        isLoading = false,
                        errorMessage = authErrorMessage(task.exception)
                    )
                }
            }
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
        if (state.isLoading) return

        val displayName = state.displayName.trim()
        val email = state.email.trim()
        when {
            displayName.isBlank() || email.isBlank() ||
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
                _signUpState.value = state.copy(
                    displayName = displayName,
                    email = email,
                    isLoading = true,
                    errorMessage = null
                )

                auth.createUserWithEmailAndPassword(email, state.password)
                    .addOnCompleteListener { createTask ->
                        if (!createTask.isSuccessful) {
                            _signUpState.value = state.copy(
                                displayName = displayName,
                                email = email,
                                isLoading = false,
                                errorMessage = authErrorMessage(createTask.exception)
                            )
                            return@addOnCompleteListener
                        }

                        val currentUser = auth.currentUser
                        if (currentUser == null) {
                            _signUpState.value = state.copy(
                                displayName = displayName,
                                email = email,
                                isLoading = false,
                                errorMessage = "Account created, but the session could not be restored"
                            )
                            return@addOnCompleteListener
                        }

                        userRepository.createUserProfile(
                            uid = currentUser.uid,
                            email = email,
                            displayName = displayName,
                            photoUrl = currentUser.photoUrl?.toString(),
                            onSuccess = {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build()

                                currentUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener {
                                        _signUpState.value = SignUpUiState()
                                        onSuccess(resolveDisplayName(fallbackDisplayName = displayName))
                                    }
                            },
                            onError = {
                                rollbackFailedSignUp(
                                    currentUser = currentUser,
                                    previousState = state,
                                    displayName = displayName,
                                    email = email
                                )
                            }
                        )
                    }
            }
        }
    }

    fun clearLoginError() { _loginState.value = _loginState.value.copy(errorMessage = null) }
    fun clearSignUpError() { _signUpState.value = _signUpState.value.copy(errorMessage = null) }

    fun getSignedInDisplayName(): String? {
        return auth.currentUser?.let { user ->
            resolveDisplayName(fallbackDisplayName = user.email?.substringBefore("@"))
        }
    }

    fun isUserSignedIn(): Boolean = auth.currentUser != null


    fun signOut() {
        auth.signOut()
        _loginState.value = LoginUiState()
        _signUpState.value = SignUpUiState()
    }

    private fun resolveDisplayName(fallbackDisplayName: String? = null): String {
        val currentUser = auth.currentUser
        return currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackDisplayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")
            ?: "User"
    }

    private fun authErrorMessage(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
            is FirebaseAuthInvalidUserException -> "No account found with that email"
            is FirebaseAuthUserCollisionException -> "An account with that email already exists"
            is FirebaseAuthWeakPasswordException -> "Password must be at least 6 characters"
            is FirebaseTooManyRequestsException -> "Too many attempts. Try again later"
            is FirebaseNetworkException -> "Network error. Check your connection and try again"
            else -> exception?.localizedMessage ?: "Authentication failed"
        }
    }

    private fun rollbackFailedSignUp(
        currentUser: FirebaseUser,
        previousState: SignUpUiState,
        displayName: String,
        email: String,
    ) {
        currentUser.delete().addOnCompleteListener { deleteTask ->
            auth.signOut()
            _signUpState.value = previousState.copy(
                displayName = displayName,
                email = email,
                isLoading = false,
                errorMessage = if (deleteTask.isSuccessful) {
                    "We couldn't create your profile. Please try again."
                } else {
                    "Account created, but profile setup failed. Try logging in again."
                }
            )
        }
    }
}
