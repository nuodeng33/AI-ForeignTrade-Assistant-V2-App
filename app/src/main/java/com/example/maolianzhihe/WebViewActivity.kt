package com.example.maolianzhihe

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback

class WebViewActivity : BaseActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // 获取跳转参数
        val webUrl = intent.getStringExtra("WEB_URL") ?: ""
        val pageTitle = intent.getStringExtra("PAGE_TITLE") ?: "网页浏览"

        // 初始化标题栏
        findViewById<TextView>(R.id.tv_title).text = pageTitle

        // 返回按钮
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // 初始化WebView
        webView = findViewById(R.id.web_view)
        val webSettings = webView.settings

        // 启用JS（添加安全注释，抑制警告）
        // 注意：仅对信任的网页启用，避免XSS安全风险
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true // 启用本地存储
        webSettings.allowFileAccess = true
        webSettings.builtInZoomControls = true // 允许缩放
        webSettings.displayZoomControls = false // 隐藏缩放按钮

        // 设置WebViewClient，避免跳转到系统浏览器
        webView.webViewClient = WebViewClient()

        // 加载邮政官网
        if (webUrl.isNotEmpty()) {
            webView.loadUrl(webUrl)
        } else {
            webView.loadData("页面加载失败", "text/html;charset=utf-8", "utf-8")
        }

        // ========== 修复1：替换废弃的onBackPressed()方法 ==========
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack() // WebView回退上一页
                } else {
                    finish() // 关闭当前页面
                }
            }
        })
    }

    // ========== 修复2：移除废弃的onBackPressed()方法 ==========

    // 销毁时释放WebView资源
    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}