package com.ereaderapp.android.ui.libraries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.data.models.Library
import com.ereaderapp.android.ui.components.BookCard
import com.ereaderapp.android.ui.components.BookListItem
import com.ereaderapp.android.ui.components.EmptyState
import com.ereaderapp.android.ui.components.ErrorMessage
import com.ereaderapp.android.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryDetailsScreen(
    library: Library,
    onNavigateBack: () -> Unit,
    onBookClick: (Book) -> Unit,
    viewModel: LibrariesViewModel = hiltViewModel()
) {
    val selectedLibrary by viewModel.selectedLibrary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionSuccess by viewModel.actionSuccess.collectAsState()

    var isGridView by remember { mutableStateOf(true) }

    LaunchedEffect(library.id) {
        viewModel.getLibraryDetails(library.id)
    }

    // Handle success messages
    LaunchedEffect(actionSuccess) {
        actionSuccess?.let {
            // Show success message
            viewModel.clearActionSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = selectedLibrary?.name ?: library.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${selectedLibrary?.bookCount ?: library.bookCount} ${if ((selectedLibrary?.bookCount ?: library.bookCount) == 1) "book" else "books"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                        contentDescription = if (isGridView) "List view" else "Grid view"
                    )
                }
            }
        )

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
                        onRetry = { viewModel.getLibraryDetails(library.id) }
                    )
                }
                isLoading && selectedLibrary == null -> {
                    LoadingIndicator(
                        modifier = Modifier.fillMaxSize(),
                        message = "Loading library..."
                    )
                }
                else -> {
                    val libraryDetails = selectedLibrary ?: library
                    val books = libraryDetails.books

                    if (books.isEmpty()) {
                        EmptyState(
                            title = "No books in this library",
                            subtitle = "Books you add to this library will appear here",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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
                            }
                        }
                    }
                }
            }
        }
    }
}