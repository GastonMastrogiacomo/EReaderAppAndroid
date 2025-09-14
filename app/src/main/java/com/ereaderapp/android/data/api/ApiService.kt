package com.ereaderapp.android.data.api

import com.ereaderapp.android.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>

    @POST("auth/validate")
    suspend fun validateToken(): Response<ApiResponse<User>>

    // Books
    @GET("books")
    suspend fun getBooks(
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<BooksResponse>

    @GET("books/{id}")
    suspend fun getBook(@Path("id") id: Int): Response<ApiResponse<Book>>

    @GET("books/popular")
    suspend fun getPopularBooks(@Query("limit") limit: Int = 10): Response<ApiResponse<List<Book>>>

    @GET("books/recent")
    suspend fun getRecentBooks(@Query("limit") limit: Int = 10): Response<ApiResponse<List<Book>>>

    @GET("books/categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    // User Profile
    @GET("user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfile>>

    @GET("user/reading-activity")
    suspend fun getReadingActivity(): Response<ApiResponse<List<ReadingActivity>>>

    // Libraries
    @GET("libraries")
    suspend fun getLibraries(): Response<ApiResponse<List<Library>>>

    @GET("libraries/{id}")
    suspend fun getLibrary(@Path("id") id: Int): Response<ApiResponse<Library>>

    @POST("libraries")
    suspend fun createLibrary(@Body request: CreateLibraryRequest): Response<ApiResponse<Library>>

    @POST("libraries/{libraryId}/books/{bookId}")
    suspend fun addBookToLibrary(
        @Path("libraryId") libraryId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Any>>

    @DELETE("libraries/{libraryId}/books/{bookId}")
    suspend fun removeBookFromLibrary(
        @Path("libraryId") libraryId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Any>>
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class CreateLibraryRequest(
    val name: String
)

data class Category(
    val id: Int,
    val name: String,
    val bookCount: Int
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val profilePicture: String?,
    val role: String,
    val createdAt: String,
    val statistics: UserStatistics
)

data class UserStatistics(
    val totalBooksRead: Int,
    val totalPagesRead: Int,
    val totalReadingHours: Double,
    val totalReviews: Int,
    val totalLibraries: Int
)

data class ReadingActivity(
    val bookId: Int,
    val book: Book,
    val firstAccess: String,
    val lastAccess: String,
    val accessCount: Int,
    val totalPagesRead: Int,
    val lastPageRead: Int,
    val totalReadingTimeMinutes: Int,
    val readingProgress: Double
)