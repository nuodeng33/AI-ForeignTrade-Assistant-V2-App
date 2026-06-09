package com.example.maolianzhihe

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class LogisticsDetailActivity : BaseActivity() {

    // 声明控件
    private lateinit var tvOrderId: TextView
    private lateinit var tvDeliveryTime: TextView
    private lateinit var tvDeliveryRoute: TextView
    private lateinit var tvRealTimeLocation: TextView
    private lateinit var tvRefreshLocation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logistics_detail)

        // 绑定控件
        bindViews()

        // 返回按钮
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // 获取传递的订单ID（从ServiceActivity跳转时传入）
        val orderId = intent.getStringExtra("ORDER_ID") ?: "默认订单号"

        // 动态更新UI（模拟从接口获取数据）
        updateLogisticsInfo(orderId)

        // 刷新位置点击事件
        tvRefreshLocation.setOnClickListener {
            refreshRealTimeLocation()
        }
    }

    /**
     * 绑定所有控件
     */
    private fun bindViews() {
        tvOrderId = findViewById(R.id.tv_order_id)
        tvDeliveryTime = findViewById(R.id.tv_delivery_time)
        tvDeliveryRoute = findViewById(R.id.tv_delivery_route)
        tvRealTimeLocation = findViewById(R.id.tv_real_time_location)
        tvRefreshLocation = findViewById(R.id.tv_refresh_location)
    }

    /**
     * 模拟从接口获取物流信息并更新UI
     */
    private fun updateLogisticsInfo(orderId: String) {
        // 实际开发中替换为接口请求
        tvOrderId.text = "订单号：$orderId"
        tvDeliveryTime.text = "预计配送时间：2025-12-18 16:30" // 动态更新时间
        tvDeliveryRoute.text = "上海港 → 新加坡港 → 鹿特丹港 → 德国汉堡仓库"
        tvRealTimeLocation.text = "新加坡港 已装船，等待起航" // 动态更新位置
    }

    /**
     * 刷新实时位置
     */
    private fun refreshRealTimeLocation() {
        // 模拟接口请求刷新位置
        tvRealTimeLocation.text = "马六甲海峡 航行中"
        Toast.makeText(this, "位置已更新", Toast.LENGTH_SHORT).show()
    }
}