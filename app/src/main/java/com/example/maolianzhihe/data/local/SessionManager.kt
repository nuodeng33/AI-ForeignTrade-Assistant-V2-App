package com.example.maolianzhihe.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.maolianzhihe.model.AuthResponse

object SessionManager {
    private const val PREF_NAME = "user_info"
    private lateinit var preferences: SharedPreferences
    private lateinit var legacyPreferences: SharedPreferences

    fun init(context: Context) {
        val appContext = context.applicationContext
        legacyPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        preferences = createSecurePreferences(appContext)
    }

    private fun createSecurePreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            legacyPreferences
        }
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("isLoggedIn", false) || legacyPreferences.getBoolean("isLoggedIn", false)
    }

    fun token(): String? {
        return preferences.getString("token", null)?.takeIf { it.isNotBlank() }
            ?: legacyPreferences.getString("token", null)?.takeIf { it.isNotBlank() }
    }

    fun username(): String {
        return preferences.getString("username", null)
            ?: legacyPreferences.getString("username", null)
            ?: "unknown"
    }

    fun save(authResponse: AuthResponse) {
        writeSession(preferences, authResponse)
        if (preferences !== legacyPreferences) {
            writeSession(legacyPreferences, authResponse)
        }
    }

    private fun writeSession(target: SharedPreferences, authResponse: AuthResponse) {
        target.edit()
            .putString("token", authResponse.jwt)
            .putInt("userId", authResponse.user.id)
            .putString("username", authResponse.user.username)
            .putString("email", authResponse.user.email ?: "")
            .putBoolean("isLoggedIn", true)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
        if (preferences !== legacyPreferences) {
            legacyPreferences.edit().clear().apply()
        }
    }
}
