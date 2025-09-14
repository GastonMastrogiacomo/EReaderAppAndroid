package com.ereaderapp.android.data.repository

import com.ereaderapp.android.data.api.*
import com.ereaderapp.android.data.local.TokenManager
import com.ereaderapp.android.data.models.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    // Authentication
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
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
            val response = apiService.register(RegisterRequest(name, email, password))
            if (response.isSuccessful && response.body() != null) {
                val registerResponse = response.body()!!
                if (registerResponse.success && registerResponse.token != null && registerResponse.user != null) {
                    tokenManager.saveAuthData(registerResponse.token, registerResponse.user)
                }
                Result.success(registerResponse)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
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

    suspend fun getBook(id: Int): Result<Book> {
        return try {
            val response = apiService.getBook(id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch book: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularBooks(limit: Int = 10): Result<List<Book>> {
        return try {
            val response = apiService.getPopularBooks(limit)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch popular books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentBooks(limit: Int = 10): Result<List<Book>> {
        return try {
            val response = apiService.getRecentBooks(limit)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch recent books: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User Profile
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch user profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReadingActivity(): Result<List<ReadingActivity>> {
        return try {
            val response = apiService.getReadingActivity()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch reading activity: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Libraries
    suspend fun getLibraries(): Result<List<Library>> {
        return try {
            val response = apiService.getLibraries()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch libraries: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLibrary(id: Int): Result<Library> {
        return try {
            val response = apiService.getLibrary(id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch library: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLibrary(name: String): Result<Library> {
        return try {
            val response = apiService.createLibrary(CreateLibraryRequest(name))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to create library: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addBookToLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return try {
            val response = apiService.addBookToLibrary(libraryId, bookId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add book to library: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeBookFromLibrary(libraryId: Int, bookId: Int): Result<Unit> {
        return try {
            val response = apiService.removeBookFromLibrary(libraryId, bookId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove book from library: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}