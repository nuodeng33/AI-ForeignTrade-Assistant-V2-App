package com.example.maolianzhihe.model

// 简化认证响应
data class SimpleAuthResponse(
    val jwt: String,
    val user: SimpleUser
)