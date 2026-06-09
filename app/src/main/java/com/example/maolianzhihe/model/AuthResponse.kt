package com.example.maolianzhihe.model

// 认证响应（用于Strapi登录/注册）
data class AuthResponse(
    val jwt: String,
    val user: SimpleUser
)