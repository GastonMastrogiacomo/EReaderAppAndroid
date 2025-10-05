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
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val token = tokenManager.getToken()
            val requestBuilder = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")

            // Add Authorization header if token exists
            if (token != null) {
                Log.d("NetworkModule", "Adding auth token to request")
                requestBuilder.addHeader("Authorization", "Bearer $token")
            } else {
                Log.d("NetworkModule", "No auth token available")
            }

            val finalRequest = requestBuilder.build()
            Log.d("NetworkModule", "Request URL: ${finalRequest.url}")
            Log.d("NetworkModule", "Request Method: ${finalRequest.method}")

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
            .connectTimeout(45, TimeUnit.SECONDS) // Increased for Render cold starts
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson() = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") // Handle backend date format
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