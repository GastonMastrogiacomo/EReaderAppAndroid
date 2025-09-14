package com.ereaderapp.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.ereaderapp.android.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class TokenManager @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore
    private val gson = Gson()

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_KEY = stringPreferencesKey("user_data")
    }

    fun getTokenFlow(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    fun getUserFlow(): Flow<User?> {
        return dataStore.data.map { preferences ->
            preferences[USER_KEY]?.let { userJson ->
                try {
                    gson.fromJson(userJson, User::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    suspend fun saveAuthData(token: String, user: User) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_KEY] = gson.toJson(user)
        }
    }

    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_KEY)
        }
    }

    fun getToken(): String? {
        return runBlocking {
            dataStore.data.first()[TOKEN_KEY]
        }
    }

    fun getUser(): User? {
        return runBlocking {
            dataStore.data.first()[USER_KEY]?.let { userJson ->
                try {
                    gson.fromJson(userJson, User::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}