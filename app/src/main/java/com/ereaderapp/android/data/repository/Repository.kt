package com.ereaderapp.android.data.repository

import android.content.Intent
import android.util.Log
import com.ereaderapp.android.data.api.*
import com.ereaderapp.android.data.local.TokenManager
import com.ereaderapp.android.data.models.*
import com.ereaderapp.android.ui.auth.GoogleAuthService
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val googleAuthService: GoogleAuthService
) {

    // Helper function to handle API responses with better error parsing
    private suspend fun <T> handleApiResponse(
        call: suspend () -> Response<T>,
        errorMessage: String
    ): Result<T> {
        return try {
            val response = call()
            Log.d("Repository", "API Response: ${response.code()} - ${response.message()}")

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                val finalErrorMessage = parseErrorMessage(response.code(), errorBody, errorMessage)
                Log.e("Repository", "API Error: $finalErrorMessage")
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            Log.e("Repository", "Network Error: ${e.message}", e)
            val finalErrorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Connection timeout. Please check your internet connection."
                e.message?.contains("ConnectException", ignoreCase = true) == true ->
                    "Cannot connect to server. Please try again."
                e.message?.contains("UnknownHostException", ignoreCase = true) == true ->
                    "No internet connection. Please check your network."
                else -> e.message ?: errorMessage
            }
            Result.failure(Exception(finalErrorMessage))
        }
    }

    private fun parseErrorMessage(code: Int, errorBody: String?, defaultMessage: String): String {
        val cleanError = cleanErrorBody(errorBody)

        return when (code) {
            400 -> {
                when {
                    cleanError?.contains("email", ignoreCase = true) == true &&
                            cleanError.contains("already exists", ignoreCase = true) ->
                        "This email is already registered. Please login or use a different email."
                    cleanError?.contains("invalid", ignoreCase = true) == true ->
                        "Invalid input. Please check your information."
                    cleanError?.contains("password", ignoreCase = true) == true ->
                        "Password does not meet requirements. Please use at least 8 characters."
                    else -> cleanError ?: "Invalid request. Please check your information."
                }
            }
            401 -> {
                when {
                    cleanError?.contains("credentials", ignoreCase = true) == true ||
                            cleanError?.contains("password", ignoreCase = true) == true ->
                        "Incorrect email or password. Please try again."
                    cleanError?.contains("token", ignoreCase = true) == true ->
                        "Session expired. Please login again."
                    else -> "Authentication failed. Please login again."
                }
            }
            403 -> "Access denied. You don't have permission to perform this action."
            404 -> "Resource not found. Please try again."
            409 -> cleanError ?: "This email is already registered. Please login instead."
            422 -> {
                when {
                    cleanError?.contains("email", ignoreCase = true) == true ->
                        "Invalid email format. Please enter a valid email address."
                    cleanError?.contains("password", ignoreCase = true) == true ->
                        "Password is too weak. Please use at least 8 characters."
                    else -> cleanError ?: "Invalid data provided. Please check your input."
                }
            }
            500, 502, 503 -> "Server error. Please try again later."
            else -> cleanError ?: "$defaultMessage (Error $code)"
        }
    }

    private fun cleanErrorBody(errorBody: String?): String? {
        if (errorBody.isNullOrEmpty()) return null

        return try {
            val jsonRegex = """"(error|message)"\s*:\s*"([^"]*)"""".toRegex()
            val match = jsonRegex.find(errorBody)

            if (match != null) {
                // Found JSON with error/message field
                match.groupValues[2]
            } else {
                // Remove common JSON artifacts
                errorBody
                    .replace(""""success"\s*:\s*false\s*,?\s*""".toRegex(), "")
                    .replace(""""success"\s*:\s*true\s*,?\s*""".toRegex(), "")
                    .replace(""""error"\s*:\s*""".toRegex(), "")
                    .replace(""""message"\s*:\s*""".toRegex(), "")
                    .replace("""[{}\[\]"]""".toRegex(), "")
                    .replace(",", "")
                    .trim()
                    .takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            // If cleaning fails, return original with basic cleanup
            errorBody.replace(""""success":false,""", "")
                .replace(""""success": false,""", "")
                .trim()
        }
    }

    suspend fun testBackendConnection(): Result<String> {
        return handleApiResponse(
            call = { apiService.healthCheck() },
            errorMessage = "Backend health check failed"
        )
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return handleApiResponse(
            call = { apiService.login(LoginRequest(email, password)) },
            errorMessage = "Login failed"
        ).onSuccess { loginResponse ->
            if (loginResponse.success && loginResponse.token != null && loginResponse.user != null) {
                tokenManager.saveAuthData(loginResponse.token, loginResponse.user)
                Log.d("Repository", "Login successful, token saved")
            }
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<LoginResponse> {
        return handleApiResponse(
            call = { apiService.register(RegisterRequest(name, email, password)) },
            errorMessage = "Registration failed"
        ).onSuccess { loginResponse ->
            if (loginResponse.success && loginResponse.token != null && loginResponse.user != null) {
                tokenManager.saveAuthData(loginResponse.token, loginResponse.user)
                Log.d("Repository", "Registration successful, token saved")
            }
        }
    }

    suspend fun googleLogin(idToken: String): Result<LoginResponse> {
        return handleApiResponse(
            call = { apiService.loginWithGoogle(GoogleLoginRequest(idToken)) },
            errorMessage = "Google login failed"
        ).onSuccess { loginResponse ->
            if (loginResponse.success && loginResponse.token != null && loginResponse.user != null) {
                tokenManager.saveAuthData(loginResponse.token, loginResponse.user)
                Log.d("Repository", "Google login successful, token saved")
            }
        }
    }

    suspend fun signInWithGoogle(): Result<String> {
        return googleAuthService.signIn()
    }

    fun getGoogleSignInIntent(): Intent {
        return googleAuthService.getSignInIntent()
    }

    suspend fun handleGoogleSignInResult(data: Intent?): Result<LoginResponse> {
        val tokenResult = googleAuthService.handleSignInResult(data)
        return tokenResult.fold(
            onSuccess = { idToken -> googleLogin(idToken) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun logout() {
        try {
            googleAuthService.signOut()
        } catch (e: Exception) {
            Log.w("Repository", "Google sign out error: ${e.message}")
        }
        tokenManager.clearAuthData()
        Log.d("Repository", "User logged out")
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun getUserFlow(): Flow<User?> {
        return tokenManager.getUserFlow()
    }

    suspend fun getBooks(
        search: String? = null,
        categoryId: Int? = null,
        sortBy: String? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): Result<BooksResponse> {
        return handleApiResponse(
            call = { apiService.getBooks(search, categoryId, sortBy, page, pageSize) },
            errorMessage = "Failed to fetch books"
        )
    }

    suspend fun getPopularBooks(limit: Int = 10): Result<List<Book>> {
        return handleApiResponse(
            call = { apiService.getPopularBooks(limit) },
            errorMessage = "Failed to fetch popular books"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch popular books")
            }
        }
    }

    suspend fun getRecentBooks(limit: Int = 10): Result<List<Book>> {
        return handleApiResponse(
            call = { apiService.getRecentBooks(limit) },
            errorMessage = "Failed to fetch recent books"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch recent books")
            }
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return handleApiResponse(
            call = { apiService.getCategories() },
            errorMessage = "Failed to fetch categories"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch categories")
            }
        }
    }

    suspend fun getBook(id: Int): Result<Book> {
        return handleApiResponse(
            call = { apiService.getBook(id) },
            errorMessage = "Failed to fetch book details"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch book details")
            }
        }
    }

    // User
    suspend fun getUserProfile(): Result<UserProfile> {
        return handleApiResponse(
            call = { apiService.getUserProfile() },
            errorMessage = "Failed to fetch user profile"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch user profile")
            }
        }
    }

    suspend fun getReadingActivity(): Result<List<ReadingActivity>> {
        return handleApiResponse(
            call = { apiService.getReadingActivity() },
            errorMessage = "Failed to fetch reading activity"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch reading activity")
            }
        }
    }

    // Libraries
    suspend fun getLibraries(): Result<List<Library>> {
        return handleApiResponse(
            call = { apiService.getLibraries() },
            errorMessage = "Failed to fetch libraries"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch libraries")
            }
        }
    }

    suspend fun getLibrary(id: Int): Result<Library> {
        return handleApiResponse(
            call = { apiService.getLibrary(id) },
            errorMessage = "Failed to fetch library"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch library")
            }
        }
    }

    suspend fun createLibrary(name: String): Result<Library> {
        return handleApiResponse(
            call = { apiService.createLibrary(CreateLibraryRequest(name)) },
            errorMessage = "Failed to create library"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to create library")
            }
        }
    }

    suspend fun addBookToLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return handleApiResponse(
            call = { apiService.addBookToLibrary(libraryId, bookId) },
            errorMessage = "Failed to add book to library"
        ).mapCatching { response ->
            when {
                response.success -> Unit
                else -> throw Exception(response.message ?: "Failed to add book to library")
            }
        }
    }

    suspend fun removeBookFromLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return handleApiResponse(
            call = { apiService.removeBookFromLibrary(libraryId, bookId) },
            errorMessage = "Failed to remove book from library"
        ).mapCatching { response ->
            when {
                response.success -> Unit
                else -> throw Exception(response.message ?: "Failed to remove book from library")
            }
        }
    }

    // Bookmarks
    suspend fun getBookmarks(bookId: Int): Result<List<Bookmark>> {
        return handleApiResponse(
            call = { apiService.getBookmarks(bookId) },
            errorMessage = "Failed to fetch bookmarks"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to fetch bookmarks")
            }
        }
    }

    suspend fun createBookmark(bookId: Int, pageNumber: Int, title: String): Result<Bookmark> {
        return handleApiResponse(
            call = { apiService.createBookmark(CreateBookmarkRequest(bookId, pageNumber, title)) },
            errorMessage = "Failed to create bookmark"
        ).mapCatching { response ->
            when {
                response.success && response.data != null -> response.data
                else -> throw Exception(response.message ?: "Failed to create bookmark")
            }
        }
    }

    suspend fun deleteBookmark(bookmarkId: Int): Result<Unit> {
        return handleApiResponse(
            call = { apiService.deleteBookmark(bookmarkId) },
            errorMessage = "Failed to delete bookmark"
        ).mapCatching { response ->
            when {
                response.success -> Unit
                else -> throw Exception(response.message ?: "Failed to delete bookmark")
            }
        }
    }

    suspend fun deleteLibrary(libraryId: Int): Result<Unit> {
        return handleApiResponse(
            call = { apiService.deleteLibrary(libraryId) },
            errorMessage = "Failed to delete library"
        ).mapCatching { response ->
            when {
                response.success -> Unit
                else -> throw Exception(response.message ?: "Failed to delete library")
            }
        }
    }




}