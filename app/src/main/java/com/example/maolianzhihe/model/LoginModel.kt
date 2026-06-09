package com.example.maolianzhihe.model

// 登录请求参数 - 为了兼容Strapi，修改字段名
data class LoginRequest(
    val identifier: String,  // Strapi要求这个字段名
    val password: String
)

// 旧的响应保留（可选，但我们使用新的AuthResponse）
// 登录响应
data class LoginResponse(
    val code: Int,
    val msg: String,
    val data: UserData?
)

// 用户数据
data class UserData(
    val token: String,
    val userId: String,
    val userName: String
)