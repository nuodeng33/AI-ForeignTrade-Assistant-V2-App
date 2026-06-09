package com.example.maolianzhihe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.maolianzhihe.model.LoginRequest
import com.example.maolianzhihe.network.ApiService
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private var isPwdVisible = false
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 检查是否已登录
        checkIfLoggedIn()

        // 绑定控件
        etUsername = findViewById(R.id.et_account)
        etPassword = findViewById(R.id.et_password)
        val ivPwdEye = findViewById<ImageView>(R.id.iv_pwd_eye)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvGoRegister = findViewById<TextView>(R.id.tv_register)

        // 密码显隐切换
        ivPwdEye.setOnClickListener {
            isPwdVisible = !isPwdVisible
            etPassword.inputType = if (isPwdVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            ivPwdEye.setImageResource(if (isPwdVisible)
                android.R.drawable.ic_menu_edit
            else
                android.R.drawable.ic_menu_view
            )
            etPassword.setSelection(etPassword.text.length)
        }

        // 登录按钮点击
        btnLogin.setOnClickListener {
            val identifier = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 基础输入校验
            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名/邮箱和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "正在登录...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                try {
                    // 正确调用方式：直接使用 ApiService.getInstance()
                    val response = ApiService.getInstance().login(
                        LoginRequest(identifier = identifier, password = password)
                    )

                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            // 保存用户信息
                            saveUserInfo(authResponse)

                            Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, ServiceActivity::class.java))

                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "登录失败：服务器返回空数据", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        when (response.code()) {
                            400 -> Toast.makeText(this@LoginActivity, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(this@LoginActivity, "登录失败，错误码：${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "网络连接失败：${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }

        // 注册跳转
        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * 检查是否已登录
     */
    private fun checkIfLoggedIn() {
        val sp: SharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        val isLoggedIn = sp.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // 已登录，直接跳转到主页面
            startActivity(Intent(this, ServiceActivity::class.java))
            finish()
        }
    }

    /**
     * 保存用户信息到SharedPreferences
     */
    private fun saveUserInfo(authResponse: com.example.maolianzhihe.model.AuthResponse) {
        val sp: SharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        sp.edit()
            .putString("token", authResponse.jwt)
            .putInt("userId", authResponse.user.id)
            .putString("username", authResponse.user.username)
            .putString("email", authResponse.user.email ?: "")
            .putBoolean("isLoggedIn", true)
            .apply()
    }
}