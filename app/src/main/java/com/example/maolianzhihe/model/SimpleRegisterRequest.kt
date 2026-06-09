package com.example.maolianzhihe.model

// 简化注册请求
data class SimpleRegisterRequest(
    val username: String,
    val email: String,
    val password: String
)