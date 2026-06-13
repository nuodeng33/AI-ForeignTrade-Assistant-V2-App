package com.example.maolianzhihe

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.maolianzhihe.model.OrderRequest
import com.example.maolianzhihe.model.OrderRequestData
import com.example.maolianzhihe.ui.UiState
import com.example.maolianzhihe.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class CreateOrderActivity : BaseActivity() {
    private lateinit var tvOrderTime: TextView
    private lateinit var etGoodsInfo: EditText
    private lateinit var etOrderNo: EditText
    private lateinit var orderViewModel: OrderViewModel
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)

        orderViewModel = ViewModelProvider(this)[OrderViewModel::class.java]

        try {
            initTitleBar("创建订单", showBack = true)
            initBottomNav(R.id.nav_mine)
            bindViews()
            observeCreateOrderState()

            findViewById<ImageView>(R.id.iv_back)?.setOnClickListener { finish() }
            tvOrderTime.setOnClickListener { showDatePicker() }
            findViewById<TextView>(R.id.tv_submit_order)?.setOnClickListener { submitOrder() }
            generateOrderNo()
        } catch (e: Exception) {
            Toast.makeText(this, "创建订单页面初始化失败：${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun bindViews() {
        tvOrderTime = findViewById(R.id.tv_order_time)
        etGoodsInfo = findViewById(R.id.et_goods_info)
        etOrderNo = findViewById(R.id.et_order_no)
        updateTimeDisplay()
    }

    private fun updateTimeDisplay() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        tvOrderTime.text = sdf.format(calendar.time)
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                updateTimeDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun generateOrderNo() {
        val timestamp = System.currentTimeMillis().toString().substring(5)
        val random = (1000..9999).random()
        etOrderNo.setText("ORD${timestamp}${random}")
    }

    private fun submitOrder() {
        val goodsInfo = etGoodsInfo.text.toString().trim()
        val orderNo = etOrderNo.text.toString().trim()

        if (goodsInfo.isEmpty()) {
            Toast.makeText(this, "请填写物品信息", Toast.LENGTH_SHORT).show()
            return
        }
        if (orderNo.isEmpty()) {
            Toast.makeText(this, "订单号不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val formattedTime = sdf.format(calendar.time)

        val orderRequest = OrderRequest(
            data = OrderRequestData(
                orderNumber = orderNo,
                goodsInfo = goodsInfo,
                status = "待发货",
                createTime = formattedTime
            )
        )

        orderViewModel.createOrder(orderRequest)
    }

    private fun observeCreateOrderState() {
        orderViewModel.createOrderState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> Toast.makeText(this, "正在提交订单...", Toast.LENGTH_SHORT).show()
                is UiState.Success -> {
                    Toast.makeText(this, "订单创建成功！", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is UiState.Empty -> Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                is UiState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
