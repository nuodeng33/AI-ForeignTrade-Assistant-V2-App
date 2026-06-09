package com.example.maolianzhihe

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.example.maolianzhihe.network.ApiService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MyOrderActivity : BaseActivity() {
    // 订单筛选标签
    private lateinit var tvAll: TextView
    private lateinit var tvUnpaid: TextView
    private lateinit var tvUnshipped: TextView
    private lateinit var tvCompleted: TextView
    // 功能按钮
    private lateinit var tvCreateOrder: TextView
    private lateinit var ivBack: ImageView

    // 存储从服务器获取的订单列表
    private val orderList = mutableListOf<OrderInfo>()

    // 订单列表项ID
    private val orderItemIds = listOf(R.id.order_item_1, R.id.order_item_2, R.id.order_item_3)
    private val tvOrderNameIds = listOf(R.id.tv_order_name1, R.id.tv_order_name2, R.id.tv_order_name3)
    private val tvOrderNoIds = listOf(R.id.tv_order_no1, R.id.tv_order_no2, R.id.tv_order_no3)
    private val tvOrderStatusIds = listOf(R.id.tv_order_status1, R.id.tv_order_status2, R.id.tv_order_status3)

    // 请求码
    private val REQUEST_CODE_CREATE_ORDER = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order)

        // 初始化标题栏和底部导航
        initTitleBar("我的订单", showSetting = true, showBack = true)
        initBottomNav(R.id.nav_mine)

        // 初始化所有控件
        initViews()
        // 设置所有点击事件
        setClickListeners()
        // 从服务器加载订单
        loadOrdersFromServer()
        // 绑定订单列表项点击事件
        bindOrderItemClick()

        // 返回键回调
        initBackPressedCallback()
    }

    /**
     * 初始化所有控件
     */
    private fun initViews() {
        ivBack = findViewById(R.id.iv_back)
        tvCreateOrder = findViewById(R.id.tv_create_order)
        tvAll = findViewById(R.id.tv_all)
        tvUnpaid = findViewById(R.id.tv_unpaid)
        tvUnshipped = findViewById(R.id.tv_unshipped)
        tvCompleted = findViewById(R.id.tv_completed)
    }

    /**
     * 设置所有点击事件
     */
    private fun setClickListeners() {
        // 返回按钮：强制跳回个人中心
        ivBack.setOnClickListener {
            goBackToPersonalCenter()
        }

        // 设置按钮跳转
        val ivSetting = findViewById<ImageView>(R.id.iv_setting)
        ivSetting?.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("FROM_PAGE_ID", R.id.nav_mine)
            startActivity(intent)
        }

        // 创建订单按钮
        tvCreateOrder.setOnClickListener {
            try {
                val intent = Intent(this@MyOrderActivity, CreateOrderActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_CREATE_ORDER)
                Toast.makeText(this, "进入创建订单页面", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "跳转创建订单页失败：${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        // 筛选标签点击事件
        tvAll.setOnClickListener { selectTab(0) }
        tvUnpaid.setOnClickListener { selectTab(1) }
        tvUnshipped.setOnClickListener { selectTab(2) }
        tvCompleted.setOnClickListener { selectTab(3) }
    }

    /**
     * 从服务器加载订单
     */
    private fun loadOrdersFromServer() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MyOrderActivity, "正在加载订单...", Toast.LENGTH_SHORT).show()

                val response = ApiService.getInstance().getOrders()
                Log.d("response", "loadOrdersFromServer: "+response)
                if (response.isSuccessful) {
                    val strapiResponse = response.body()
                    val orders = strapiResponse?.data ?: emptyList()
                    Log.d("strapiResponse", "loadOrdersFromServer: "+strapiResponse)
                    // 清空现有订单列表
                    orderList.clear()

                    // 添加服务器返回的订单
                    orders.forEach { order ->
                        orderList.add(OrderInfo(
                            orderId = order.id.toString(),
                            orderName = order.goodsInfo,
                            trackingNumber = order.orderNumber,
                            orderStatus = order.status,
                            createTime = order.createdAt
                        ))
                    }
//                    Log.d("strapiResponse", "loadOrdersFromServer: "+orderList)
                    // 刷新UI
                    refreshOrderListUI()
                    Toast.makeText(this@MyOrderActivity, "加载成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MyOrderActivity, "服务器错误：${response.code()}", Toast.LENGTH_SHORT).show()
                    loadDefaultOrders()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyOrderActivity, "网络连接失败：${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                loadDefaultOrders()
            }
        }
    }

    /**
     * 加载默认订单（网络失败时使用）
     */
    private fun loadDefaultOrders() {
        orderList.clear()
        orderList.addAll(listOf(
            OrderInfo("ORDER001", "跨境物流包裹", "9418368276452", "待发货", getCurrentTime()),
            OrderInfo("ORDER002", "外贸样品快递", "9876543210987", "运输中", getCurrentTime()),

            OrderInfo("ORDER003", "清关文件快递", "1234567890123", "已完成", getCurrentTime())
        ))
        refreshOrderListUI()
    }

    /**
     * 刷新订单列表UI
     */
    private fun refreshOrderListUI() {
        // 遍历显示所有订单（最多显示3条）
        for (i in 0 until 3) {

            if (i < orderList.size) {
                // 显示订单数据
                val order = orderList[i]
                findViewById<TextView>(tvOrderNameIds[i])?.text = order.orderName
                findViewById<TextView>(tvOrderNoIds[i])?.text = "单号：${order.trackingNumber}"
                findViewById<TextView>(tvOrderStatusIds[i])?.text = order.orderStatus
                // 显示订单项布局
                findViewById<View>(orderItemIds[i])?.visibility = View.VISIBLE
            } else {
                // 隐藏空的订单项
                findViewById<View>(orderItemIds[i])?.visibility = View.GONE
            }
        }


    }

    /**
     * 绑定订单列表项点击事件
     */
    private fun bindOrderItemClick() {
        orderItemIds.forEachIndexed { index, itemId ->
            val orderItem = findViewById<View>(itemId)
            orderItem?.setOnClickListener {
                if (index < orderList.size) {
                    val trackingNumber = orderList[index].trackingNumber
                    val orderName = orderList[index].orderName
                    showTrackingOptions(trackingNumber, orderName)
                }
            }
        }
    }

    /**
     * 显示快递查询选项对话框
     */
    private fun showTrackingOptions(trackingNumber: String, orderName: String) {
        try {
            // 先复制单号到剪贴板
            copyToClipboard("快递单号", trackingNumber)

            // 创建选项数组
            val options = arrayOf(
                "在邮政官网查询",
                "在菜鸟裹裹查询",
                "分享快递单号",
                "查看订单详情"
            )

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
                    Toast.makeText(this, "单号已复制", Toast.LENGTH_SHORT).show()
                    jumpToWebViewDetail(
                        ServiceDetailActivity.URL_YOUZHNEG,
                        "$orderName"
                    )

                }
                .show()

        } catch (e: Exception) {
            Toast.makeText(this, "操作失败：${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * 打开邮政官网查询
     */
    private fun openPostalTracking(trackingNumber: String) {
        try {
            // 邮政官方查询页面（用户需要手动输入单号）
            val postalUrl = "https://www.11183.com.cn/mailtracking/yjcx/index.html"

            // 显示提示信息
            Toast.makeText(this,
                "请在邮政官网输入单号：\n$trackingNumber",
                Toast.LENGTH_LONG
            ).show()

            // 打开浏览器
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(postalUrl))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "未找到浏览器应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开邮政官网", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 打开菜鸟裹裹查询
     */
    private fun openCainiaoTracking(trackingNumber: String) {
        try {
            // 菜鸟裹裹查询页面（支持直接传参）
            val cainiaoUrl = "https://www.cainiao.com/query.html?mailNoList=$trackingNumber"

            // 打开浏览器
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cainiaoUrl))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // 如果无法打开，提示用户安装菜鸟裹裹
                AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("是否安装菜鸟裹裹App？")
                    .setPositiveButton("安装") { _, _ ->
                        // 跳转到应用市场
                        try {
                            val marketUri = Uri.parse("market://details?id=com.cainiao.wireless")
                            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                            startActivity(marketIntent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "无法打开应用市场", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开菜鸟裹裹", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 分享快递单号
     */
    private fun shareTrackingNumber(trackingNumber: String, orderName: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "快递单号分享")
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                "【$orderName】\n" +
                        "快递单号：$trackingNumber\n" +
                        "查询链接：\n" +
                        "1. 邮政查询：https://www.11183.com.cn\n" +
                        "2. 菜鸟裹裹：https://www.cainiao.com"
            )

            startActivity(Intent.createChooser(shareIntent, "分享快递单号"))
        } catch (e: Exception) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show()
        }
    }


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
    /**
     * 显示订单详情
     */
    private fun showOrderDetails(trackingNumber: String, orderName: String) {
        try {
            // 查找对应的订单信息
            val order = orderList.find { it.trackingNumber == trackingNumber }

            val details = if (order != null) {
                """
                订单名称：${order.orderName}
                快递单号：${order.trackingNumber}
                订单状态：${order.orderStatus}
                创建时间：${order.createTime}
                
                说明：
                1. 此单号已复制到剪贴板
                2. 您可以在任意快递平台查询
                3. 如需帮助请联系客服
                """.trimIndent()
            } else {
                "订单信息：$orderName\n快递单号：$trackingNumber"
            }

            AlertDialog.Builder(this)
                .setTitle("订单详情")
                .setMessage(details)
                .setPositiveButton("关闭", null)
                .setNeutralButton("复制单号") { _, _ ->
                    copyToClipboard("快递单号", trackingNumber)
                    Toast.makeText(this, "单号已复制", Toast.LENGTH_SHORT).show()
                }
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "显示详情失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 复制文本到剪贴板
     */
    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * 接收创建订单页面返回的结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CREATE_ORDER && resultCode == RESULT_OK) {
            // 创建订单成功后，重新加载订单列表
            loadOrdersFromServer()
        }
    }

    /**
     * 初始化返回键回调
     */
    private fun initBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBackToPersonalCenter()
            }
        })
    }

    /**
     * 返回个人中心
     */
    private fun goBackToPersonalCenter() {
        try {
            val intent = Intent(this, PersonalCenterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
            Toast.makeText(this, "返回个人中心", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            finish()
            Toast.makeText(this, "返回失败，已关闭订单页面", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * 选中筛选标签并更新样式
     */
    private fun selectTab(index: Int) {
        resetTabs()
        when (index) {
            0 -> updateTabStyle(tvAll)
            1 -> updateTabStyle(tvUnpaid)
            2 -> updateTabStyle(tvUnshipped)
            3 -> updateTabStyle(tvCompleted)
        }
        val tabName = when(index) {
            0 -> "全部订单"
            1 -> "待付款订单"
            2 -> "待发货订单"
            3 -> "已完成订单"
            else -> "全部订单"
        }
        Toast.makeText(this, "已切换至：$tabName", Toast.LENGTH_SHORT).show()
    }

    /**
     * 更新选中标签样式
     */
    private fun updateTabStyle(tv: TextView) {
        tv.setTextColor(ContextCompat.getColor(this, R.color.primary))
        tv.setBackgroundResource(R.drawable.shape_edittext)


        lifecycleScope.launch {
            try {
                Toast.makeText(this@MyOrderActivity, "正在加载订单...", Toast.LENGTH_SHORT).show()

                val response = ApiService.getInstance().getOrders()
                Log.d("response", "loadOrdersFromServer: "+response)
                if (response.isSuccessful) {
                    val strapiResponse = response.body()
                    val orders = strapiResponse?.data ?: emptyList()
                    Log.d("strapiResponse", "loadOrdersFromServer: "+strapiResponse)
                    // 清空现有订单列表
                    orderList.clear()

                    // 添加服务器返回的订单
                    orders.forEach { order ->
                        orderList.add(OrderInfo(
                            orderId = order.id.toString(),
                            orderName = order.goodsInfo,
                            trackingNumber = order.orderNumber,
                            orderStatus = order.status,
                            createTime = order.createdAt
                        ))
                    }
//                    Log.d("strapiResponse", "loadOrdersFromServer: "+orderList)
                    // 刷新UI
                    refreshOrderListUI()
                    Toast.makeText(this@MyOrderActivity, "加载成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MyOrderActivity, "服务器错误：${response.code()}", Toast.LENGTH_SHORT).show()
                    loadDefaultOrders()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyOrderActivity, "网络连接失败：${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                loadDefaultOrders()
            }
        }



    }

    /**
     * 重置标签样式
     */
    private fun resetTabs() {
        val grayColor = ContextCompat.getColor(this, R.color.gray_400)
        listOf(tvAll, tvUnpaid, tvUnshipped, tvCompleted).forEach { tab ->
            tab.setTextColor(grayColor)
            tab.setBackgroundResource(0)
        }
    }

    /**
     * 获取当前时间
     */
    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        return sdf.format(Date())
    }

    /**
     * 订单数据模型（本地使用）
     */
    private data class OrderInfo(
        val orderId: String,
        val orderName: String,
        val trackingNumber: String,
        val orderStatus: String,
        val createTime: String
    )
}