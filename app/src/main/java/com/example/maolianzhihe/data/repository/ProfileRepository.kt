package com.example.maolianzhihe.data.repository

import com.example.maolianzhihe.data.local.SessionManager

class ProfileRepository {
    fun username(): String = SessionManager.username()

    fun logout() {
        SessionManager.clear()
    }
}
