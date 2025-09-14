package com.ereaderapp.android.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.ui.books.BooksViewModel
import com.ereaderapp.android.ui.components.BookCard
import com.ereaderapp.android.ui.components.LoadingIndicator

@Composable
fun HomeScreen(
    onBookClick: (Book) -> Unit,
    viewModel: BooksViewModel = hiltViewModel()
) {
    val popularBooks by viewModel.popularBooks.collectAsState()
    val recentBooks by viewModel.recentBooks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Welcome Section
            Column {
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discover your next great read",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Popular Books Section
        item {
            BookSection(
                title = "Popular Books",
                books = popularBooks,
                onBookClick = onBookClick,
                isLoading = isLoading && popularBooks.isEmpty()
            )
        }

        // Recent Books Section
        item {
            BookSection(
                title = "Recently Added",
                books = recentBooks,
                onBookClick = onBookClick,
                isLoading = isLoading && recentBooks.isEmpty()
            )
        }
    }
}

@Composable
private fun BookSection(
    title: String,
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    isLoading: Boolean
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    message = "Loading books..."
                )
            }
            books.isEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "No books available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(books) { book ->
                        BookCard(
                            book = book,
                            modifier = Modifier.width(160.dp),
                            onClick = { onBookClick(book) }
                        )
                    }
                }
            }
        }
    }
}