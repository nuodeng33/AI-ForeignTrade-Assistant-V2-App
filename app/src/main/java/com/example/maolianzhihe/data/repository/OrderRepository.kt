package com.example.maolianzhihe.data.repository

import com.example.maolianzhihe.model.Order
import com.example.maolianzhihe.model.OrderRequest
import com.example.maolianzhihe.network.ApiService
import com.example.maolianzhihe.ui.UiState

class OrderRepository(
    private val apiService: ApiService = ApiService.getInstance()
) {
    suspend fun getOrders(): UiState<List<Order>> {
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful) {
                val orders = response.body()?.data.orEmpty()
                if (orders.isEmpty()) UiState.Empty("暂无订单") else UiState.Success(orders)
            } else {
                UiState.Error("服务器错误：${response.code()}", response.code())
            }
        } catch (e: Exception) {
            UiState.Error("网络连接失败：${e.message ?: "未知错误"}")
        }
    }

    suspend fun createOrder(request: OrderRequest): UiState<Order> {
        return try {
            val response = apiService.createOrder(request)
            if (response.isSuccessful) {
                val order = response.body()?.data
                if (order != null) UiState.Success(order) else UiState.Empty("服务器返回空数据")
            } else {
                UiState.Error("创建订单失败：${response.code()}", response.code())
            }
        } catch (e: Exception) {
            UiState.Error("网络连接失败：${e.message ?: "未知错误"}")
        }
    }
}
