package com.example.maolianzhihe

// 消息类型枚举（必须和Adapter在同一包下）
enum class ChatType {
    TYPE_AI,   // AI回复
    TYPE_USER  // 客户提问
}

// 聊天消息实体
data class ChatMessage(
    val content: String,
    val type: ChatType
)