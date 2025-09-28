package com.ereaderapp.android.data.repository

import android.content.Intent
import com.ereaderapp.android.data.api.*
import com.ereaderapp.android.data.local.TokenManager
import com.ereaderapp.android.data.models.*
import com.ereaderapp.android.ui.auth.GoogleAuthService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val googleAuthService: GoogleAuthService
) {

    // CORREGIDO: Google Login usando Supabase Auth
    suspend fun googleLogin(idToken: String): Result<LoginResponse> {
        return try {
            val request = SupabaseGoogleLoginRequest(id_token = idToken)
            val response = apiService.loginWithGoogle(request)

            if (response.isSuccessful && response.body() != null) {
                val supabaseAuth = response.body()!!

                val user = User(
                    id = supabaseAuth.user.id.toIntOrNull() ?: 0,
                    name = supabaseAuth.user.user_metadata.name ?: supabaseAuth.user.user_metadata.full_name ?: "",
                    email = supabaseAuth.user.email,
                    profilePicture = supabaseAuth.user.user_metadata.picture ?: supabaseAuth.user.user_metadata.avatar_url,
                    role = "User",
                    createdAt = supabaseAuth.user.created_at
                )

                val loginResponse = LoginResponse(
                    success = true,
                    token = supabaseAuth.access_token,
                    user = user,
                    expiresIn = supabaseAuth.expires_in.toLong(),
                    message = "Login successful"
                )

                tokenManager.saveAuthData(supabaseAuth.access_token, user)
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Google login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Authentication methods
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
            // Handle silently
        }
        tokenManager.clearAuthData()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun getUserFlow(): Flow<User?> {
        return tokenManager.getUserFlow()
    }

    // Placeholder methods for email/password (implement if needed)
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        // TODO: Implement with Supabase email/password auth if needed
        return Result.failure(Exception("Email/password login not implemented"))
    }

    suspend fun register(name: String, email: String, password: String): Result<LoginResponse> {
        // TODO: Implement with Supabase email/password auth if needed
        return Result.failure(Exception("Email/password registration not implemented"))
    }

    // Books (simplified for now)
    suspend fun getBooks(
        search: String? = null,
        categoryId: Int? = null,
        sortBy: String? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): Result<BooksResponse> {
        return try {
            val response = apiService.getBooks(limit = pageSize, offset = (page - 1) * pageSize)
            if (response.isSuccessful && response.body() != null) {
                val books = response.body()!!
                val booksResponse = BooksResponse(
                    success = true,
                    data = books,
                    pagination = null,
                    message = "Success"
                )
                Result.success(booksResponse)
            } else {
                Result.failure(Exception("Failed to fetch books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularBooks(limit: Int = 10): Result<List<Book>> {
        return try {
            val response = apiService.getBooks(limit = limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch popular books"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentBooks(limit: Int = 10): Result<List<Book>> {
        return try {
            val response = apiService.getBooks(limit = limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch recent books"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Placeholder methods for other features
    suspend fun getBook(id: Int): Result<Book> {
        return Result.failure(Exception("Get book details not implemented"))
    }

    suspend fun getUserProfile(): Result<UserProfile> {
        return Result.failure(Exception("User profile not implemented"))
    }

    suspend fun getReadingActivity(): Result<List<ReadingActivity>> {
        return Result.failure(Exception("Reading activity not implemented"))
    }

    suspend fun getLibraries(): Result<List<Library>> {
        return Result.failure(Exception("Libraries not implemented"))
    }

    suspend fun getLibrary(id: Int): Result<Library> {
        return Result.failure(Exception("Library details not implemented"))
    }

    suspend fun createLibrary(name: String): Result<Library> {
        return Result.failure(Exception("Create library not implemented"))
    }

    suspend fun addBookToLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return Result.failure(Exception("Add book to library not implemented"))
    }

    suspend fun removeBookFromLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return Result.failure(Exception("Remove book from library not implemented"))
    }
}