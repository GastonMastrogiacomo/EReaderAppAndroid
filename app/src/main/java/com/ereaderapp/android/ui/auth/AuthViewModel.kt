package com.ereaderapp.android.ui.auth

import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.ereaderapp.android.data.models.User
import com.ereaderapp.android.data.repository.Repository
import com.ereaderapp.android.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: Repository
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
            Log.d("AuthViewModel", "Attempting login for email: $email")
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { loginResponse ->
                    Log.d("AuthViewModel", "Login response: ${loginResponse.success}")
                    if (loginResponse.success) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        val errorMsg = loginResponse.message ?: "Login failed"
                        Log.e("AuthViewModel", "Login failed: $errorMsg")
                        setError(errorMsg)
                        _authState.value = AuthState.Unauthenticated
                    }
                },
                onFailure = { exception ->
                    val errorMsg = exception.message ?: "Login failed"
                    Log.e("AuthViewModel", "Login exception: $errorMsg", exception)
                    setError(errorMsg)
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }

    fun register(name: String, email: String, password: String) {
        executeWithLoading {
            Log.d("AuthViewModel", "Attempting registration for email: $email")
            val result = repository.register(name, email, password)
            result.fold(
                onSuccess = { registerResponse ->
                    Log.d("AuthViewModel", "Registration response: ${registerResponse.success}")
                    if (registerResponse.success) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        val errorMsg = registerResponse.message ?: "Registration failed"
                        Log.e("AuthViewModel", "Registration failed: $errorMsg")
                        setError(errorMsg)
                        _authState.value = AuthState.Unauthenticated
                    }
                },
                onFailure = { exception ->
                    val errorMsg = exception.message ?: "Registration failed"
                    Log.e("AuthViewModel", "Registration exception: $errorMsg", exception)
                    setError(errorMsg)
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }

    fun signInWithGoogle() {
        executeWithLoading {
            Log.d("AuthViewModel", "Starting Google sign-in")
            val signInResult = repository.signInWithGoogle()
            signInResult.fold(
                onSuccess = { idToken ->
                    Log.d("AuthViewModel", "Google sign-in successful, sending token to backend")
                    // Send the ID token to your backend
                    val result = repository.googleLogin(idToken)
                    result.fold(
                        onSuccess = { loginResponse ->
                            Log.d("AuthViewModel", "Backend Google login response: ${loginResponse.success}")
                            if (loginResponse.success) {
                                _authState.value = AuthState.Authenticated
                            } else {
                                val errorMsg = loginResponse.message ?: "Google sign-in failed"
                                Log.e("AuthViewModel", "Backend Google login failed: $errorMsg")
                                setError(errorMsg)
                                _authState.value = AuthState.Unauthenticated
                            }
                        },
                        onFailure = { exception ->
                            val errorMsg = exception.message ?: "Google sign-in failed"
                            Log.e("AuthViewModel", "Backend Google login exception: $errorMsg", exception)
                            setError(errorMsg)
                            _authState.value = AuthState.Unauthenticated
                        }
                    )
                },
                onFailure = { exception ->
                    val errorMsg = when {
                        exception.message?.contains("10") == true ->
                            "Google Sign-In configuration error. Please check SHA-1 fingerprint and client ID."
                        exception.message?.contains("12") == true ->
                            "Google Sign-In quota exceeded or API not enabled."
                        exception.message?.contains("7") == true ->
                            "Network error. Please check your internet connection."
                        else -> exception.message ?: "Google sign-in failed"
                    }
                    Log.e("AuthViewModel", "Google sign-in failed: $errorMsg", exception)
                    setError(errorMsg)
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return repository.getGoogleSignInIntent()
    }

    fun handleGoogleSignInResult(data: Intent?) {
        executeWithLoading {
            Log.d("AuthViewModel", "Handling Google sign-in result")
            val result = repository.handleGoogleSignInResult(data)
            result.fold(
                onSuccess = { loginResponse ->
                    Log.d("AuthViewModel", "Google sign-in result successful: ${loginResponse.success}")
                    if (loginResponse.success) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        val errorMsg = loginResponse.message ?: "Google sign-in failed"
                        Log.e("AuthViewModel", "Google sign-in result failed: $errorMsg")
                        setError(errorMsg)
                        _authState.value = AuthState.Unauthenticated
                    }
                },
                onFailure = { exception ->
                    val errorMsg = when {
                        exception.message?.contains("10") == true ->
                            "Google Sign-In configuration error. Please check SHA-1 fingerprint and client ID."
                        exception.message?.contains("12") == true ->
                            "Google Sign-In quota exceeded or API not enabled."
                        exception.message?.contains("7") == true ->
                            "Network error. Please check your internet connection."
                        else -> exception.message ?: "Google sign-in failed"
                    }
                    Log.e("AuthViewModel", "Google sign-in result failed: $errorMsg", exception)
                    setError(errorMsg)
                    _authState.value = AuthState.Unauthenticated
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Logging out")
            repository.logout()
            _authState.value = AuthState.Unauthenticated
            _user.value = null
        }
    }

    // Validation methods
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