package com.example.maolianzhihe.data.repository

import com.example.maolianzhihe.data.local.SessionManager
import com.example.maolianzhihe.model.AuthResponse
import com.example.maolianzhihe.model.LoginRequest
import com.example.maolianzhihe.model.RegisterRequest
import com.example.maolianzhihe.network.ApiService
import com.example.maolianzhihe.ui.UiState

class AuthRepository(
    private val apiService: ApiService = ApiService.getInstance()
) {
    suspend fun login(identifier: String, password: String): UiState<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(identifier = identifier, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    SessionManager.save(body)
                    UiState.Success(body)
                } else {
                    UiState.Empty("服务器返回空数据")
                }
            } else {
                UiState.Error(authErrorMessage(response.code(), true), response.code())
            }
        } catch (e: Exception) {
            UiState.Error("网络连接失败：${e.message ?: "未知错误"}")
        }
    }

    suspend fun register(username: String, email: String, password: String): UiState<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(username = username, email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    SessionManager.save(body)
                    UiState.Success(body)
                } else {
                    UiState.Empty("服务器返回空数据")
                }
            } else {
                UiState.Error(authErrorMessage(response.code(), false), response.code())
            }
        } catch (e: Exception) {
            UiState.Error("网络连接失败：${e.message ?: "未知错误"}")
        }
    }

    private fun authErrorMessage(code: Int, isLogin: Boolean): String {
        return when {
            isLogin && code == 400 -> "用户名或密码错误"
            !isLogin && code == 400 -> "用户名或邮箱已被注册"
            else -> "请求失败，错误码：$code"
        }
    }
}
