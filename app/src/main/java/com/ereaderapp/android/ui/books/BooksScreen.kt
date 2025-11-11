package com.ereaderapp.android.ui.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.ui.components.BookCard
import com.ereaderapp.android.ui.components.BookListItem
import com.ereaderapp.android.ui.components.EmptyState
import com.ereaderapp.android.ui.components.ErrorMessage
import com.ereaderapp.android.ui.components.LoadingIndicator
import android.content.res.Configuration

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
    var showFilters by remember { mutableStateOf(true) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(searchQuery) {
        searchText = searchQuery
    }

    // Auto-collapse filters in landscape
    LaunchedEffect(isLandscape) {
        if (isLandscape) {
            showFilters = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Compact Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = if (isLandscape) 8.dp else 16.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Books",
                        style = if (isLandscape)
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        else
                            MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Filter toggle button
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            modifier = Modifier.size(if (isLandscape) 36.dp else 48.dp)
                        ) {
                            Badge(
                                modifier = Modifier.offset(x = 8.dp, y = (-8).dp),
                                containerColor = if (selectedCategoryId != null || searchQuery.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surface
                            ) {
                                Icon(
                                    imageVector = if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showFilters) "Hide filters" else "Show filters"
                                )
                            }
                        }
                        IconButton(
                            onClick = { showSearchBar = !showSearchBar },
                            modifier = Modifier.size(if (isLandscape) 36.dp else 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier.size(if (isLandscape) 36.dp else 48.dp)
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = if (isGridView) "List view" else "Grid view"
                            )
                        }
                    }
                }

                // Search Bar - Compact in landscape
                if (showSearchBar) {
                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Search books...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 48.dp else 56.dp),
                        singleLine = true,
                        textStyle = if (isLandscape)
                            MaterialTheme.typography.bodyMedium
                        else
                            MaterialTheme.typography.bodyLarge
                    )

                    LaunchedEffect(searchText) {
                        delay(500) // Debounce
                        if (searchText != searchQuery) {
                            viewModel.searchBooks(searchText)
                        }
                    }
                }

                // Collapsible Filters Section
                androidx.compose.animation.AnimatedVisibility(
                    visible = showFilters,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    Column {
                        // Category Filters
                        Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.clearFilters() },
                                    label = {
                                        Text(
                                            "All",
                                            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    selected = selectedCategoryId == null && searchQuery.isEmpty(),
                                    modifier = Modifier.height(if (isLandscape) 28.dp else 32.dp)
                                )
                            }

                            items(categories) { category ->
                                FilterChip(
                                    onClick = { viewModel.filterByCategory(category.id) },
                                    label = {
                                        Text(
                                            category.name,
                                            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    selected = selectedCategoryId == category.id,
                                    modifier = Modifier.height(if (isLandscape) 28.dp else 32.dp)
                                )
                            }
                        }

                        // Sort Options
                        Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 8.dp)
                        ) {
                            item {
                                AssistChip(
                                    onClick = { viewModel.sortBooks("title") },
                                    label = {
                                        Text(
                                            "Title",
                                            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (sortBy == "title") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.height(if (isLandscape) 28.dp else 32.dp)
                                )
                            }
                            item {
                                AssistChip(
                                    onClick = { viewModel.sortBooks("author") },
                                    label = {
                                        Text(
                                            "Author",
                                            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (sortBy == "author") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.height(if (isLandscape) 28.dp else 32.dp)
                                )
                            }
                            item {
                                AssistChip(
                                    onClick = { viewModel.sortBooks("rating") },
                                    label = {
                                        Text(
                                            "Rating",
                                            style = if (isLandscape) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (sortBy == "rating") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.height(if (isLandscape) 28.dp else 32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 0.dp))
                    }
                }
            }
        }

        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val currentError = error
            when {
                currentError != null -> {
                    ErrorMessage(
                        message = currentError,
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
                    // Adjust grid columns and padding based on orientation
                    val gridColumns = if (isLandscape) 3 else 2
                    val contentPadding = if (isLandscape) 12.dp else 16.dp
                    val itemSpacing = if (isLandscape) 10.dp else 12.dp

                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            contentPadding = PaddingValues(contentPadding),
                            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                            verticalArrangement = Arrangement.spacedBy(itemSpacing)
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
                            contentPadding = PaddingValues(contentPadding),
                            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 8.dp)
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