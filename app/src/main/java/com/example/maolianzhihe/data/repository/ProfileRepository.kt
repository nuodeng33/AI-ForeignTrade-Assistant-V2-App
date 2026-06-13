package com.example.maolianzhihe.data.repository

import com.example.maolianzhihe.data.local.SessionManager
import com.example.maolianzhihe.model.ChartPoint
import com.example.maolianzhihe.model.Order
import com.example.maolianzhihe.model.ProfileSummary
import com.example.maolianzhihe.network.ApiService
import com.example.maolianzhihe.ui.UiState
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class ProfileRepository(
    private val apiService: ApiService = ApiService.getInstance()
) {
    fun username(): String = SessionManager.username()

    suspend fun getProfileSummary(): UiState<ProfileSummary> {
        return try {
            val response = apiService.getOrders()
            if (response.isSuccessful) {
                UiState.Success(buildSummary(response.body()?.data.orEmpty()))
            } else {
                UiState.Error("获取个人中心数据失败：${response.code()}", response.code())
            }
        } catch (e: Exception) {
            UiState.Error("网络连接失败：${e.message ?: "未知错误"}")
        }
    }

    fun logout() {
        SessionManager.clear()
    }

    private fun buildSummary(orders: List<Order>): ProfileSummary {
        val currentMonth = YearMonth.now()
        val monthOrderCount = orders.count { order ->
            orderMonth(order)?.let { it == currentMonth } == true
        }
        val ongoingOrderCount = orders.count { it.status.isOngoingStatus() }
        val orderStatusData = statusBuckets.map { (label, matcher) ->
            ChartPoint(label, orders.count { matcher(it.status) })
        }.filter { it.value > 0 }

        return ProfileSummary(
            monthOrderCount = monthOrderCount,
            ongoingOrderCount = ongoingOrderCount,
            orderStatusData = orderStatusData
        )
    }

    private fun orderMonth(order: Order): YearMonth? {
        val rawTime = order.createTime ?: order.createdAt
        return try {
            val localDate = Instant.parse(rawTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            YearMonth.from(localDate)
        } catch (_: Exception) {
            null
        }
    }

    private fun String.isOngoingStatus(): Boolean {
        return !contains("完成") && !contains("取消") && !contains("关闭")
    }

    private val statusBuckets: List<Pair<String, (String) -> Boolean>> = listOf(
        "待付款" to { status -> status.contains("待付") || status.contains("未付") },
        "待发货" to { status -> status.contains("待发") },
        "运输中" to { status -> status.contains("运输") || status.contains("清关") },
        "已完成" to { status -> status.contains("完成") },
        "其他" to { status ->
            !status.contains("待付") &&
                !status.contains("未付") &&
                !status.contains("待发") &&
                !status.contains("运输") &&
                !status.contains("清关") &&
                !status.contains("完成")
        }
    )
}
