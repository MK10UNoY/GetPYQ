package com.infomanix.getpyq.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Singleton DataStore tied to app context
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences private constructor(context: Context) {
    private val dataStore = context.dataStore

    // User info keys
    private val userNameKey = stringPreferencesKey("user_name")
    private val scholarIdKey = stringPreferencesKey("scholar_id")
    private val userTypeKey = stringPreferencesKey("user_type")
    private val userEmailKey = stringPreferencesKey("user_email")

    // Splash screen key
    private val hasSeenSplashKey = booleanPreferencesKey("has_seen_splash")

    // --- User Data Handling ---
    val userName: Flow<String?> = dataStore.data.map { it[userNameKey] }
    val scholarId: Flow<String?> = dataStore.data.map { it[scholarIdKey] }
    val userType: Flow<String?> = dataStore.data.map { it[userTypeKey] }
    val userEmail: Flow<String?> = dataStore.data.map { it[userEmailKey] }

    suspend fun saveUserInfo(name: String, email: String, scholarId: String, userType: String) {
        dataStore.edit { prefs ->
            prefs[userNameKey] = name
            prefs[userEmailKey] = email
            prefs[scholarIdKey] = scholarId
            prefs[userTypeKey] = userType
        }
    }
    suspend fun updateUserInfo(email: String, userType: String) {
        dataStore.edit { prefs ->
            prefs[userEmailKey] = email
            prefs[userTypeKey] = userType
        }
    }

    suspend fun clearUserInfo() {
        // Log all stored values first
        dataStore.data.collect { preferences ->
            // Convert the Preferences object to a map and log all the keys and values
            preferences.asMap().forEach { (key, value) ->
                Log.d("user", "Key: $key, Value: $value")
            }
        }

        dataStore.edit { it.clear() }
        Log.d("UsrPrefs", "User info cleared")
        dataStore.data.collect { preferences ->
            // Convert the Preferences object to a map and log all the keys and values
            preferences.asMap().forEach { (key, value) ->
                Log.d("UsrPrefs", "Key: $key, Value: $value")
            }
        }

    }

    // --- Splash State Handling ---
    val hasSeenSplash: Flow<Boolean> = dataStore.data.map { it[hasSeenSplashKey] ?: false }

    suspend fun setSplashSeen() {
        dataStore.edit { prefs ->
            prefs[hasSeenSplashKey] = true
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
