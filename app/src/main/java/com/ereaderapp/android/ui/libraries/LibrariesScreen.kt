package com.ereaderapp.android.ui.libraries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ereaderapp.android.data.models.Library
import com.ereaderapp.android.ui.components.EmptyState
import com.ereaderapp.android.ui.components.ErrorMessage
import com.ereaderapp.android.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    onLibraryClick: (Library) -> Unit,
    viewModel: LibrariesViewModel = hiltViewModel()
) {
    val libraries by viewModel.libraries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionSuccess by viewModel.actionSuccess.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Handle success messages
    LaunchedEffect(actionSuccess) {
        if (actionSuccess != null) {
            // Show success message
            viewModel.clearActionSuccess()
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Libraries",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Library"
                        )
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
                            onRetry = { viewModel.loadLibraries() }
                        )
                    }
                    isLoading && libraries.isEmpty() -> {
                        LoadingIndicator(
                            modifier = Modifier.fillMaxSize(),
                            message = "Loading libraries..."
                        )
                    }
                    libraries.isEmpty() -> {
                        EmptyState(
                            title = "No libraries yet",
                            subtitle = "Create your first library to organize your books",
                            modifier = Modifier.fillMaxSize(),
                            action = {
                                Button(onClick = { showCreateDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Library")
                                }
                            }
                        )
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(libraries) { library ->
                                LibraryCard(
                                    library = library,
                                    onClick = { onLibraryClick(library) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Create Library Dialog
        if (showCreateDialog) {
            CreateLibraryDialog(
                onDismiss = { showCreateDialog = false },
                onCreateLibrary = { libraryName ->
                    viewModel.createLibrary(libraryName)
                    showCreateDialog = false
                }
            )
        }
    }

    @Composable
    private fun LibraryCard(
        library: Library,
        onClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Library Icon
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Library Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${library.bookCount} ${if (library.bookCount == 1) "book" else "books"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
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
                Column {
                    Text(
                        text = "Give your library a name to help organize your books.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = libraryName,
                        onValueChange = { libraryName = it },
                        label = { Text("Library Name") },
                        placeholder = { Text("e.g., Favorites, To Read, Fiction") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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