package com.ereaderapp.android.ui.libraries

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.ereaderapp.android.ui.components.SuccessSnackbar
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip

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
    var bookToRemove by remember { mutableStateOf<Book?>(null) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }

    // KEY FIX: Always refresh library details when screen is opened
    LaunchedEffect(library.id) {
        viewModel.getLibraryDetails(library.id)
    }

    // Handle success messages
    LaunchedEffect(actionSuccess) {
        actionSuccess?.let {
            showSuccessMessage = it
            kotlinx.coroutines.delay(100)
            viewModel.clearActionSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                                // GRID VIEW with delete button
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(books, key = { it.id }) { book ->
                                        BookCardWithDelete(
                                            book = book,
                                            onBookClick = { onBookClick(book) },
                                            onDeleteClick = { bookToRemove = book }
                                        )
                                    }
                                }
                            } else {
                                // LIST VIEW with delete button
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(books, key = { it.id }) { book ->
                                        BookListItemWithDelete(
                                            book = book,
                                            onBookClick = { onBookClick(book) },
                                            onDeleteClick = { bookToRemove = book }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Success message overlay
        showSuccessMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                SuccessSnackbar(
                    message = message,
                    onDismiss = { showSuccessMessage = null }
                )
            }
        }
    }

    // Remove book confirmation dialog
    bookToRemove?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToRemove = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Remove Book",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove \"${book.title}\" from this library?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeBookFromLibrary(library.id, book.id)
                        bookToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToRemove = null }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
private fun BookCardWithDelete(
    book: Book,
    onBookClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box {
        BookCard(
            book = book,
            onClick = onBookClick
        )

        // Delete button overlay in top-left corner
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            shadowElevation = 4.dp
        ) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from library",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun BookListItemWithDelete(
    book: Book,
    onBookClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onBookClick)
            ) {
                BookListItem(
                    book = book,
                    onClick = onBookClick
                )
            }

            // Delete button on the right
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from library",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}