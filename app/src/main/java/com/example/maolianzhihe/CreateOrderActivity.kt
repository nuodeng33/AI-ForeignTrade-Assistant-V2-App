package com.example.maolianzhihe

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.maolianzhihe.model.OrderRequest
import com.example.maolianzhihe.model.OrderRequestData
import com.example.maolianzhihe.network.ApiService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateOrderActivity : BaseActivity() {
    private lateinit var tvOrderTime: TextView
    private lateinit var etGoodsInfo: EditText
    private lateinit var etOrderNo: EditText
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)

        try {
            // 初始化标题栏 + 底部导航
            initTitleBar("创建订单", showBack = true)
            initBottomNav(R.id.nav_mine)

            // 绑定控件
            bindViews()

            // 返回按钮点击事件
            findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
                finish()
            }

            // 时间选择器
            tvOrderTime.setOnClickListener {
                showDatePicker()
            }

            // 提交订单按钮
            findViewById<TextView>(R.id.tv_submit_order)?.setOnClickListener {
                submitOrder()
            }

            // 自动生成订单号
            generateOrderNo()

        } catch (e: Exception) {
            Toast.makeText(this, "创建订单页面初始化失败：${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    /**
     * 绑定页面控件
     */
    private fun bindViews() {
        tvOrderTime = findViewById(R.id.tv_order_time)
        etGoodsInfo = findViewById(R.id.et_goods_info)
        etOrderNo = findViewById(R.id.et_order_no)
        // 初始化时间显示为当前时间
        updateTimeDisplay()
    }

    /**
     * 更新时间显示
     */
    private fun updateTimeDisplay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        tvOrderTime.text = sdf.format(calendar.time)
    }

    /**
     * 显示日期选择器
     */
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                updateTimeDisplay()
            },
            year, month, day
        ).show()
    }

    /**
     * 自动生成订单号
     */
    private fun generateOrderNo() {
        val timestamp = System.currentTimeMillis().toString().substring(5)
        val random = (1000..9999).random()
        etOrderNo.setText("ORD${timestamp}${random}")
    }

    /**
     * 提交订单到服务器
     */
    private fun submitOrder() {
        val goodsInfo = etGoodsInfo.text.toString().trim()
        val orderNo = etOrderNo.text.toString().trim()

        // 表单校验
        if (goodsInfo.isEmpty()) {
            Toast.makeText(this, "请填写物品信息", Toast.LENGTH_SHORT).show()
            return
        }
        if (orderNo.isEmpty()) {
            Toast.makeText(this, "订单号不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        // 格式化时间为 ISO 8601 格式
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val formattedTime = sdf.format(calendar.time)

        // 创建 Strapi 格式的请求
        val orderRequest = OrderRequest(
            data = OrderRequestData(
                orderNumber = orderNo,
                goodsInfo = goodsInfo,
                status = "待发货",
                createTime = formattedTime
            )
        )

        // 显示加载提示
        Toast.makeText(this, "正在提交订单...", Toast.LENGTH_SHORT).show()

        // 发起网络请求
        lifecycleScope.launch {
            try {
                val response = ApiService.getInstance().createOrder(orderRequest)

                if (response.isSuccessful) {
                    Toast.makeText(this@CreateOrderActivity, "订单创建成功！", Toast.LENGTH_LONG).show()

                    // 返回MyOrderActivity
                    val intent = Intent(this@CreateOrderActivity, MyOrderActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@CreateOrderActivity, "创建订单失败：${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateOrderActivity, "网络连接失败：${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }}