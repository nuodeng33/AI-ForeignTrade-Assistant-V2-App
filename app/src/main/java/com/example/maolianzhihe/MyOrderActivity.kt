package com.example.maolianzhihe

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.maolianzhihe.model.Order
import com.example.maolianzhihe.ui.UiState
import com.example.maolianzhihe.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyOrderActivity : BaseActivity() {
    private lateinit var tvAll: TextView
    private lateinit var tvUnpaid: TextView
    private lateinit var tvUnshipped: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvCreateOrder: TextView
    private lateinit var ivBack: ImageView
    private lateinit var orderViewModel: OrderViewModel

    private val orderList = mutableListOf<OrderInfo>()
    private var selectedTabIndex = 0

    private val orderItemIds = listOf(R.id.order_item_1, R.id.order_item_2, R.id.order_item_3)
    private val tvOrderNameIds = listOf(R.id.tv_order_name1, R.id.tv_order_name2, R.id.tv_order_name3)
    private val tvOrderNoIds = listOf(R.id.tv_order_no1, R.id.tv_order_no2, R.id.tv_order_no3)
    private val tvOrderStatusIds = listOf(R.id.tv_order_status1, R.id.tv_order_status2, R.id.tv_order_status3)

    private val requestCodeCreateOrder = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order)

        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]

        initTitleBar("我的订单", showSetting = true, showBack = true)
        initBottomNav(R.id.nav_mine)
        initViews()
        observeOrdersState()
        setClickListeners()
        bindOrderItemClick()
        initBackPressedCallback()
        orderViewModel.loadOrders()
    }

    private fun initViews() {
        ivBack = findViewById(R.id.iv_back)
        tvCreateOrder = findViewById(R.id.tv_create_order)
        tvAll = findViewById(R.id.tv_all)
        tvUnpaid = findViewById(R.id.tv_unpaid)
        tvUnshipped = findViewById(R.id.tv_unshipped)
        tvCompleted = findViewById(R.id.tv_completed)
    }

    private fun observeOrdersState() {
        orderViewModel.ordersState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Toast.makeText(this, "正在加载订单...", Toast.LENGTH_SHORT).show()
                is UiState.Success -> {
                    orderList.clear()
                    orderList.addAll(state.data.map { it.toOrderInfo() })
                    refreshOrderListUI()
                }
                is UiState.Empty -> {
                    orderList.clear()
                    refreshOrderListUI()
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    loadDefaultOrders()
                }
            }
        }
    }

    private fun setClickListeners() {
        ivBack.setOnClickListener { goBackToPersonalCenter() }

        findViewById<ImageView>(R.id.iv_setting)?.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("FROM_PAGE_ID", R.id.nav_mine)
            startActivity(intent)
        }

        tvCreateOrder.setOnClickListener {
            try {
                val intent = Intent(this@MyOrderActivity, CreateOrderActivity::class.java)
                startActivityForResult(intent, requestCodeCreateOrder)
            } catch (e: Exception) {
                Toast.makeText(this, "跳转创建订单页失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        tvAll.setOnClickListener { selectTab(0) }
        tvUnpaid.setOnClickListener { selectTab(1) }
        tvUnshipped.setOnClickListener { selectTab(2) }
        tvCompleted.setOnClickListener { selectTab(3) }
    }

    private fun loadDefaultOrders() {
        orderList.clear()
        orderList.addAll(
            listOf(
                OrderInfo("ORDER001", "跨境物流包裹", "9418368276452", "待发货", getCurrentTime()),
                OrderInfo("ORDER002", "外贸样品快递", "9876543210987", "运输中", getCurrentTime()),
                OrderInfo("ORDER003", "清关文件快递", "1234567890123", "已完成", getCurrentTime())
            )
        )
        refreshOrderListUI()
    }

    private fun refreshOrderListUI() {
        val displayOrders = filteredOrders().take(3)
        for (i in orderItemIds.indices) {
            if (i < displayOrders.size) {
                val order = displayOrders[i]
                findViewById<TextView>(tvOrderNameIds[i])?.text = order.orderName
                findViewById<TextView>(tvOrderNoIds[i])?.text = "单号：${order.trackingNumber}"
                findViewById<TextView>(tvOrderStatusIds[i])?.text = order.orderStatus
                findViewById<View>(orderItemIds[i])?.visibility = View.VISIBLE
            } else {
                findViewById<View>(orderItemIds[i])?.visibility = View.GONE
            }
        }
    }

    private fun filteredOrders(): List<OrderInfo> {
        return when (selectedTabIndex) {
            1 -> orderList.filter { it.orderStatus.contains("待付") || it.orderStatus.contains("未付") }
            2 -> orderList.filter { it.orderStatus.contains("待发") }
            3 -> orderList.filter { it.orderStatus.contains("完成") }
            else -> orderList
        }
    }

    private fun bindOrderItemClick() {
        orderItemIds.forEachIndexed { index, itemId ->
            findViewById<View>(itemId)?.setOnClickListener {
                val displayOrders = filteredOrders()
                if (index < displayOrders.size) {
                    val order = displayOrders[index]
                    showTrackingOptions(order.trackingNumber, order.orderName)
                }
            }
        }
    }

    private fun showTrackingOptions(trackingNumber: String, orderName: String) {
        try {
            copyToClipboard("快递单号", trackingNumber)
            val options = arrayOf("在邮政官网查询", "在菜鸟裹裹查询", "分享快递单号", "查看订单详情")

            AlertDialog.Builder(this)
                .setTitle("订单查询：$orderName")
                .setMessage("快递单号：$trackingNumber\n单号已自动复制到剪贴板")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openPostalTracking(trackingNumber)
                        1 -> openCainiaoTracking(trackingNumber)
                        2 -> shareTrackingNumber(trackingNumber, orderName)
                        3 -> showOrderDetails(trackingNumber, orderName)
                    }
                }
                .setNegativeButton("取消", null)
                .setPositiveButton("知道了") { _, _ ->
                    jumpToWebViewDetail(ServiceDetailActivity.URL_YOUZHNEG, orderName)
                }
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "操作失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPostalTracking(trackingNumber: String) {
        val postalUrl = "https://www.11183.com.cn/mailtracking/yjcx/index.html"
        Toast.makeText(this, "请在邮政官网输入单号：\n$trackingNumber", Toast.LENGTH_LONG).show()
        openExternalUrl(postalUrl, "未找到浏览器应用")
    }

    private fun openCainiaoTracking(trackingNumber: String) {
        openExternalUrl("https://www.cainiao.com/query.html?mailNoList=$trackingNumber", "无法打开菜鸟裹裹")
    }

    private fun openExternalUrl(url: String, errorMessage: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(packageManager) != null) startActivity(intent) else Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareTrackingNumber(trackingNumber: String, orderName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "快递单号分享")
            putExtra(Intent.EXTRA_TEXT, "【$orderName】\n快递单号：$trackingNumber\n查询链接：\n1. 邮政查询：https://www.11183.com.cn\n2. 菜鸟裹裹：https://www.cainiao.com")
        }
        startActivity(Intent.createChooser(shareIntent, "分享快递单号"))
    }

    private fun jumpToWebViewDetail(targetUrl: String, pageTitle: String) {
        val intent = Intent(this, ServiceDetailActivity::class.java)
        intent.putExtra(ServiceDetailActivity.KEY_URL, targetUrl)
        intent.putExtra(ServiceDetailActivity.KEY_TITLE, pageTitle)
        startActivity(intent)
    }

    private fun showOrderDetails(trackingNumber: String, orderName: String) {
        val order = orderList.find { it.trackingNumber == trackingNumber }
        val details = if (order != null) {
            "订单名称：${order.orderName}\n快递单号：${order.trackingNumber}\n订单状态：${order.orderStatus}\n创建时间：${order.createTime}"
        } else {
            "订单信息：$orderName\n快递单号：$trackingNumber"
        }
        AlertDialog.Builder(this)
            .setTitle("订单详情")
            .setMessage(details)
            .setPositiveButton("关闭", null)
            .setNeutralButton("复制单号") { _, _ -> copyToClipboard("快递单号", trackingNumber) }
            .show()
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCodeCreateOrder && resultCode == RESULT_OK) {
            orderViewModel.loadOrders()
        }
    }

    private fun initBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToPersonalCenter()
            }
        })
    }

    private fun goBackToPersonalCenter() {
        val intent = Intent(this, PersonalCenterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun selectTab(index: Int) {
        selectedTabIndex = index
        resetTabs()
        when (index) {
            0 -> updateTabStyle(tvAll)
            1 -> updateTabStyle(tvUnpaid)
            2 -> updateTabStyle(tvUnshipped)
            3 -> updateTabStyle(tvCompleted)
        }
        refreshOrderListUI()
    }

    private fun updateTabStyle(tv: TextView) {
        tv.setTextColor(ContextCompat.getColor(this, R.color.primary))
        tv.setBackgroundResource(R.drawable.shape_edittext)
    }

    private fun resetTabs() {
        val grayColor = ContextCompat.getColor(this, R.color.gray_400)
        listOf(tvAll, tvUnpaid, tvUnshipped, tvCompleted).forEach { tab ->
            tab.setTextColor(grayColor)
            tab.setBackgroundResource(0)
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        return sdf.format(Date())
    }

    private fun Order.toOrderInfo(): OrderInfo {
        return OrderInfo(
            orderId = id.toString(),
            orderName = goodsInfo,
            trackingNumber = orderNumber,
            orderStatus = status,
            createTime = createdAt
        )
    }

    private data class OrderInfo(
        val orderId: String,
        val orderName: String,
        val trackingNumber: String,
        val orderStatus: String,
        val createTime: String
    )
}
