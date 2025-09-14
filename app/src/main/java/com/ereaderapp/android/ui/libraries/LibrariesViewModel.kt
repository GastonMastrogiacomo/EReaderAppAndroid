package com.ereaderapp.android.ui.libraries

import androidx.lifecycle.viewModelScope
import com.ereaderapp.android.data.models.Library
import com.ereaderapp.android.data.repository.Repository
import com.ereaderapp.android.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibrariesViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel() {

    private val _libraries = MutableStateFlow<List<Library>>(emptyList())
    val libraries: StateFlow<List<Library>> = _libraries.asStateFlow()

    private val _selectedLibrary = MutableStateFlow<Library?>(null)
    val selectedLibrary: StateFlow<Library?> = _selectedLibrary.asStateFlow()

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess.asStateFlow()

    init {
        loadLibraries()
    }

    fun loadLibraries() {
        executeWithLoading {
            val result = repository.getLibraries()
            result.fold(
                onSuccess = { libraries ->
                    _libraries.value = libraries
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Failed to load libraries")
                }
            )
        }
    }

    fun getLibraryDetails(libraryId: Int) {
        executeWithLoading {
            val result = repository.getLibrary(libraryId)
            result.fold(
                onSuccess = { library ->
                    _selectedLibrary.value = library
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Failed to load library details")
                }
            )
        }
    }

    fun createLibrary(name: String) {
        executeWithLoading {
            val result = repository.createLibrary(name)
            result.fold(
                onSuccess = { library ->
                    _libraries.value = _libraries.value + library
                    _actionSuccess.value = "Library created successfully"
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Failed to create library")
                }
            )
        }
    }

    fun addBookToLibrary(libraryId: Int, bookId: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val result = repository.addBookToLibrary(libraryId, bookId)
                result.fold(
                    onSuccess = {
                        _actionSuccess.value = "Book added to library"
                        // Refresh the specific library if it's currently selected
                        if (_selectedLibrary.value?.id == libraryId) {
                            getLibraryDetails(libraryId)
                        }
                        // Refresh all libraries to update book counts
                        loadLibraries()
                    },
                    onFailure = { exception ->
                        setError(exception.message ?: "Failed to add book to library")
                    }
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to add book to library")
            } finally {
                setLoading(false)
            }
        }
    }

    fun removeBookFromLibrary(libraryId: Int, bookId: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val result = repository.removeBookFromLibrary(libraryId, bookId)
                result.fold(
                    onSuccess = {
                        _actionSuccess.value = "Book removed from library"
                        // Refresh the specific library if it's currently selected
                        if (_selectedLibrary.value?.id == libraryId) {
                            getLibraryDetails(libraryId)
                        }
                        // Refresh all libraries to update book counts
                        loadLibraries()
                    },
                    onFailure = { exception ->
                        setError(exception.message ?: "Failed to remove book from library")
                    }
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to remove book from library")
            } finally {
                setLoading(false)
            }
        }
    }

    fun clearSelectedLibrary() {
        _selectedLibrary.value = null
    }

    fun clearActionSuccess() {
        _actionSuccess.value = null
    }
}