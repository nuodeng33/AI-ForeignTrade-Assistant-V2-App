package com.example.maolianzhihe.model

data class ProfileSummary(
    val monthOrderCount: Int,
    val ongoingOrderCount: Int,
    val orderStatusData: List<ChartPoint>
)

data class ChartPoint(
    val label: String,
    val value: Int
)
