package com.ereaderapp.android.data.api

import com.ereaderapp.android.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication endpoints - matching your web app
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>

    @POST("api/auth/validate")
    suspend fun validateToken(): Response<ApiResponse<User>>

    // Google OAuth login (if your backend supports it)
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body googleLoginRequest: GoogleLoginRequest): Response<LoginResponse>

    // Books endpoints - matching your web app's BooksApiController
    @GET("api/booksapi")
    suspend fun getBooks(
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<BooksResponse>

    @GET("api/booksapi/{id}")
    suspend fun getBook(@Path("id") id: Int): Response<ApiResponse<BookDetails>>

    @GET("api/booksapi/popular")
    suspend fun getPopularBooks(@Query("limit") limit: Int = 10): Response<ApiResponse<List<Book>>>

    @GET("api/booksapi/recent")
    suspend fun getRecentBooks(@Query("limit") limit: Int = 10): Response<ApiResponse<List<Book>>>

    @GET("api/booksapi/categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    // User Profile endpoints - matching your web app's UserApiController
    @GET("api/userapi/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfile>>

    @GET("api/userapi/reading-activity")
    suspend fun getReadingActivity(): Response<ApiResponse<List<ReadingActivity>>>

    // Libraries endpoints - matching your web app's LibrariesApiController
    @GET("api/librariesapi")
    suspend fun getLibraries(): Response<ApiResponse<List<Library>>>

    @GET("api/librariesapi/{id}")
    suspend fun getLibrary(@Path("id") id: Int): Response<ApiResponse<LibraryDetails>>

    @POST("api/librariesapi")
    suspend fun createLibrary(@Body request: CreateLibraryRequest): Response<ApiResponse<Library>>

    @POST("api/librariesapi/{libraryId}/books/{bookId}")
    suspend fun addBookToLibrary(
        @Path("libraryId") libraryId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Any>>

    @DELETE("api/librariesapi/{libraryId}/books/{bookId}")
    suspend fun removeBookFromLibrary(
        @Path("libraryId") libraryId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Any>>

    // Reading state endpoints
    @GET("api/readingapi/state/{bookId}")
    suspend fun getReadingState(@Path("bookId") bookId: Int): Response<ApiResponse<ReadingState>>

    @POST("api/readingapi/state")
    suspend fun saveReadingState(@Body request: SaveReadingStateRequest): Response<ApiResponse<Any>>

    // Reviews endpoints
    @POST("api/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<ApiResponse<Review>>

    @DELETE("api/reviews/{id}")
    suspend fun deleteReview(@Path("id") id: Int): Response<ApiResponse<Any>>
}

// Request/Response models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class CreateLibraryRequest(
    val name: String
)

data class SaveReadingStateRequest(
    val bookId: Int,
    val currentPage: Int,
    val zoomLevel: Float,
    val viewMode: String,
    val readingTimeMinutes: Int
)

data class CreateReviewRequest(
    val bookId: Int,
    val comment: String,
    val rating: Int
)

// Extended models
data class BookDetails(
    val id: Int,
    val title: String,
    val author: String,
    val description: String?,
    val imageLink: String?,
    val releaseDate: String?,
    val pageCount: Int?,
    val score: Double?,
    val authorBio: String?,
    val pdfPath: String?,
    val averageRating: Double,
    val reviewCount: Int,
    val categories: List<Category>,
    val reviews: List<Review>
)

data class LibraryDetails(
    val id: Int,
    val name: String,
    val bookCount: Int,
    val books: List<Book>
)

data class Review(
    val id: Int,
    val comment: String,
    val rating: Int,
    val createdAt: String,
    val user: UserInfo
)

data class UserInfo(
    val id: Int,
    val name: String,
    val profilePicture: String?
)

data class ReadingState(
    val bookId: Int,
    val currentPage: Int,
    val zoomLevel: Float,
    val viewMode: String,
    val lastAccessed: String?
)