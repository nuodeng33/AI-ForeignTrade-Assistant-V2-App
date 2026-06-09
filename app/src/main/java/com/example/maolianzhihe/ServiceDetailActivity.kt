package com.example.maolianzhihe

import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class ServiceDetailActivity : AppCompatActivity() {
    // 传参Key + 服务对应的网页URL（替换为你的实际链接）
    companion object {
        const val KEY_URL = "service_url"
        const val KEY_TITLE = "service_title"
        const val URL_MARKET_RESEARCH = "https://www.fxbaogao.com/?keyword=%E5%B8%82%E5%9C%BA%E7%A0%94%E7%A9%B6%E6%8A%A5%E5%91%8A&refer=bing&msclkid=e8ad5609d0d4147971cf3f36b4724956"   // 市场调研
        const val URL_CUSTOMS_CLEARANCE = "https://www.chinapantom.com/?msclkid=2b0f9672225a1cd08bc7ff4d44e4cb65#bing&%E6%8A%A5%E5%85%B3%E4%B8%8E%E6%B8%85%E5%85%B3&%E6%8A%A5%E5%85%B3%E4%B8%8E%E6%B8%85%E5%85%B3%E6%9C%8D%E5%8A%A1&p" // 报关清关
        const val URL_LOGISTICS = "https://www.zebra.cn/cn/zh/industry/transportation-logistics.html?tactic_type=SEMB&tactic_detail=TL_Transport_Logistics_Text+ZH_APAC_CN&QueryString=%E7%89%A9%E6%B5%81%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88"// 物流解决方案
        const val URL_LAW_CONSULT = "https://qianhu.wejianzhan.com/site/wjzefu83/432f0821-01e9-415b-b7ad-8e43998d56db/?bing&q=%E5%A4%96%E8%B4%B8%E6%B3%95%E5%BE%8B%E5%92%A8%E8%AF%A2&k=%E5%A4%96%E8%B4%B8%E6%B3%95%E5%BE%8B%E5%92%A8%E8%AF%A2&msclkid=1056172ae8c41c59864d7e15987d0d02"
        const val URL_YOUZHNEG = "https://www.ems.com.cn/"

    }

    private lateinit var wvServiceDetail: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_detail)

        // 初始化控件
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        wvServiceDetail = findViewById(R.id.wv_service_detail)

        // 接收从ServiceActivity传递的参数
        val targetUrl = intent.getStringExtra(KEY_URL) ?: ""
        val serviceTitle = intent.getStringExtra(KEY_TITLE) ?: "服务详情"
        tvTitle.text = serviceTitle

        // 初始化WebView（修复所有警告）
        initWebViewSettings()

        // 加载指定网页
        if (targetUrl.isNotEmpty()) {
            wvServiceDetail.loadUrl(targetUrl)
        } else {
            Toast.makeText(this, "网页地址为空，请检查配置", Toast.LENGTH_SHORT).show()
        }

        // 返回按钮：关闭当前页面
        ivBack.setOnClickListener {
            finish()
        }

        // 修复：替换废弃的onBackPressed()，使用新的回退回调
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (wvServiceDetail.canGoBack()) {
                    wvServiceDetail.goBack() // 网页回退
                } else {
                    finish() // 无网页可回退则关闭页面
                }
            }
        })
    }

    /**
     * 配置WebView（消除所有安全/废弃提示）
     */
    private fun initWebViewSettings() {
        val webSettings = wvServiceDetail.settings

        // 启用JavaScript（添加安全注释，抑制警告）
        // 注意：仅对信任的网页启用，避免XSS安全风险
        webSettings.javaScriptEnabled = true

        // 屏幕适配
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        // 缩放配置
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // 缓存与存储
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.domStorageEnabled = true // 启用DOM存储，兼容复杂网页

        // 修复：使用新API的shouldOverrideUrlLoading，消除废弃提示
        wvServiceDetail.webViewClient = object : WebViewClient() {
            // 新API（Android 7.0+）
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                view.loadUrl(url)
                return true
            }

            // 兼容旧版本（Android 6.0及以下）
            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
    }

    // 销毁WebView，防止内存泄漏（核心优化）
    override fun onDestroy() {
        wvServiceDetail.destroy()
        super.onDestroy()
    }
}