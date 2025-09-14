package com.ereaderapp.android.ui.reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ereaderapp.android.data.models.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }
    var zoomLevel by remember { mutableStateOf(1f) }

    val listState = rememberLazyListState()

    // Load PDF
    LaunchedEffect(book.pdfPath) {
        if (!book.pdfPath.isNullOrEmpty()) {
            try {
                isLoading = true
                error = null

                val file = if (book.pdfPath.startsWith("http")) {
                    // Download PDF if it's a URL
                    downloadPdf(book.pdfPath, context.cacheDir)
                } else {
                    File(book.pdfPath)
                }

                if (file.exists()) {
                    val parcelFileDescriptor = ParcelFileDescriptor.open(
                        file,
                        ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    val renderer = PdfRenderer(parcelFileDescriptor)
                    pdfRenderer = renderer
                    pageCount = renderer.pageCount
                    isLoading = false
                } else {
                    error = "PDF file not found"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load PDF"
                isLoading = false
            }
        } else {
            error = "No PDF path provided"
            isLoading = false
        }
    }

    // Track current page based on scroll position
    LaunchedEffect(listState.firstVisibleItemIndex) {
        currentPage = listState.firstVisibleItemIndex
    }

    // Cleanup renderer when leaving
    DisposableEffect(Unit) {
        onDispose {
            try {
                pdfRenderer?.close()
            } catch (e: Exception) {
                // Handle cleanup error silently
            }
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
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    if (pageCount > 0) {
                        Text(
                            text = "Page ${currentPage + 1} of $pageCount",
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
            },
            actions = {
                if (pdfRenderer != null) {
                    IconButton(
                        onClick = {
                            zoomLevel = (zoomLevel * 0.8f).coerceAtLeast(0.5f)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom Out"
                        )
                    }
                    IconButton(
                        onClick = {
                            zoomLevel = (zoomLevel * 1.2f).coerceAtMost(3f)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom In"
                        )
                    }
                }
            }
        )

        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading PDF...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                error != null -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error Loading PDF",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }

                pdfRenderer != null -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed((0 until pageCount).toList()) { index, pageIndex ->
                            PdfPageItem(
                                pdfRenderer = pdfRenderer!!,
                                pageIndex = pageIndex,
                                zoomLevel = zoomLevel,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageItem(
    pdfRenderer: PdfRenderer,
    pageIndex: Int,
    zoomLevel: Float,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(pageIndex, zoomLevel) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pageIndex, zoomLevel) { mutableStateOf(true) }
    var error by remember(pageIndex, zoomLevel) { mutableStateOf<String?>(null) }

    LaunchedEffect(pageIndex, zoomLevel) {
        withContext(Dispatchers.IO) {
            try {
                isLoading = true
                error = null

                val page = pdfRenderer.openPage(pageIndex)

                // Calculate dimensions with zoom
                val baseWidth = 800 // Base width for good quality
                val baseHeight = (page.height.toFloat() / page.width.toFloat() * baseWidth).toInt()

                val scaledWidth = (baseWidth * zoomLevel).toInt()
                val scaledHeight = (baseHeight * zoomLevel).toInt()

                val pageBitmap = Bitmap.createBitmap(
                    scaledWidth,
                    scaledHeight,
                    Bitmap.Config.ARGB_8888
                )

                page.render(
                    pageBitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )
                page.close()

                withContext(Dispatchers.Main) {
                    bitmap = pageBitmap
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error = e.message ?: "Failed to render page"
                    isLoading = false
                }
            }
        }
    }

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Page number indicator
            Text(
                text = "Page ${pageIndex + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Page content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    error != null -> {
                        Text(
                            text = "Error loading page",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    bitmap != null -> {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page ${pageIndex + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

private suspend fun downloadPdf(url: String, cacheDir: File): File = withContext(Dispatchers.IO) {
    val fileName = "temp_pdf_${url.hashCode()}.pdf"
    val file = File(cacheDir, fileName)

    // Check if file already exists
    if (file.exists()) {
        return@withContext file
    }

    try {
        URL(url).openStream().use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        // If download fails, try to clean up
        if (file.exists()) {
            file.delete()
        }
        throw e
    }

    file
}