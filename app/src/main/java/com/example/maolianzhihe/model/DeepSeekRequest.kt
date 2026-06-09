package com.example.maolianzhihe.model

// DeepSeek请求模型
data class DeepSeekRequest(
    val question: String,
    val history: List<Message>
)

// 消息模型（用于历史记录）
data class Message(
    val role: String,    // "user" 或 "assistant"
    val content: String
)