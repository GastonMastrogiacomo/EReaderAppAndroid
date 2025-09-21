package com.ereaderapp.android.ui.auth

import android.content.Intent
import android.util.Log
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
        Log.d("GoogleAuthService", "Creating Google Sign-In intent")
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): Result<String> {
        return try {
            Log.d("GoogleAuthService", "Handling Google Sign-In result")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            Log.d("GoogleAuthService", "Google Sign-In successful")
            Log.d("GoogleAuthService", "Account email: ${account?.email}")
            Log.d("GoogleAuthService", "Account display name: ${account?.displayName}")

            val idToken = account?.idToken

            if (idToken != null) {
                Log.d("GoogleAuthService", "ID Token obtained successfully")
                Result.success(idToken)
            } else {
                Log.e("GoogleAuthService", "ID Token is null")
                Result.failure(Exception("Failed to get ID token from Google"))
            }
        } catch (e: ApiException) {
            val errorMessage = when (e.statusCode) {
                10 -> "Developer error: Check SHA-1 fingerprint and client ID configuration"
                12 -> "Sign-in quota exceeded or API not enabled"
                7 -> "Network error: Check internet connection"
                4 -> "Sign-in required: User needs to sign in again"
                8 -> "Internal error: Try again later"
                else -> "Google Sign-In failed with code ${e.statusCode}: ${e.message}"
            }
            Log.e("GoogleAuthService", errorMessage, e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Log.e("GoogleAuthService", "Unexpected error during Google Sign-In", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(): Result<String> {
        return try {
            // For newer implementation, you might want to use Credential Manager
            // For now, this will indicate to use the intent-based approach
            Log.w("GoogleAuthService", "Direct signIn() not implemented, use getSignInIntent() instead")
            Result.failure(Exception("Use getSignInIntent() instead"))
        } catch (e: Exception) {
            Log.e("GoogleAuthService", "signIn() error", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            Log.d("GoogleAuthService", "Signing out from Google")
            googleSignInClient.signOut().await()
            Log.d("GoogleAuthService", "Google sign-out successful")
        } catch (e: Exception) {
            Log.e("GoogleAuthService", "Error during Google sign-out", e)
            // Handle silently - sign-out errors are usually not critical
        }
    }

    suspend fun revokeAccess() {
        try {
            Log.d("GoogleAuthService", "Revoking Google access")
            googleSignInClient.revokeAccess().await()
            Log.d("GoogleAuthService", "Google access revoked successfully")
        } catch (e: Exception) {
            Log.e("GoogleAuthService", "Error revoking Google access", e)
        }
    }
}