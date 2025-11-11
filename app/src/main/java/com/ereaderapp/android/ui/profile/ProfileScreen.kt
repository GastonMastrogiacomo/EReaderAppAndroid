package com.ereaderapp.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ereaderapp.android.data.models.ReadingActivity
import com.ereaderapp.android.data.models.UserStatistics
import com.ereaderapp.android.ui.auth.AuthViewModel
import com.ereaderapp.android.ui.components.LoadingIndicator
import com.ereaderapp.android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val user by authViewModel.user.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val readingActivity by profileViewModel.readingActivity.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        profileViewModel.loadReadingActivity()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryBlue,
                                        PrimaryBlueLight
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))

                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // White border
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 8.dp
                            ) {}

                            // Profile image
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            ) {
                                if (!user?.profilePicture.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(user?.profilePicture)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape,
                                        color = PrimaryBlueContainer
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            tint = PrimaryBlue
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Name
                        Text(
                            text = user?.name ?: "Unknown User",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // User Email with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Blue.copy(alpha = 0.9f)
                            )
                            Text(
                                text = user?.email ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black.copy(alpha = 0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Logout Button - Improved design
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red,
                                containerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Black, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Logout",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            // Reading Statistics
            if (isLoading && userProfile == null) {
                LoadingIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    message = "Loading statistics..."
                )
            } else {
                userProfile?.statistics?.let { stats ->
                    ReadingStatistics(statistics = stats)
                }
            }
        }

        item {
            // Recent Reading Activity
            if (readingActivity.isNotEmpty()) {
                RecentReadingActivity(
                    activities = readingActivity.take(5)
                )
            }
        }
    }

    // Logout Confirmation Dialog - Improved
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout? You'll need to sign in again to access your account.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
private fun ReadingStatistics(
    statistics: UserStatistics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Reading Statistics",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.MenuBook,
                    label = "Books Read",
                    value = statistics.totalBooksRead.toString(),
                    color = AccentGreen
                )
                StatItem(
                    icon = Icons.Default.Article,
                    label = "Pages Read",
                    value = statistics.totalPagesRead.toString(),
                    color = AccentOrange
                )
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "Hours Read",
                    value = "${"%.1f".format(statistics.totalReadingHours)}",
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Star,
                    label = "Reviews",
                    value = statistics.totalReviews.toString(),
                    color = StarGold
                )
                StatItem(
                    icon = Icons.Default.LibraryBooks,
                    label = "Libraries",
                    value = statistics.totalLibraries.toString(),
                    color = AccentRed
                )
                // Empty space for alignment
                Box(modifier = Modifier.width(80.dp))
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun RecentReadingActivity(
    activities: List<ReadingActivity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Recent Reading",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            activities.forEach { activity ->
                ReadingActivityItem(activity = activity)
                if (activity != activities.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ReadingActivityItem(
    activity: ReadingActivity
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Progress circle
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { (activity.readingProgress / 100).toFloat() },
                modifier = Modifier.size(48.dp),
                color = PrimaryBlue,
                strokeWidth = 4.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${activity.readingProgress.toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryBlue
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.book.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )
            Text(
                text = activity.book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }


    }
}