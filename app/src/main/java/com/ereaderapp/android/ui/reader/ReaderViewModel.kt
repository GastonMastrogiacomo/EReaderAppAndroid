package com.ereaderapp.android.ui.reader

import androidx.lifecycle.viewModelScope
import com.ereaderapp.android.data.models.Bookmark
import com.ereaderapp.android.data.repository.Repository
import com.ereaderapp.android.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel() {

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadBookmarks(bookId: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val result = repository.getBookmarks(bookId)
                result.fold(
                    onSuccess = { bookmarks ->
                        _bookmarks.value = bookmarks.sortedBy { it.pageNumber }
                    },
                    onFailure = { exception ->
                        // Handle silently - bookmarks are optional
                        _bookmarks.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                _bookmarks.value = emptyList()
            } finally {
                setLoading(false)
            }
        }
    }

    fun createBookmark(bookId: Int, pageNumber: Int, title: String) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val result = repository.createBookmark(bookId, pageNumber, title)
                result.fold(
                    onSuccess = { bookmark ->
                        _bookmarks.value = (_bookmarks.value + bookmark).sortedBy { it.pageNumber }
                        _successMessage.value = "Bookmark added successfully!"
                    },
                    onFailure = { exception ->
                        setError(exception.message ?: "Failed to create bookmark")
                    }
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to create bookmark")
            } finally {
                setLoading(false)
            }
        }
    }

    fun deleteBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val result = repository.deleteBookmark(bookmarkId)
                result.fold(
                    onSuccess = {
                        _bookmarks.value = _bookmarks.value.filter { it.id != bookmarkId }
                        _successMessage.value = "Bookmark removed successfully!"
                    },
                    onFailure = { exception ->
                        setError(exception.message ?: "Failed to delete bookmark")
                    }
                )
            } catch (e: Exception) {
                setError(e.message ?: "Failed to delete bookmark")
            } finally {
                setLoading(false)
            }
        }
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}