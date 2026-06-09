package com.example.maolianzhihe.model

// DeepSeek响应模型
data class DeepSeekResponse(
    val success: Boolean,
    val data: DeepSeekData?
)

// 数据部分
data class DeepSeekData(
    val question: String,
    val answer: String,
    val newHistory: List<Message>
)