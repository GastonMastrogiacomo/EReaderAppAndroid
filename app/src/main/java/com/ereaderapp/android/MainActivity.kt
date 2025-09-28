// Updated MainActivity.kt with fixed backend test

package com.ereaderapp.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ereaderapp.android.navigation.EReaderNavigation
import com.ereaderapp.android.ui.theme.EReaderAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //testBackendAPI()

        setContent {
            EReaderAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EReaderNavigation()
                }
            }
        }
    }

    private fun testBackendAPI() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { // FIX: Run on background thread
                try {
                    val client = OkHttpClient()

                    // Test 1: Check if the backend is accessible
                    Log.d("BackendTest", "Testing backend connectivity...")
                    val pingRequest = Request.Builder()
                        .url("https://librolibredv.onrender.com/")
                        .build()

                    val pingResponse = client.newCall(pingRequest).execute()
                    Log.d("BackendTest", "Backend ping response: ${pingResponse.code}")

                    // Test 2: Check registration endpoint
                    Log.d("BackendTest", "Testing registration endpoint...")
                    val registerBody = JSONObject().apply {
                        put("name", "Test User")
                        put("email", "test@example.com")
                        put("password", "testpassword123")
                    }.toString()

                    val registerRequest = Request.Builder()
                        .url("https://librolibredv.onrender.com/api/auth/register")
                        .post(registerBody.toRequestBody("application/json".toMediaType()))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .build()

                    val registerResponse = client.newCall(registerRequest).execute()
                    Log.d("BackendTest", "Register response code: ${registerResponse.code}")
                    val registerResponseBody = registerResponse.body?.string()
                    Log.d("BackendTest", "Register response body: $registerResponseBody")

                    // Test 3: Check available endpoints
                    val endpoints = listOf(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/google",  // This one is missing!
                        "/api/booksapi",
                        "/auth/login",      // Alternative endpoints
                        "/auth/register",
                        "/auth/google"
                    )

                    endpoints.forEach { endpoint ->
                        try {
                            val testRequest = Request.Builder()
                                .url("https://librolibredv.onrender.com$endpoint")
                                .build()
                            val testResponse = client.newCall(testRequest).execute()
                            Log.d("BackendTest", "Endpoint $endpoint: ${testResponse.code}")
                        } catch (e: Exception) {
                            Log.e("BackendTest", "Error testing $endpoint", e)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("BackendTest", "Backend test failed", e)
                }
            }
        }
    }
}