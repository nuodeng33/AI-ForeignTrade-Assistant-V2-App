package com.example.maolianzhihe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class ServiceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        initBottomNav(R.id.tv_nav_text1)

        try {
            // 1. 初始化标题栏、底部导航
            initTitleBar("我们的服务", showSetting = true)
            initBottomNav(R.id.nav_service)

            // 2. 设置按钮跳转逻辑
            val ivSetting = findViewById<ImageView>(R.id.iv_setting)
            ivSetting.setOnClickListener {
                val intent = Intent(this, SettingActivity::class.java)
                intent.putExtra("FROM_PAGE_ID", R.id.nav_service)
                startActivity(intent)
            }

            // 3. 市场调研-查看详情 → 跳WebView网址
            val tvMarketDetail = findViewById<TextView>(R.id.tv_market_detail)
            tvMarketDetail.setOnClickListener {
                jumpToWebViewDetail(
                    ServiceDetailActivity.URL_MARKET_RESEARCH,
                    "市场调研与分析"
                )
            }

            // 4. 报关清关-立即咨询 → 跳WebView网址
            val tvCustomsConsult = findViewById<TextView>(R.id.tv_customs_consult)
            tvCustomsConsult.setOnClickListener {
                jumpToWebViewDetail(
                    ServiceDetailActivity.URL_CUSTOMS_CLEARANCE,
                    "报关与清关服务"
                )
            }

            // 5. 物流解决方案-卡片整体点击 → 跳物流详情页（查配送信息）
            val llLogisticsCard = findViewById<LinearLayout>(R.id.ll_logistics_card)
            llLogisticsCard.setOnClickListener {
                val intent = Intent(this, LogisticsDetailActivity::class.java)
                intent.putExtra("ORDER_ID", "LOG20251215001") // 传递订单ID
                startActivity(intent)
                Toast.makeText(this, "查看物流配送信息", Toast.LENGTH_SHORT).show()
            }

            // 6. 物流解决方案-查看详情按钮 → 跳WebView网址
            val tvLogisticsDetail = findViewById<TextView>(R.id.tv_logistics_detail)
            tvLogisticsDetail.setOnClickListener {
                jumpToWebViewDetail(
                    ServiceDetailActivity.URL_LOGISTICS,
                    "物流解决方案"
                )
            }

            // 7. 法律咨询-在线咨询 → 跳WebView网址
            val tvLawConsult = findViewById<TextView>(R.id.tv_law_consult)
            tvLawConsult.setOnClickListener {
                jumpToWebViewDetail(
                    ServiceDetailActivity.URL_LAW_CONSULT,
                    "外贸法律咨询"
                )
            }

        } catch (e: Exception) {
            Toast.makeText(this, "服务页面初始化失败：${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    /**
     * 封装WebView详情页跳转逻辑（统一异常处理）
     */
    private fun jumpToWebViewDetail(targetUrl: String, pageTitle: String) {
        try {
            val intent = Intent(this, ServiceDetailActivity::class.java)
            intent.putExtra(ServiceDetailActivity.KEY_URL, targetUrl)
            intent.putExtra(ServiceDetailActivity.KEY_TITLE, pageTitle)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "跳转${pageTitle}详情页失败：${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}