package com.ereaderapp.android.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    protected fun setError(error: String?) {
        _error.value = error
    }

    // Make clearError public so it can be called from Composables
    fun clearError() {
        _error.value = null
    }

    protected fun <T> executeWithLoading(
        onError: (String) -> Unit = { setError(it) },
        block: suspend () -> T
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                clearError()
                block()
            } catch (e: Exception) {
                onError(e.message ?: "An unknown error occurred")
            } finally {
                setLoading(false)
            }
        }
    }
}