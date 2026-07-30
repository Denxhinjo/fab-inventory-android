package com.denxhinjo.fabinventory.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

data class UserSession(
    val token: String,
    val role: String,
    val fullName: String,
    val email: String,
)

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("access_token")
        val ROLE = stringPreferencesKey("role")
        val FULL_NAME = stringPreferencesKey("full_name")
        val EMAIL = stringPreferencesKey("email")
    }

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { it[Keys.TOKEN] }

    val sessionFlow: Flow<UserSession?> = context.authDataStore.data.map { prefs ->
        val token = prefs[Keys.TOKEN] ?: return@map null
        UserSession(
            token = token,
            role = prefs[Keys.ROLE].orEmpty(),
            fullName = prefs[Keys.FULL_NAME].orEmpty(),
            email = prefs[Keys.EMAIL].orEmpty(),
        )
    }

    suspend fun saveSession(token: String, role: String, fullName: String, email: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.ROLE] = role
            prefs[Keys.FULL_NAME] = fullName
            prefs[Keys.EMAIL] = email
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { it.clear() }
    }

    /** Synchronous read for use inside the OkHttp interceptor. */
    suspend fun currentToken(): String? = tokenFlow.first()
}
