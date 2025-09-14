package com.ereaderapp.android.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.data.models.Library
import com.ereaderapp.android.ui.components.BookRating
import com.ereaderapp.android.ui.components.ErrorMessage
import com.ereaderapp.android.ui.components.LoadingIndicator
import com.ereaderapp.android.ui.libraries.LibrariesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    book: Book,
    onNavigateBack: () -> Unit,
    onReadBook: (Book) -> Unit,
    booksViewModel: BooksViewModel = hiltViewModel(),
    librariesViewModel: LibrariesViewModel = hiltViewModel()
) {
    val selectedBook by booksViewModel.selectedBook.collectAsState()
    val isLoading by booksViewModel.isLoading.collectAsState()
    val error by booksViewModel.error.collectAsState()

    val libraries by librariesViewModel.libraries.collectAsState()
    val librariesLoading by librariesViewModel.isLoading.collectAsState()
    val actionSuccess by librariesViewModel.actionSuccess.collectAsState()

    var showAddToLibraryDialog by remember { mutableStateOf(false) }
    var showCreateLibraryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(book.id) {
        booksViewModel.getBookDetails(book.id)
    }

    // Handle success messages
    LaunchedEffect(actionSuccess) {
        if (actionSuccess != null) {
            // Show success message (you can use a SnackBar here)
            librariesViewModel.clearActionSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Book Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        when {
            error != null -> {
                ErrorMessage(
                    message = error,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    onRetry = { booksViewModel.getBookDetails(book.id) }
                )
            }
            isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading book details..."
                )
            }
            else -> {
                val bookDetails = selectedBook ?: book

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        BookHeader(
                            book = bookDetails,
                            onReadBook = onReadBook,
                            onAddToLibrary = { showAddToLibraryDialog = true }
                        )
                    }

                    item {
                        BookInfo(book = bookDetails)
                    }

                    if (!bookDetails.description.isNullOrEmpty()) {
                        item {
                            BookDescription(description = bookDetails.description)
                        }
                    }

                    if (!bookDetails.authorBio.isNullOrEmpty()) {
                        item {
                            AuthorBio(authorBio = bookDetails.authorBio)
                        }
                    }
                }
            }
        }
    }

    // Add to Library Dialog
    if (showAddToLibraryDialog) {
        AddToLibraryDialog(
            libraries = libraries,
            isLoading = librariesLoading,
            onDismiss = { showAddToLibraryDialog = false },
            onAddToLibrary = { libraryId ->
                librariesViewModel.addBookToLibrary(libraryId, book.id)
                showAddToLibraryDialog = false
            },
            onCreateNewLibrary = {
                showAddToLibraryDialog = false
                showCreateLibraryDialog = true
            }
        )
    }

    // Create Library Dialog
    if (showCreateLibraryDialog) {
        CreateLibraryDialog(
            onDismiss = { showCreateLibraryDialog = false },
            onCreateLibrary = { libraryName ->
                librariesViewModel.createLibrary(libraryName)
                showCreateLibraryDialog = false
            }
        )
    }
}

@Composable
private fun BookHeader(
    book: Book,
    onReadBook: (Book) -> Unit,
    onAddToLibrary: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book Cover
            Box(
                modifier = Modifier
                    .size(120.dp, 160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!book.imageLink.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.imageLink)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Book cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Book Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                if (book.averageRating > 0) {
                    BookRating(
                        rating = book.averageRating,
                        reviewCount = book.reviewCount
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onReadBook(book) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Read")
                    }

                    OutlinedButton(
                        onClick = onAddToLibrary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookInfo(book: Book) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Book Information",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            if (!book.releaseDate.isNullOrEmpty()) {
                InfoRow(label = "Release Date", value = book.releaseDate)
            }

            if (book.pageCount != null && book.pageCount > 0) {
                InfoRow(label = "Pages", value = "${book.pageCount}")
            }

            if (book.score != null && book.score > 0) {
                InfoRow(label = "Score", value = "${"%.1f".format(book.score)}/10")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun BookDescription(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AuthorBio(authorBio: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "About the Author",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = authorBio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AddToLibraryDialog(
    libraries: List<Library>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAddToLibrary: (Int) -> Unit,
    onCreateNewLibrary: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Library") },
        text = {
            if (isLoading) {
                LoadingIndicator(message = "Loading libraries...")
            } else if (libraries.isEmpty()) {
                Column {
                    Text("You don't have any libraries yet.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Create your first library to organize your books.")
                }
            } else {
                LazyColumn {
                    items(libraries) { library ->
                        TextButton(
                            onClick = { onAddToLibrary(library.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(library.name)
                                Text(
                                    text = "${library.bookCount} books",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNewLibrary) {
                Text("Create New Library")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CreateLibraryDialog(
    onDismiss: () -> Unit,
    onCreateLibrary: (String) -> Unit
) {
    var libraryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Library") },
        text = {
            OutlinedTextField(
                value = libraryName,
                onValueChange = { libraryName = it },
                label = { Text("Library Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (libraryName.isNotBlank()) {
                        onCreateLibrary(libraryName.trim())
                    }
                },
                enabled = libraryName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}