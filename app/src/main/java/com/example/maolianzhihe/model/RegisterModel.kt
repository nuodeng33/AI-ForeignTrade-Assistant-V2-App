package com.example.maolianzhihe.model

// 注册请求参数 - 简化版
data class RegisterRequest(
    val username: String,
    val email: String,  // Strapi要求必须有邮箱
    val password: String
)

// 旧的响应保留（可选，但我们使用新的AuthResponse）
// 注册响应
data class RegisterResponse(
    val code: Int,
    val msg: String
)