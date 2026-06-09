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
import com.example.maolianzhihe.model.RegisterRequest
import com.example.maolianzhihe.network.ApiService
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private var isPwdVisible = false
    private var isPwdConfirmVisible = false
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPwdConfirm: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 绑定控件
        etUsername = findViewById(R.id.et_account)
        etEmail = findViewById(R.id.et_email)  // 需要在布局中添加这个id
        etPassword = findViewById(R.id.et_password)
        etPwdConfirm = findViewById(R.id.et_pwd_confirm)
        val ivPwdEye = findViewById<ImageView>(R.id.iv_pwd_eye)
        val ivPwdConfirmEye = findViewById<ImageView>(R.id.iv_pwd_confirm_eye)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvLogin = findViewById<TextView>(R.id.tv_login)

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

        // 确认密码显隐切换
        ivPwdConfirmEye.setOnClickListener {
            isPwdConfirmVisible = !isPwdConfirmVisible
            etPwdConfirm.inputType = if (isPwdConfirmVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            ivPwdConfirmEye.setImageResource(if (isPwdConfirmVisible)
                android.R.drawable.ic_menu_edit
            else
                android.R.drawable.ic_menu_view
            )
            etPwdConfirm.setSelection(etPwdConfirm.text.length)
        }

        // 注册按钮点击
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val passwordConfirm = etPwdConfirm.text.toString().trim()

            // 输入校验
            if (username.isEmpty()) {
                Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "请输入邮箱", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 6) {
                Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != passwordConfirm) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "正在注册...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                try {
                    // 正确调用方式：直接使用 ApiService.getInstance()
                    val response = ApiService.getInstance().register(
                        RegisterRequest(
                            username = username,
                            email = email,
                            password = password
                        )
                    )

                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse != null) {
                            // 保存用户信息
                            saveUserInfo(authResponse)

                            Toast.makeText(this@RegisterActivity, "注册成功！", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegisterActivity, ServiceActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, "注册失败：服务器返回空数据", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        when (response.code()) {
                            400 -> Toast.makeText(this@RegisterActivity, "用户名或邮箱已被注册", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(this@RegisterActivity, "注册失败，错误码：${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "网络连接失败：${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }

        // 跳转登录页
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * 保存用户信息
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