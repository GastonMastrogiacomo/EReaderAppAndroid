package com.ereaderapp.android.ui.auth

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthService @Inject constructor(
    private val googleSignInClient: GoogleSignInClient
) {

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): Result<String> {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                Result.success(idToken)
            } else {
                Result.failure(Exception("Failed to get ID token"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        }
    }

    suspend fun signIn(): Result<String> {
        return try {
            // For newer implementation, you might want to use Credential Manager
            // For now, this will indicate to use the intent-based approach
            Result.failure(Exception("Use getSignInIntent() instead"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            // Handle silently
        }
    }
}