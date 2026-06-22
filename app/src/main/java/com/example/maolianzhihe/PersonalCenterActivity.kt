package com.example.maolianzhihe

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.maolianzhihe.data.local.SessionManager
import com.example.maolianzhihe.model.ChartPoint
import com.example.maolianzhihe.model.ProfileSummary
import com.example.maolianzhihe.ui.UiState
import com.example.maolianzhihe.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.FileInputStream
import java.io.FileNotFoundException

class PersonalCenterActivity : BaseActivity() {

    // 底部导航ID常量
    companion object {
        private val NAV_SERVICE = R.id.nav_service
        private val NAV_CUSTOMER = R.id.nav_customer
        private val NAV_CONTACT = R.id.nav_contact
        private val NAV_MINE = R.id.nav_mine

        // 相册选择请求码
        private const val GALLERY_REQUEST_CODE = 101
    }

    // 核心控件
    private lateinit var ivAvatar: ImageView
    private lateinit var tvMonthConsultCount: TextView
    private lateinit var tvOngoingOrderCount: TextView
    private lateinit var barChartConsultType: BarChart
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_center)
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val usernameText: TextView = findViewById(R.id.user_name)
        usernameText.text = SessionManager.username()

        try {
            // 初始化标题栏
            initTitleBar(getString(R.string.title_mine), showSetting = true)
            // 初始化底部导航
            initBottomNav(NAV_MINE)

            // 绑定所有控件
            bindViews()

            // 绑定头像点击事件
            bindAvatarClick()

            // 绑定设置按钮跳转
            bindSettingClick()

            // 绑定个人中心项点击事件
            bindPersonalCenterItemClicks()

            // 绑定底部导航点击事件
            bindBottomNavClicks()

            observeProfileSummaryState()

        } catch (e: Exception) {
            Toast.makeText(this, "个人中心初始化失败：${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::profileViewModel.isInitialized) {
            fetchAndUpdatePersonalData()
        }
    }

    /**
     * 绑定所有UI控件
     */
    private fun bindViews() {
        // 头像控件
        ivAvatar = findViewById(R.id.iv_avatar)
        // 统计数据控件
        tvMonthConsultCount = findViewById(R.id.tv_month_consult_count)
        tvOngoingOrderCount = findViewById(R.id.tv_ongoing_order_count)
        // 图表控件
        barChartConsultType = findViewById(R.id.bar_chart_consult_type)
    }

    /**
     * 绑定头像点击事件
     */
    private fun bindAvatarClick() {
        ivAvatar.setOnClickListener {
            openGallery()
        }
    }

    /**
     * 绑定设置按钮点击事件
     */
    private fun bindSettingClick() {
        val ivSetting = findViewById<ImageView>(R.id.iv_setting)
        ivSetting?.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            intent.putExtra("FROM_PAGE_ID", NAV_MINE)
            startActivitySafely(intent, "设置页")
        }
    }

    private fun fetchAndUpdatePersonalData() {
        profileViewModel.loadProfileSummary()
    }

    private fun observeProfileSummaryState() {
        profileViewModel.profileSummaryState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> showProfileLoading()
                is UiState.Success -> updatePersonalDataUI(state.data)
                is UiState.Empty -> showProfileEmpty(state.message)
                is UiState.Error -> showProfileError(state.message)
            }
        }
    }

    private fun showProfileLoading() {
        tvMonthConsultCount.text = "--"
        tvOngoingOrderCount.text = "--"
        barChartConsultType.clear()
        barChartConsultType.setNoDataText("正在加载订单统计...")
    }

    private fun showProfileEmpty(message: String) {
        tvMonthConsultCount.text = "0"
        tvOngoingOrderCount.text = "0"
        barChartConsultType.clear()
        barChartConsultType.setNoDataText(message)
    }

    private fun showProfileError(message: String) {
        tvMonthConsultCount.text = "0"
        tvOngoingOrderCount.text = "0"
        barChartConsultType.clear()
        barChartConsultType.setNoDataText("暂无订单统计")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updatePersonalDataUI(data: ProfileSummary) {
        tvMonthConsultCount.text = data.monthOrderCount.toString()
        tvOngoingOrderCount.text = data.ongoingOrderCount.toString()
        updateOrderStatusChart(data.orderStatusData)
    }

    private fun updateOrderStatusChart(orderStatusData: List<ChartPoint>) {
        if (orderStatusData.isEmpty()) {
            barChartConsultType.clear()
            barChartConsultType.setNoDataText("暂无订单统计")
            return
        }

        // 1. 准备图表数据
        val entries = mutableListOf<BarEntry>()
        val xAxisLabels = mutableListOf<String>()

        orderStatusData.forEachIndexed { index, item ->
            entries.add(BarEntry(index.toFloat(), item.value.toFloat()))
            xAxisLabels.add(item.label)
        }

        // 2. 配置数据集
        val dataSet = BarDataSet(entries, "订单数量")
        // 设置柱状图颜色
        dataSet.color = ContextCompat.getColor(this, R.color.blue_500)
        // 设置数值文字颜色
        dataSet.valueTextColor = ContextCompat.getColor(this, R.color.black)
        // 设置数值文字大小
        dataSet.valueTextSize = 10f

        // 3. 配置图表
        barChartConsultType.apply {
            // 关闭描述文字
            description.isEnabled = false
            // 关闭图例
            legend.isEnabled = false
            // 禁用缩放
            setScaleEnabled(false)
            // 禁用拖拽
            setDragEnabled(false)

            // 配置X轴
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                setDrawGridLines(false)
                granularity = 1f
                textSize = 10f
            }

            // 配置左侧Y轴
            axisLeft.apply {
                setDrawGridLines(true)
                textSize = 10f
                // 设置Y轴最小值为0
                axisMinimum = 0f
            }

            // 隐藏右侧Y轴
            axisRight.isEnabled = false

            // 设置图表数据
            data = BarData(dataSet)
            // 刷新图表
            invalidate()
        }
    }

    /**
     * 打开系统相册
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    /**
     * 相册选择结果回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageUri?.let {
                try {
                    val inputStream: FileInputStream = contentResolver.openInputStream(it) as FileInputStream
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    ivAvatar.setImageBitmap(bitmap)
                    Toast.makeText(this, "头像已选择", Toast.LENGTH_SHORT).show()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 绑定个人中心核心项点击事件
     */
    private fun bindPersonalCenterItemClicks() {
        // 我的订单
        findViewById<LinearLayout>(R.id.ll_order)?.setOnClickListener {
            startActivitySafely(Intent(this, MyOrderActivity::class.java), "我的订单页")
        }
        // 账单与支付
        findViewById<LinearLayout>(R.id.ll_bill)?.setOnClickListener {
            Toast.makeText(this, "查看账单与支付", Toast.LENGTH_SHORT).show()
        }
        // 我的收藏
        findViewById<LinearLayout>(R.id.ll_collect)?.setOnClickListener {
            Toast.makeText(this, "查看我的收藏", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 绑定底部导航点击事件
     */
    private fun bindBottomNavClicks() {
        // 服务页
        findViewById<LinearLayout>(NAV_SERVICE)?.setOnClickListener {
            if (!isCurrentPage(ServiceActivity::class.java)) {
                startActivitySafely(Intent(this, ServiceActivity::class.java), "服务页")
                finish()
            }
        }

        // 客服页
        findViewById<LinearLayout>(NAV_CUSTOMER)?.setOnClickListener {
            if (!isCurrentPage(CustomerServiceActivity::class.java)) {
                startActivitySafely(Intent(this, CustomerServiceActivity::class.java), "客服页")
                finish()
            }
        }

        // 联系页
        findViewById<LinearLayout>(NAV_CONTACT)?.setOnClickListener {
            if (!isCurrentPage(ContactActivity::class.java)) {
                startActivitySafely(Intent(this, ContactActivity::class.java), "联系页")
                finish()
            }
        }

        // 我的页（当前页）- 点击刷新数据
        findViewById<LinearLayout>(NAV_MINE)?.setOnClickListener {
            Toast.makeText(this, "当前已是个人中心页面，正在刷新数据...", Toast.LENGTH_SHORT).show()
            fetchAndUpdatePersonalData()
        }
    }

    /**
     * 安全跳转封装
     */
    private fun startActivitySafely(intent: Intent, pageName: String) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "跳转${pageName}失败：${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * 判断当前是否已是目标页面
     */
    private fun <T> isCurrentPage(clazz: Class<T>): Boolean {
        return this::class.java == clazz
    }

    /**
     * 刷新个人中心数据（兼容原有调用）
     */
    private fun refreshPersonalData() {
        fetchAndUpdatePersonalData()
        Toast.makeText(this, "个人中心数据已刷新", Toast.LENGTH_SHORT).show()
    }
}
