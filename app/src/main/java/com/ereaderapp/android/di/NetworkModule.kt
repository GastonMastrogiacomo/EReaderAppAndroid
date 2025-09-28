package com.ereaderapp.android.di

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.ereaderapp.android.BuildConfig
import com.ereaderapp.android.data.api.ApiService
import com.ereaderapp.android.data.local.TokenManager
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("app_preferences")

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // SUPABASE CONFIGURATION
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNseGF4eHBjYXlmdWZic2xxZWJuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkzMzAxODgsImV4cCI6MjA2NDkwNjE4OH0.y-0qdnJLa37MawOa8ABDTloNTqUpn66ql-DDzynDj_k"

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context) = context.dataStore

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideSupabaseAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val token = tokenManager.getToken()
            val requestBuilder = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("apikey", SUPABASE_ANON_KEY)

            // Add Authorization header if token exists
            if (token != null) {
                Log.d("NetworkModule", "Adding Supabase auth token to request")
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                Log.d("NetworkModule", "No auth token available")
            }

            val finalRequest = requestBuilder.build()
            val response = chain.proceed(finalRequest)

            Log.d("NetworkModule", "Response code: ${response.code}")
            if (!response.isSuccessful) {
                Log.e("NetworkModule", "Request failed: ${response.code} - ${response.message}")
                val responseBody = response.peekBody(2048).string()
                Log.e("NetworkModule", "Response body: $responseBody")
            }

            response
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d("HTTP", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson() = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: com.google.gson.Gson
    ): Retrofit {
        val baseUrl = BuildConfig.BASE_URL
        Log.d("NetworkModule", "Using base URL: $baseUrl")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}