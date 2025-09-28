package com.ereaderapp.android.data.api

import com.ereaderapp.android.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // CORREGIDO: Endpoints de Supabase Auth reales
    @POST("auth/v1/token?grant_type=id_token")
    @Headers("Content-Type: application/json")
    suspend fun loginWithGoogle(
        @Body request: SupabaseGoogleLoginRequest
    ): Response<SupabaseAuthResponse>

    // Endpoints para datos personalizados (si existen en tu backend)
    @GET("rest/v1/books")
    suspend fun getBooks(
        @Query("select") select: String = "*",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<List<Book>>

    @GET("rest/v1/books")
    suspend fun getBook(
        @Query("id") id: String,
        @Query("select") select: String = "*"
    ): Response<List<Book>>

    @GET("rest/v1/categories")
    suspend fun getCategories(
        @Query("select") select: String = "*"
    ): Response<List<Category>>

    // User profile from Supabase Auth
    @GET("auth/v1/user")
    suspend fun getCurrentUser(): Response<SupabaseUser>
}

// CORREGIDO: Modelos para Supabase Auth
data class SupabaseGoogleLoginRequest(
    val id_token: String,
    val provider: String = "google"
)

data class SupabaseAuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: SupabaseUser
)

data class SupabaseUser(
    val id: String,
    val email: String,
    val user_metadata: SupabaseUserMetadata,
    val created_at: String,
    val updated_at: String
)

data class SupabaseUserMetadata(
    val full_name: String?,
    val name: String?,
    val picture: String?,
    val avatar_url: String?
)

// Mantener modelos existentes para compatibilidad
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