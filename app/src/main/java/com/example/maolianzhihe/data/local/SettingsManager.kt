package com.example.maolianzhihe.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.os.LocaleListCompat

object SettingsManager {
    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_ZH = "zh"
    const val LANGUAGE_EN = "en"

    const val NOTIFICATION_ALL = "all"
    const val NOTIFICATION_ORDERS = "orders"
    const val NOTIFICATION_SILENT = "silent"
    const val NOTIFICATION_OFF = "off"

    private const val PREF_NAME = "app_settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_NOTIFICATION_MODE = "notification_mode"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun languageMode(): String {
        return preferences.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }

    fun setLanguageMode(mode: String) {
        preferences.edit().putString(KEY_LANGUAGE, mode).apply()
    }

    fun localeList(): LocaleListCompat = localeListFor(languageMode())

    fun localeListFor(mode: String): LocaleListCompat {
        return when (mode) {
            LANGUAGE_ZH -> LocaleListCompat.forLanguageTags("zh-CN")
            LANGUAGE_EN -> LocaleListCompat.forLanguageTags("en")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
    }

    fun notificationMode(): String {
        return preferences.getString(KEY_NOTIFICATION_MODE, NOTIFICATION_ALL) ?: NOTIFICATION_ALL
    }

    fun setNotificationMode(mode: String) {
        preferences.edit().putString(KEY_NOTIFICATION_MODE, mode).apply()
    }

    fun canSendOrderNotifications(): Boolean {
        return notificationMode() in setOf(NOTIFICATION_ALL, NOTIFICATION_ORDERS, NOTIFICATION_SILENT)
    }

    fun isSilentNotificationMode(): Boolean {
        return notificationMode() == NOTIFICATION_SILENT
    }
}
