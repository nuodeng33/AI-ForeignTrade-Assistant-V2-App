package com.example.maolianzhihe

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.FileInputStream
import java.io.FileNotFoundException

// 数据模型：存储个人中心统计数据
data class PersonalData(
    val monthConsultCount: Int, // 本月咨询数
    val ongoingOrderCount: Int, // 进行中订单数
    val consultTypeData: List<Pair<String, Int>> // 咨询类型统计（类型/日期, 数量）
)

class PersonalCenterActivity : BaseActivity() {

    // 底部导航ID常量
    companion object {
        private val NAV_SERVICE = R.id.nav_service
        private val NAV_CUSTOMER = R.id.nav_customer
        private val NAV_CONTACT = R.id.nav_contact
        private val NAV_MINE = R.id.nav_mine

        // 权限请求码
        private const val PERMISSION_REQUEST_CODE = 100
        // 相册选择请求码
        private const val GALLERY_REQUEST_CODE = 101
    }

    // 核心控件
    private lateinit var ivAvatar: ImageView
    private lateinit var tvMonthConsultCount: TextView
    private lateinit var tvOngoingOrderCount: TextView
    private lateinit var barChartConsultType: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_center)
        initBottomNav(R.id.tv_nav_text4)

        val spf:SharedPreferences = getSharedPreferences("user_info",MODE_PRIVATE)
        val username : String  = spf.getString("username","unknown")!!
        val usernameTetx : TextView = findViewById(R.id.user_name)
        usernameTetx.text = username

        try {
            // 初始化标题栏
            initTitleBar("个人中心", showSetting = true)
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

            // 初始化并更新数据
            fetchAndUpdatePersonalData()

        } catch (e: Exception) {
            Toast.makeText(this, "个人中心初始化失败：${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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
            checkPermissionAndOpenGallery()
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

    /**
     * 请求并更新个人中心数据（模拟接口请求，实际替换为真实接口）
     */
    private fun fetchAndUpdatePersonalData() {
        // 模拟从服务器获取数据（实际项目替换为Retrofit/OkHttp请求）
        val mockData = PersonalData(
            monthConsultCount = 35, // 模拟本月咨询数
            ongoingOrderCount = 7,  // 模拟进行中订单数
            consultTypeData = listOf( // 模拟咨询类型统计数据
                "邮件" to 12, "电话" to 18, "在线" to 25, "上门" to 8,
                "微信" to 20, "短信" to 5
            )
        )

        // 更新UI显示
        updatePersonalDataUI(mockData)
    }

    /**
     * 根据数据更新UI
     */
    private fun updatePersonalDataUI(data: PersonalData) {
        // 更新本月咨询数
        tvMonthConsultCount.text = data.monthConsultCount.toString()
        // 更新进行中订单数
        tvOngoingOrderCount.text = data.ongoingOrderCount.toString()
        // 更新咨询类型统计图
        updateConsultTypeChart(data.consultTypeData)
    }

    /**
     * 更新咨询类型柱状图
     */
    private fun updateConsultTypeChart(consultData: List<Pair<String, Int>>) {
        // 1. 准备图表数据
        val entries = mutableListOf<BarEntry>()
        val xAxisLabels = mutableListOf<String>()

        consultData.forEachIndexed { index, (type, count) ->
            entries.add(BarEntry(index.toFloat(), count.toFloat()))
            xAxisLabels.add(type)
        }

        // 2. 配置数据集
        val dataSet = BarDataSet(entries, "咨询数量")
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
     * 检查权限并打开相册
     */
    private fun checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            openGallery()
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
     * 权限申请结果回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show()
            }
        }
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