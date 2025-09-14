package com.ereaderapp.android.ui.reader

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.ereaderapp.android.data.models.Book
import java.io.File
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    book: Book,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(0) }
    var totalPages by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (totalPages > 0) {
                        Text(
                            text = "Page ${currentPage + 1} of $totalPages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // PDF Viewer
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading PDF",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (!book.pdfPath.isNullOrEmpty()) {
                AndroidView(
                    factory = { context ->
                        PDFView(context, null).apply {
                            try {
                                if (book.pdfPath.startsWith("http")) {
                                    // Load from URL
                                    fromUri(android.net.Uri.parse(book.pdfPath))
                                } else {
                                    // Load from local file
                                    val file = File(book.pdfPath)
                                    if (file.exists()) {
                                        fromFile(file)
                                    } else {
                                        // Try as asset
                                        fromAsset(book.pdfPath)
                                    }
                                }

                                defaultPage(0)
                                    .onPageChange { page, pageCount ->
                                        currentPage = page
                                        totalPages = pageCount
                                    }
                                    .onLoad {
                                        isLoading = false
                                        error = null
                                    }
                                    .onError { throwable ->
                                        isLoading = false
                                        error = throwable.message ?: "Unknown error occurred"
                                    }
                                    .enableSwipe(true)
                                    .swipeHorizontal(false)
                                    .enableDoubletap(true)
                                    .pageFitPolicy(FitPolicy.WIDTH)
                                    .nightMode(false)
                                    .load()
                            } catch (e: Exception) {
                                isLoading = false
                                error = e.message ?: "Failed to load PDF"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}