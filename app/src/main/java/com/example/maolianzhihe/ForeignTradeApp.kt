package com.example.maolianzhihe

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.maolianzhihe.data.local.SettingsManager
import com.example.maolianzhihe.data.local.SessionManager
import com.example.maolianzhihe.notification.AppNotificationHelper

class ForeignTradeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        SettingsManager.init(this)
        AppCompatDelegate.setApplicationLocales(SettingsManager.localeList())
        AppNotificationHelper.createChannels(this)
    }
}
