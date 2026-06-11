package com.example.maolianzhihe

import android.app.Application
import com.example.maolianzhihe.data.local.SessionManager

class ForeignTradeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
