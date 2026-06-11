package com.example.maolianzhihe

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
    companion object {
        const val KEY_URL = "service_url"
        const val KEY_TITLE = "service_title"
        const val URL_MARKET_RESEARCH = "https://www.fxbaogao.com/?keyword=%E5%B8%82%E5%9C%BA%E7%A0%94%E7%A9%B6%E6%8A%A5%E5%91%8A&refer=bing&msclkid=e8ad5609d0d4147971cf3f36b4724956"
        const val URL_CUSTOMS_CLEARANCE = "https://www.chinapantom.com/?msclkid=2b0f9672225a1cd08bc7ff4d44e4cb65#bing&%E6%8A%A5%E5%85%B3%E4%B8%8E%E6%B8%85%E5%85%B3&%E6%8A%A5%E5%85%B3%E4%B8%8E%E6%B8%85%E5%85%B3%E6%9C%8D%E5%8A%A1&p"
        const val URL_LOGISTICS = "https://www.zebra.cn/cn/zh/industry/transportation-logistics.html?tactic_type=SEMB&tactic_detail=TL_Transport_Logistics_Text+ZH_APAC_CN&QueryString=%E7%89%A9%E6%B5%81%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88"
        const val URL_LAW_CONSULT = "https://qianhu.wejianzhan.com/site/wjzefu83/432f0821-01e9-415b-b7ad-8e43998d56db/?bing&q=%E5%A4%96%E8%B4%B8%E6%B3%95%E5%BE%8B%E5%92%A8%E8%AF%A2&k=%E5%A4%96%E8%B4%B8%E6%B3%95%E5%BE%8B%E5%92%A8%E8%AF%A2&msclkid=1056172ae8c41c59864d7e15987d0d02"
        const val URL_YOUZHNEG = "https://www.ems.com.cn/"

        private val TRUSTED_HOSTS = setOf(
            "www.fxbaogao.com",
            "www.chinapantom.com",
            "www.zebra.cn",
            "qianhu.wejianzhan.com",
            "www.ems.com.cn",
            "ems.com.cn"
        )
    }

    private lateinit var wvServiceDetail: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_detail)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        wvServiceDetail = findViewById(R.id.wv_service_detail)

        val targetUrl = intent.getStringExtra(KEY_URL).orEmpty()
        val serviceTitle = intent.getStringExtra(KEY_TITLE) ?: "服务详情"
        tvTitle.text = serviceTitle

        initWebViewSettings()

        if (targetUrl.isNotEmpty() && isTrustedUrl(targetUrl)) {
            wvServiceDetail.loadUrl(targetUrl)
        } else {
            Toast.makeText(this, "网页地址不受信任，已拦截", Toast.LENGTH_SHORT).show()
        }

        ivBack.setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (wvServiceDetail.canGoBack()) wvServiceDetail.goBack() else finish()
            }
        })
    }

    private fun initWebViewSettings() {
        val webSettings = wvServiceDetail.settings
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = false
        webSettings.allowContentAccess = false
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        wvServiceDetail.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                return if (isTrustedUrl(url)) {
                    view.loadUrl(url)
                    true
                } else {
                    Toast.makeText(this@ServiceDetailActivity, "已拦截不受信任链接", Toast.LENGTH_SHORT).show()
                    true
                }
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return if (isTrustedUrl(url)) {
                    view.loadUrl(url)
                    true
                } else {
                    Toast.makeText(this@ServiceDetailActivity, "已拦截不受信任链接", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
    }

    private fun isTrustedUrl(url: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.scheme == "https" && TRUSTED_HOSTS.contains(uri.host)
        } catch (_: Exception) {
            false
        }
    }

    override fun onDestroy() {
        wvServiceDetail.destroy()
        super.onDestroy()
    }
}
