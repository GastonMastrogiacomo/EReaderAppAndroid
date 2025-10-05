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
import com.ereaderapp.android.data.repository.Repository
import com.ereaderapp.android.navigation.EReaderNavigation
import com.ereaderapp.android.ui.theme.EReaderAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Test backend connectivity on app start
        testBackendIntegration()

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

    private fun testBackendIntegration() {
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "=== Starting Backend Integration Test ===")

                // 1. Test backend connectivity
                Log.d("MainActivity", "1. Testing backend health...")
                val healthResult = repository.testBackendConnection()
                healthResult.fold(
                    onSuccess = { response ->
                        Log.d("MainActivity", "✅ Backend health check: $response")
                    },
                    onFailure = { error ->
                        Log.e("MainActivity", "❌ Backend health check failed: ${error.message}")
                    }
                )

                // 2. Test categories endpoint
                Log.d("MainActivity", "2. Testing categories endpoint...")
                val categoriesResult = repository.getCategories()
                categoriesResult.fold(
                    onSuccess = { categories ->
                        Log.d("MainActivity", "✅ Categories loaded: ${categories.size} categories")
                        categories.take(3).forEach { category ->
                            Log.d("MainActivity", "   - ${category.name}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("MainActivity", "❌ Categories failed: ${error.message}")
                    }
                )

                // 3. Test books endpoint
                Log.d("MainActivity", "3. Testing books endpoint...")
                val booksResult = repository.getBooks(page = 1, pageSize = 5)
                booksResult.fold(
                    onSuccess = { booksResponse ->
                        Log.d("MainActivity", "✅ Books loaded: ${booksResponse.data.size} books")
                        Log.d("MainActivity", "   Success: ${booksResponse.success}")
                        Log.d("MainActivity", "   Pagination: ${booksResponse.pagination?.totalItems} total")
                        booksResponse.data.take(2).forEach { book ->
                            Log.d("MainActivity", "   - ${book.title} by ${book.author}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("MainActivity", "❌ Books failed: ${error.message}")
                    }
                )

                // 4. Test popular books
                Log.d("MainActivity", "4. Testing popular books...")
                val popularResult = repository.getPopularBooks(limit = 3)
                popularResult.fold(
                    onSuccess = { books ->
                        Log.d("MainActivity", "✅ Popular books loaded: ${books.size} books")
                        books.forEach { book ->
                            Log.d("MainActivity", "   - ${book.title} (Rating: ${book.averageRating})")
                        }
                    },
                    onFailure = { error ->
                        Log.e("MainActivity", "❌ Popular books failed: ${error.message}")
                    }
                )

                // 5. Test recent books
                Log.d("MainActivity", "5. Testing recent books...")
                val recentResult = repository.getRecentBooks(limit = 3)
                recentResult.fold(
                    onSuccess = { books ->
                        Log.d("MainActivity", "✅ Recent books loaded: ${books.size} books")
                        books.forEach { book ->
                            Log.d("MainActivity", "   - ${book.title}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("MainActivity", "❌ Recent books failed: ${error.message}")
                    }
                )

                Log.d("MainActivity", "=== Backend Integration Test Complete ===")

            } catch (e: Exception) {
                Log.e("MainActivity", "Backend test failed with exception", e)
            }
        }
    }
}