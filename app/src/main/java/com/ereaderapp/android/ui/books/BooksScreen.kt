package com.ereaderapp.android.ui.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.ui.components.BookCard
import com.ereaderapp.android.ui.components.BookListItem
import com.ereaderapp.android.ui.components.EmptyState
import com.ereaderapp.android.ui.components.ErrorMessage
import com.ereaderapp.android.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    onBookClick: (Book) -> Unit,
    viewModel: BooksViewModel = hiltViewModel()
) {
    val books by viewModel.books.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasNextPage by viewModel.hasNextPage.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }

    LaunchedEffect(searchQuery) {
        searchText = searchQuery
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar with Search and View Toggle
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Books",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = { isGridView = !isGridView }) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = if (isGridView) "List view" else "Grid view"
                            )
                        }
                    }
                }

                // Search Bar
                if (showSearchBar) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search books...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    LaunchedEffect(searchText) {
                        kotlinx.coroutines.delay(500) // Debounce
                        if (searchText != searchQuery) {
                            viewModel.searchBooks(searchText)
                        }
                    }
                }

                // Filters
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { viewModel.clearFilters() },
                            label = { Text("All") },
                            selected = selectedCategoryId == null && searchQuery.isEmpty()
                        )
                    }

                    items(categories) { category ->
                        FilterChip(
                            onClick = { viewModel.filterByCategory(category.id) },
                            label = { Text(category.name) },
                            selected = selectedCategoryId == category.id
                        )
                    }
                }

                // Sort Options
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        AssistChip(
                            onClick = { viewModel.sortBooks("title") },
                            label = { Text("Title") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (sortBy == "title") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                    item {
                        AssistChip(
                            onClick = { viewModel.sortBooks("author") },
                            label = { Text("Author") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (sortBy == "author") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                    item {
                        AssistChip(
                            onClick = { viewModel.sortBooks("rating") },
                            label = { Text("Rating") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (sortBy == "rating") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }

        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                error != null -> {
                    ErrorMessage(
                        message = error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onRetry = { viewModel.loadBooks() }
                    )
                }
                isLoading && books.isEmpty() -> {
                    LoadingIndicator(
                        modifier = Modifier.fillMaxSize(),
                        message = "Loading books..."
                    )
                }
                books.isEmpty() -> {
                    EmptyState(
                        title = "No books found",
                        subtitle = if (searchQuery.isNotEmpty()) {
                            "Try adjusting your search or filters"
                        } else {
                            "Check back later for new books"
                        },
                        modifier = Modifier.fillMaxSize(),
                        action = if (searchQuery.isNotEmpty() || selectedCategoryId != null) {
                            {
                                Button(onClick = { viewModel.clearFilters() }) {
                                    Text("Clear Filters")
                                }
                            }
                        } else null
                    )
                }
                else -> {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(books) { book ->
                                BookCard(
                                    book = book,
                                    onClick = { onBookClick(book) }
                                )
                            }

                            if (hasNextPage) {
                                item {
                                    LaunchedEffect(Unit) {
                                        viewModel.loadMoreBooks()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(books) { book ->
                                BookListItem(
                                    book = book,
                                    onClick = { onBookClick(book) }
                                )
                            }

                            if (hasNextPage) {
                                item {
                                    LaunchedEffect(Unit) {
                                        viewModel.loadMoreBooks()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}