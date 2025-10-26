package com.ereaderapp.android.ui.reader

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    book: Book,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }
    var zoomLevel by remember { mutableStateOf(1f) }
    var showControls by remember { mutableStateOf(true) }
    var readerMode by remember { mutableStateOf(ReaderMode.LIGHT) }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            kotlinx.coroutines.delay(3000)
            showControls = false
        }
    }

    // Load PDF
    LaunchedEffect(book.pdfPath) {
        if (!book.pdfPath.isNullOrEmpty()) {
            try {
                isLoading = true
                error = null

                val file = if (book.pdfPath.startsWith("http")) {
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

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            try {
                pdfRenderer?.close()
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    // Apply reader theme
    ReaderTheme(mode = readerMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars) // Agregar padding para system bars
        ) {
            when {
                isLoading -> {
                    LoadingScreen()
                }
                error != null -> {
                    ErrorScreen(
                        message = error!!,
                        onBack = onNavigateBack
                    )
                }
                pdfRenderer != null -> {
                    // Main reader content
                    ReaderContent(
                        pdfRenderer = pdfRenderer!!,
                        currentPage = currentPage,
                        pageCount = pageCount,
                        zoomLevel = zoomLevel,
                        isLandscape = isLandscape,
                        onPageChange = { newPage ->
                            currentPage = newPage.coerceIn(0, pageCount - 1)
                        },
                        onZoomChange = { newZoom ->
                            zoomLevel = newZoom.coerceIn(0.5f, 3f)
                        },
                        onTap = {
                            showControls = !showControls
                        }
                    )

                    // Top controls
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        ReaderTopBar(
                            book = book,
                            currentPage = currentPage,
                            pageCount = pageCount,
                            readerMode = readerMode,
                            isLandscape = isLandscape,
                            onBack = onNavigateBack,
                            onModeChange = { readerMode = it }
                        )
                    }

                    // Bottom controls
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        ReaderBottomBar(
                            currentPage = currentPage,
                            pageCount = pageCount,
                            zoomLevel = zoomLevel,
                            isLandscape = isLandscape,
                            onPageChange = { currentPage = it.coerceIn(0, pageCount - 1) },
                            onZoomIn = { zoomLevel = min(zoomLevel + 0.2f, 3f) },
                            onZoomOut = { zoomLevel = max(zoomLevel - 0.2f, 0.5f) },
                            onZoomReset = { zoomLevel = 1f }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderContent(
    pdfRenderer: PdfRenderer,
    currentPage: Int,
    pageCount: Int,
    zoomLevel: Float,
    isLandscape: Boolean,
    onPageChange: (Int) -> Unit,
    onZoomChange: (Float) -> Unit,
    onTap: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(zoomLevel) }

    LaunchedEffect(zoomLevel) {
        scale = zoomLevel
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    onZoomChange(scale)

                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (scale <= 1f) {
                        if (dragAmount < -50 && currentPage < pageCount - 1) {
                            onPageChange(currentPage + 1)
                        } else if (dragAmount > 50 && currentPage > 0) {
                            onPageChange(currentPage - 1)
                        }
                    }
                }
            }
    ) {
        PdfPageRenderer(
            pdfRenderer = pdfRenderer,
            pageIndex = currentPage,
            zoomLevel = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            isLandscape = isLandscape
        )
    }
}

@Composable
private fun PdfPageRenderer(
    pdfRenderer: PdfRenderer,
    pageIndex: Int,
    zoomLevel: Float,
    offsetX: Float,
    offsetY: Float,
    isLandscape: Boolean
) {
    var bitmap by remember(pageIndex, zoomLevel) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pageIndex, zoomLevel) { mutableStateOf(true) }

    LaunchedEffect(pageIndex, zoomLevel) {
        withContext(Dispatchers.IO) {
            try {
                isLoading = true
                val page = pdfRenderer.openPage(pageIndex)

                val baseWidth = if (isLandscape) 1200 else 800
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
                    isLoading = false
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = PrimaryBlue)
        } else if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "PDF Page ${pageIndex + 1}",
                modifier = Modifier
                    .graphicsLayer(
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopBar(
    book: Book,
    currentPage: Int,
    pageCount: Int,
    readerMode: ReaderMode,
    isLandscape: Boolean,
    onBack: () -> Unit,
    onModeChange: (ReaderMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                if (!isLandscape) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Page ${currentPage + 1} of $pageCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Theme selector
                ReaderMode.values().forEach { mode ->
                    IconButton(
                        onClick = { onModeChange(mode) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (readerMode == mode)
                                    PrimaryBlue.copy(alpha = 0.2f)
                                else
                                    Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                ReaderMode.LIGHT -> Icons.Default.WbSunny
                                ReaderMode.SEPIA -> Icons.Default.Brightness6
                                ReaderMode.NIGHT -> Icons.Default.DarkMode
                            },
                            contentDescription = mode.name,
                            tint = if (readerMode == mode) PrimaryBlue else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    pageCount: Int,
    zoomLevel: Float,
    isLandscape: Boolean,
    onPageChange: (Int) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomReset: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(if (isLandscape) 8.dp else 12.dp)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = (currentPage + 1).toFloat() / pageCount.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onPageChange(0) },
                        enabled = currentPage > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.FirstPage,
                            "First page",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onPageChange(currentPage - 1) },
                        enabled = currentPage > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            "Previous",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "${currentPage + 1} / $pageCount",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .align(Alignment.CenterVertically)
                    )

                    IconButton(
                        onClick = { onPageChange(currentPage + 1) },
                        enabled = currentPage < pageCount - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            "Next",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onPageChange(pageCount - 1) },
                        enabled = currentPage < pageCount - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.LastPage,
                            "Last page",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Zoom controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onZoomOut,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ZoomOut,
                            "Zoom out",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${(zoomLevel * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onZoomReset)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .align(Alignment.CenterVertically)
                    )
                    IconButton(
                        onClick = onZoomIn,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            "Zoom in",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = PrimaryBlue
            )
            Text(
                text = "Loading document...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = AccentRed.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = AccentRed
                )
                Text(
                    text = "Error Loading PDF",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentRed,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed
                    )
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

enum class ReaderMode {
    LIGHT, SEPIA, NIGHT
}

@Composable
private fun ReaderTheme(
    mode: ReaderMode,
    content: @Composable () -> Unit
) {
    val colorScheme = when (mode) {
        ReaderMode.LIGHT -> lightColorScheme(
            background = Color.White,
            surface = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
        ReaderMode.SEPIA -> lightColorScheme(
            background = ReaderSepia,
            surface = ReaderSepia,
            onBackground = ReaderSepiaText,
            onSurface = ReaderSepiaText
        )
        ReaderMode.NIGHT -> darkColorScheme(
            background = ReaderNight,
            surface = ReaderNight,
            onBackground = ReaderNightText,
            onSurface = ReaderNightText
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

private suspend fun downloadPdf(url: String, cacheDir: File): File = withContext(Dispatchers.IO) {
    val fileName = "temp_pdf_${url.hashCode()}.pdf"
    val file = File(cacheDir, fileName)

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
        if (file.exists()) {
            file.delete()
        }
        throw e
    }

    file
}