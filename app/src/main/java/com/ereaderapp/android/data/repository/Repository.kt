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

    // Authentication methods
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.success && loginResponse.token != null && loginResponse.user != null) {
                    tokenManager.saveAuthData(loginResponse.token, loginResponse.user)
                }
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<LoginResponse> {
        return try {
            val request = RegisterRequest(name, email, password)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.success && loginResponse.token != null && loginResponse.user != null) {
                    tokenManager.saveAuthData(loginResponse.token, loginResponse.user)
                }
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun googleLogin(idToken: String): Result<LoginResponse> {
        return try {
            val request = GoogleLoginRequest(idToken)
            val response = apiService.loginWithGoogle(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.success && loginResponse.token != null && loginResponse.user != null) {
                    tokenManager.saveAuthData(loginResponse.token, loginResponse.user)
                }
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Google login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    // Books
    suspend fun getBooks(
        search: String? = null,
        categoryId: Int? = null,
        sortBy: String? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): Result<BooksResponse> {
        return try {
            val response = apiService.getBooks(search, categoryId, sortBy, page, pageSize)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularBooks(limit: Int = 10): Result<List<Book>> {
        return try {
            val response = apiService.getPopularBooks(limit)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch popular books"))
                }
            } else {
                Result.failure(Exception("Failed to fetch popular books"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentBooks(limit: Int = 10): Result<List<Book>> {
        return try {
            val response = apiService.getRecentBooks(limit)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch recent books"))
                }
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
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch categories"))
                }
            } else {
                Result.failure(Exception("Failed to fetch categories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBook(id: Int): Result<Book> {
        return try {
            val response = apiService.getBook(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch book"))
                }
            } else {
                Result.failure(Exception("Failed to fetch book"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch profile"))
                }
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReadingActivity(): Result<List<ReadingActivity>> {
        return try {
            val response = apiService.getReadingActivity()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch reading activity"))
                }
            } else {
                Result.failure(Exception("Failed to fetch reading activity"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Libraries
    suspend fun getLibraries(): Result<List<Library>> {
        return try {
            val response = apiService.getLibraries()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch libraries"))
                }
            } else {
                Result.failure(Exception("Failed to fetch libraries"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLibrary(id: Int): Result<Library> {
        return try {
            val response = apiService.getLibrary(id)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch library"))
                }
            } else {
                Result.failure(Exception("Failed to fetch library"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLibrary(name: String): Result<Library> {
        return try {
            val request = CreateLibraryRequest(name)
            val response = apiService.createLibrary(request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to create library"))
                }
            } else {
                Result.failure(Exception("Failed to create library"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBookToLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return try {
            val response = apiService.addBookToLibrary(libraryId, bookId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to add book to library"))
                }
            } else {
                Result.failure(Exception("Failed to add book to library"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeBookFromLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return try {
            val response = apiService.removeBookFromLibrary(libraryId, bookId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to remove book from library"))
                }
            } else {
                Result.failure(Exception("Failed to remove book from library"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}