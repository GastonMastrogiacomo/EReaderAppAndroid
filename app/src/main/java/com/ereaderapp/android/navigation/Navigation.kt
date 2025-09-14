package com.ereaderapp.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.ereaderapp.android.data.models.Book
import com.ereaderapp.android.ui.auth.AuthScreen
import com.ereaderapp.android.ui.auth.AuthViewModel
import com.ereaderapp.android.ui.books.BookDetailsScreen
import com.ereaderapp.android.ui.books.BooksScreen
import com.ereaderapp.android.ui.home.HomeScreen
import com.ereaderapp.android.ui.libraries.LibrariesScreen
import com.ereaderapp.android.ui.libraries.LibraryDetailsScreen
import com.ereaderapp.android.ui.profile.ProfileScreen
import com.ereaderapp.android.ui.reader.PdfReaderScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Books : Screen("books")
    object Libraries : Screen("libraries")
    object Profile : Screen("profile")
    object BookDetails : Screen("book_details")
    object LibraryDetails : Screen("library_details")
    object PdfReader : Screen("pdf_reader")
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Books, "Books", Icons.Default.MenuBook),
    BottomNavItem(Screen.Libraries, "Libraries", Icons.Default.LibraryBooks),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EReaderNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // Track the selected book and library for navigation
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var selectedLibrary by remember { mutableStateOf<com.ereaderapp.android.data.models.Library?>(null) }

    when (authState) {
        is com.ereaderapp.android.ui.auth.AuthState.Authenticated -> {
            Scaffold(
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    // Only show bottom bar on main screens
                    if (currentDestination?.route in bottomNavItems.map { it.screen.route }) {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                                    onClick = {
                                        navController.navigate(item.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onBookClick = { book ->
                                selectedBook = book
                                navController.navigate(Screen.BookDetails.route)
                            }
                        )
                    }

                    composable(Screen.Books.route) {
                        BooksScreen(
                            onBookClick = { book ->
                                selectedBook = book
                                navController.navigate(Screen.BookDetails.route)
                            }
                        )
                    }

                    composable(Screen.Libraries.route) {
                        LibrariesScreen(
                            onLibraryClick = { library ->
                                selectedLibrary = library
                                navController.navigate(Screen.LibraryDetails.route)
                            }
                        )
                    }

                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onEditProfile = {
                                // TODO: Navigate to edit profile screen
                            },
                            onLogout = {
                                navController.navigate(Screen.Auth.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.BookDetails.route) {
                        selectedBook?.let { book ->
                            BookDetailsScreen(
                                book = book,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onReadBook = { bookToRead ->
                                    selectedBook = bookToRead
                                    navController.navigate(Screen.PdfReader.route)
                                }
                            )
                        }
                    }

                    composable(Screen.LibraryDetails.route) {
                        selectedLibrary?.let { library ->
                            LibraryDetailsScreen(
                                library = library,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onBookClick = { book ->
                                    selectedBook = book
                                    navController.navigate(Screen.BookDetails.route)
                                }
                            )
                        }
                    }

                    composable(Screen.PdfReader.route) {
                        selectedBook?.let { book ->
                            PdfReaderScreen(
                                book = book,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
        else -> {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}