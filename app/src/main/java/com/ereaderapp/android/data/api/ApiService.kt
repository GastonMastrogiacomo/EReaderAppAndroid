package com.ereaderapp.android.data.api

import com.ereaderapp.android.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("api/debug/test")
    suspend fun healthCheck(): Response<String>

    // Authentication endpoints
    @POST("api/auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register")
    @Headers("Content-Type: application/json")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>

    @POST("api/auth/google")
    @Headers("Content-Type: application/json")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/validate")
    @Headers("Content-Type: application/json")
    suspend fun validateToken(): Response<ApiResponse<User>>

    @GET("api/books")
    suspend fun getBooks(
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<BooksResponse>

    @GET("api/books/{id}")
    suspend fun getBook(
        @Path("id") id: Int
    ): Response<ApiResponse<Book>>

    @GET("api/books/popular")
    suspend fun getPopularBooks(
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<Book>>>

    @GET("api/books/recent")
    suspend fun getRecentBooks(
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<Book>>>

    @GET("api/books/categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    // User endpoints
    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfile>>

    @GET("api/user/reading-activity")
    suspend fun getReadingActivity(): Response<ApiResponse<List<ReadingActivity>>>

    // Libraries endpoints
    @GET("api/libraries")
    suspend fun getLibraries(): Response<ApiResponse<List<Library>>>

    @GET("api/libraries/{id}")
    suspend fun getLibrary(
        @Path("id") id: Int
    ): Response<ApiResponse<Library>>

    @POST("api/libraries")
    suspend fun createLibrary(
        @Body request: CreateLibraryRequest
    ): Response<ApiResponse<Library>>

    @POST("api/libraries/{libraryId}/books/{bookId}")
    suspend fun addBookToLibrary(
        @Path("libraryId") libraryId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Unit>>

    @DELETE("api/libraries/{libraryId}/books/{bookId}")
    suspend fun removeBookFromLibrary(
        @Path("libraryId") libraryId: Int,
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<Unit>>

    // Bookmarks endpoints
    @GET("api/reading/bookmarks/{bookId}")
    suspend fun getBookmarks(
        @Path("bookId") bookId: Int
    ): Response<ApiResponse<List<Bookmark>>>

    @POST("api/reading/bookmarks")
    suspend fun createBookmark(
        @Body request: CreateBookmarkRequest
    ): Response<ApiResponse<Bookmark>>

    @DELETE("api/reading/bookmarks/{id}")
    suspend fun deleteBookmark(
        @Path("id") id: Int
    ): Response<ApiResponse<Unit>>
}

// Request models
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