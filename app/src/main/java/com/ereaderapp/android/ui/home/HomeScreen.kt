package com.ereaderapp.android.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.ui.books.BooksViewModel
import com.ereaderapp.android.ui.components.*
import com.ereaderapp.android.ui.theme.*

@Composable
fun HomeScreen(
    onBookClick: (Book) -> Unit,
    viewModel: BooksViewModel = hiltViewModel()
) {
    val popularBooks by viewModel.popularBooks.collectAsState()
    val recentBooks by viewModel.recentBooks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Animación de entrada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header con animación
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(animationSpec = tween(600))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineLarge.copy(
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
        }

        // Popular Books Section
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) +
                        slideInVertically(animationSpec = tween(600, delayMillis = 200))
            ) {
                BookSection(
                    title = "Popular Books",
                    subtitle = "Most loved by readers",
                    books = popularBooks,
                    onBookClick = onBookClick,
                    isLoading = isLoading && popularBooks.isEmpty()
                )
            }
        }

        // Recent Books Section
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) +
                        slideInVertically(animationSpec = tween(600, delayMillis = 400))
            ) {
                BookSection(
                    title = "Recently Added",
                    subtitle = "Fresh arrivals in our collection",
                    books = recentBooks,
                    onBookClick = onBookClick,
                    isLoading = isLoading && recentBooks.isEmpty()
                )
            }
        }
    }
}

@Composable
private fun BookSection(
    title: String,
    subtitle: String,
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    isLoading: Boolean
) {
    Column {
        // Section Header
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                // Loading placeholder con shimmer
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(4) {
                        BookCardPlaceholder()
                    }
                }
            }
            books.isEmpty() -> {
                // Empty state compacto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No books available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
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

@Composable
private fun BookCardPlaceholder() {
    Column(
        modifier = Modifier.width(160.dp)
    ) {
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.height(4.dp))
        ShimmerEffect(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
        )
    }
}