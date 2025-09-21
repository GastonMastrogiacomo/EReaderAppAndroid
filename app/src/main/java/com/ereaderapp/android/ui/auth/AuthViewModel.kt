package com.ereaderapp.android.ui.auth

import androidx.lifecycle.viewModelScope
import com.ereaderapp.android.data.models.User
import com.ereaderapp.android.data.repository.Repository
import com.ereaderapp.android.services.GoogleSignInService
import com.ereaderapp.android.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: Repository,
    private val googleSignInService: GoogleSignInService
) : BaseViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getUserFlow().collect { user ->
                _user.value = user
                _authState.value = if (user != null) AuthState.Authenticated else AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        executeWithLoading {
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { loginResponse ->
                    if (loginResponse.success) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        setError(loginResponse.message ?: "Login failed")
                        _authState.value = AuthState.Unauthenticated
                    }
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Login failed")
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }

    fun signInWithGoogle() {
        executeWithLoading {
            val signInResult = googleSignInService.signIn()
            signInResult.fold(
                onSuccess = { idToken ->
                    // Send the ID token to your backend
                    val result = repository.loginWithGoogle(idToken)
                    result.fold(
                        onSuccess = { loginResponse ->
                            if (loginResponse.success) {
                                _authState.value = AuthState.Authenticated
                            } else {
                                setError(loginResponse.message ?: "Google sign-in failed")
                                _authState.value = AuthState.Unauthenticated
                            }
                        },
                        onFailure = { exception ->
                            setError(exception.message ?: "Google sign-in failed")
                            _authState.value = AuthState.Unauthenticated
                        }
                    )
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Google sign-in failed")
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }

    fun register(name: String, email: String, password: String) {
        executeWithLoading {
            val result = repository.register(name, email, password)
            result.fold(
                onSuccess = { registerResponse ->
                    if (registerResponse.success) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        setError(registerResponse.message ?: "Registration failed")
                        _authState.value = AuthState.Unauthenticated
                    }
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Registration failed")
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Unauthenticated
            _user.value = null
        }
    }

    // Validation methods remain the same
    fun validateLoginForm(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            password.isBlank() -> "Password is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validateRegisterForm(name: String, email: String, password: String, confirmPassword: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            email.isBlank() -> "Email is required"
            password.isBlank() -> "Password is required"
            confirmPassword.isBlank() -> "Please confirm your password"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            password.length < 8 -> "Password must be at least 8 characters"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}