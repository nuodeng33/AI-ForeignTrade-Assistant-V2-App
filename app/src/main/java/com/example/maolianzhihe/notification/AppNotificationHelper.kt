package com.example.maolianzhihe.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.maolianzhihe.data.local.SettingsManager

object AppNotificationHelper {
    const val CHANNEL_ORDERS = "orders"
    const val CHANNEL_ORDERS_SILENT = "orders_silent"
    const val CHANNEL_SYSTEM = "system"
    const val CHANNEL_AI = "ai"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(CHANNEL_ORDERS, "订单提醒", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_ORDERS_SILENT, "静默订单提醒", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
                enableVibration(false)
            },
            NotificationChannel(CHANNEL_SYSTEM, "系统通知", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel(CHANNEL_AI, "AI提醒", NotificationManager.IMPORTANCE_DEFAULT)
        )
        notificationManager.createNotificationChannels(channels)
    }

    fun orderChannelId(): String {
        return if (SettingsManager.isSilentNotificationMode()) CHANNEL_ORDERS_SILENT else CHANNEL_ORDERS
    }
}
