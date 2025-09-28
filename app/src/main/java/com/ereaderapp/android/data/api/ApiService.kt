package com.ereaderapp.android.data.api

import com.ereaderapp.android.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication endpoints - Updated for Supabase REST API
    // Note: These endpoints depend on your Supabase Edge Functions or RPC setup
    @POST("rest/v1/rpc/login")  // or "auth/v1/token" for Supabase Auth
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("rest/v1/rpc/register")  // or "auth/v1/signup" for Supabase Auth
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>

    @POST("rest/v1/rpc/validate_token")
    suspend fun validateToken(): Response<ApiResponse<User>>

    // Google OAuth - Supabase has built-in OAuth support
    @POST("rest/v1/rpc/google_login")
    suspend fun loginWithGoogle(@Body googleLoginRequest: GoogleLoginRequest): Response<LoginResponse>

    // Books endpoints - Updated for Supabase REST API
    @GET("rest/v1/books")
    suspend fun getBooks(
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("order") sortBy: String? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<Book>>  // Supabase returns arrays directly

    @GET("rest/v1/books")
    suspend fun getBook(
        @Query("id") id: Int,
        @Query("select") select: String = "*"
    ): Response<List<BookDetails>>  // Supabase returns arrays, take first item

    @GET("rest/v1/books")
    suspend fun getPopularBooks(
        @Query("order") order: String = "score.desc",
        @Query("limit") limit: Int = 10
    ): Response<List<Book>>

    @GET("rest/v1/books")
    suspend fun getRecentBooks(
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 10
    ): Response<List<Book>>

    @GET("rest/v1/categories")
    suspend fun getCategories(): Response<List<Category>>

    // User Profile endpoints
    @GET("rest/v1/user_profiles")
    suspend fun getUserProfile(
        @Query("user_id") userId: Int? = null
    ): Response<List<UserProfile>>

    @GET("rest/v1/reading_activities")
    suspend fun getReadingActivity(
        @Query("user_id") userId: Int? = null
    ): Response<List<ReadingActivity>>

    // Libraries endpoints
    @GET("rest/v1/libraries")
    suspend fun getLibraries(
        @Query("user_id") userId: Int? = null
    ): Response<List<Library>>

    @GET("rest/v1/libraries")
    suspend fun getLibrary(
        @Query("id") id: Int,
        @Query("select") select: String = "*,library_books(book:books(*))"
    ): Response<List<LibraryDetails>>

    @POST("rest/v1/libraries")
    suspend fun createLibrary(@Body request: CreateLibraryRequest): Response<Library>

    @POST("rest/v1/library_books")
    suspend fun addBookToLibrary(@Body request: AddBookToLibraryRequest): Response<Any>

    @DELETE("rest/v1/library_books")
    suspend fun removeBookFromLibrary(
        @Query("library_id") libraryId: Int,
        @Query("book_id") bookId: Int
    ): Response<Any>

    // Reading state endpoints
    @GET("rest/v1/reading_states")
    suspend fun getReadingState(
        @Query("book_id") bookId: Int,
        @Query("user_id") userId: Int? = null
    ): Response<List<ReadingState>>

    @POST("rest/v1/reading_states")
    suspend fun saveReadingState(@Body request: SaveReadingStateRequest): Response<Any>

    // Reviews endpoints
    @POST("rest/v1/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Review>

    @DELETE("rest/v1/reviews")
    suspend fun deleteReview(
        @Query("id") id: Int
    ): Response<Any>
}

// Request/Response models - Updated for Supabase
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String? = null,  // Supabase Auth uses metadata for extra fields
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val idToken: String
)

data class CreateLibraryRequest(
    val name: String,
    val user_id: Int? = null  // Will be set by server from auth context
)

data class AddBookToLibraryRequest(
    val library_id: Int,
    val book_id: Int
)

data class SaveReadingStateRequest(
    val book_id: Int,
    val user_id: Int? = null,  // Will be set by server from auth context
    val current_page: Int,
    val zoom_level: Float,
    val view_mode: String,
    val reading_time_minutes: Int
)

data class CreateReviewRequest(
    val book_id: Int,
    val user_id: Int? = null,  // Will be set by server from auth context
    val comment: String,
    val rating: Int
)

// Extended models remain the same but with snake_case for Supabase
data class BookDetails(
    val id: Int,
    val title: String,
    val author: String,
    val description: String?,
    val image_link: String?,
    val release_date: String?,
    val page_count: Int?,
    val score: Double?,
    val author_bio: String?,
    val pdf_path: String?,
    val average_rating: Double,
    val review_count: Int,
    val categories: List<Category>? = null,
    val reviews: List<Review>? = null
)

data class LibraryDetails(
    val id: Int,
    val name: String,
    val book_count: Int? = null,
    val books: List<Book>? = null
)

data class Review(
    val id: Int,
    val comment: String,
    val rating: Int,
    val created_at: String,
    val user: UserInfo? = null
)

data class UserInfo(
    val id: Int,
    val name: String? = null,
    val profile_picture: String? = null
)

data class ReadingState(
    val book_id: Int,
    val current_page: Int,
    val zoom_level: Float,
    val view_mode: String,
    val last_accessed: String?
)