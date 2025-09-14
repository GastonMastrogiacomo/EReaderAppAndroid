package com.ereaderapp.android.ui.books

import androidx.lifecycle.viewModelScope
import com.ereaderapp.android.data.api.Category
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.data.repository.Repository
import com.ereaderapp.android.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _popularBooks = MutableStateFlow<List<Book>>(emptyList())
    val popularBooks: StateFlow<List<Book>> = _popularBooks.asStateFlow()

    private val _recentBooks = MutableStateFlow<List<Book>>(emptyList())
    val recentBooks: StateFlow<List<Book>> = _recentBooks.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _sortBy = MutableStateFlow("title")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _hasNextPage = MutableStateFlow(false)
    val hasNextPage: StateFlow<Boolean> = _hasNextPage.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadPopularBooks()
        loadRecentBooks()
        loadCategories()
        loadBooks()
    }

    fun loadBooks(
        search: String? = _searchQuery.value.takeIf { it.isNotEmpty() },
        categoryId: Int? = _selectedCategoryId.value,
        sortBy: String? = _sortBy.value,
        page: Int = 1,
        append: Boolean = false
    ) {
        executeWithLoading {
            val result = repository.getBooks(search, categoryId, sortBy, page, 20)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        if (append && page > 1) {
                            _books.value = _books.value + response.data
                        } else {
                            _books.value = response.data
                        }
                        _currentPage.value = page
                        _hasNextPage.value = response.pagination?.hasNextPage ?: false
                    } else {
                        setError("Failed to load books")
                    }
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Failed to load books")
                }
            )
        }
    }

    fun loadMoreBooks() {
        if (_hasNextPage.value && !isLoading.value) {
            loadBooks(page = _currentPage.value + 1, append = true)
        }
    }

    fun searchBooks(query: String) {
        _searchQuery.value = query
        _currentPage.value = 1
        loadBooks(search = query.takeIf { it.isNotEmpty() })
    }

    fun filterByCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
        _currentPage.value = 1
        loadBooks(categoryId = categoryId)
    }

    fun sortBooks(sortBy: String) {
        _sortBy.value = sortBy
        _currentPage.value = 1
        loadBooks(sortBy = sortBy)
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoryId.value = null
        _sortBy.value = "title"
        _currentPage.value = 1
        loadBooks()
    }

    private fun loadPopularBooks() {
        viewModelScope.launch {
            val result = repository.getPopularBooks(10)
            result.fold(
                onSuccess = { books ->
                    _popularBooks.value = books
                },
                onFailure = { /* Handle silently for home screen */ }
            )
        }
    }

    private fun loadRecentBooks() {
        viewModelScope.launch {
            val result = repository.getRecentBooks(10)
            result.fold(
                onSuccess = { books ->
                    _recentBooks.value = books
                },
                onFailure = { /* Handle silently for home screen */ }
            )
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = repository.getCategories()
            result.fold(
                onSuccess = { categories ->
                    _categories.value = categories
                },
                onFailure = { /* Handle silently */ }
            )
        }
    }

    fun getBookDetails(bookId: Int) {
        executeWithLoading {
            val result = repository.getBook(bookId)
            result.fold(
                onSuccess = { book ->
                    _selectedBook.value = book
                },
                onFailure = { exception ->
                    setError(exception.message ?: "Failed to load book details")
                }
            )
        }
    }

    fun clearSelectedBook() {
        _selectedBook.value = null
    }
}