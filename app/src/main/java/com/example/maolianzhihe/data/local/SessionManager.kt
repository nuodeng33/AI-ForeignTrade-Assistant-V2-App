package com.example.maolianzhihe.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.maolianzhihe.model.AuthResponse

object SessionManager {
    private const val PREF_NAME = "user_info"
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = createSecurePreferences(context.applicationContext)
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
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun isLoggedIn(): Boolean = preferences.getBoolean("isLoggedIn", false)

    fun token(): String? = preferences.getString("token", null)?.takeIf { it.isNotBlank() }

    fun username(): String = preferences.getString("username", "unknown") ?: "unknown"

    fun save(authResponse: AuthResponse) {
        preferences.edit()
            .putString("token", authResponse.jwt)
            .putInt("userId", authResponse.user.id)
            .putString("username", authResponse.user.username)
            .putString("email", authResponse.user.email ?: "")
            .putBoolean("isLoggedIn", true)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }
}
