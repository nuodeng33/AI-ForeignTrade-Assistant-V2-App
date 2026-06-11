package com.example.maolianzhihe.model

// Strapi 标准响应格式
data class StrapiResponse<T>(
    val data: T,
    val meta: Meta? = null
)

data class StrapiListResponse<T>(
    val data: List<T>,
    val meta: Meta? = null
)

data class Meta(
    val pagination: Pagination? = null
)

data class Pagination(
    val page: Int,
    val pageSize: Int,
    val pageCount: Int,
    val total: Int
)

// 订单数据
data class Order(
    val id: Int,
    val orderNumber: String,
    val goodsInfo: String,
    val status: String,
    val createdAt: String,
    val documentId: String? = null,
    val createTime: String? = null
)

data class OrderAttributes(
    val orderNumber: String,
    val goodsInfo: String,
    val status: String,
    val createdAt: String,
    val createTime: String? = null
)

// 创建订单请求
data class OrderRequest(
    val data: OrderRequestData
)

data class OrderRequestData(
    val orderNumber: String,
    val goodsInfo: String,
    val status: String,
    val createTime: String
)