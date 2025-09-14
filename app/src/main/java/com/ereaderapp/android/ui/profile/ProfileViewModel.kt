package com.ereaderapp.android.ui.profile

import androidx.lifecycle.viewModelScope
import com.ereaderapp.android.data.api.ReadingActivity
import com.ereaderapp.android.data.api.UserProfile
import com.ereaderapp.android.data.repository.Repository
import com.ereaderapp.android.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _readingActivity = MutableStateFlow<List<ReadingActivity>>(emptyList())
    val readingActivity: StateFlow<List<ReadingActivity>> = _readingActivity.asStateFlow()

    fun loadProfile() {
        executeWithLoading {
            val result = repository.getUserProfile()
            result.fold(
                onSuccess = { profile ->
                    _userProfile.value = profile
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Failed to load profile")
                }
            )
        }
    }

    fun loadReadingActivity() {
        viewModelScope.launch {
            try {
                val result = repository.getReadingActivity()
                result.fold(
                    onSuccess = { activities ->
                        _readingActivity.value = activities
                    },
                    onFailure = { exception ->
                        // Handle silently for reading activity
                        _readingActivity.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                _readingActivity.value = emptyList()
            }
        }
    }
}