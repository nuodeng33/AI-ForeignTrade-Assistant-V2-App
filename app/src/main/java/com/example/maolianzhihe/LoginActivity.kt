package com.example.maolianzhihe

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.maolianzhihe.data.local.SessionManager
import com.example.maolianzhihe.ui.UiState
import com.example.maolianzhihe.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private var isPwdVisible = false
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        checkIfLoggedIn()

        etUsername = findViewById(R.id.et_account)
        etPassword = findViewById(R.id.et_password)
        val ivPwdEye = findViewById<ImageView>(R.id.iv_pwd_eye)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvGoRegister = findViewById<TextView>(R.id.tv_register)

        observeLoginState(btnLogin)

        ivPwdEye.setOnClickListener {
            isPwdVisible = !isPwdVisible
            etPassword.inputType = if (isPwdVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            ivPwdEye.setImageResource(
                if (isPwdVisible) android.R.drawable.ic_menu_edit else android.R.drawable.ic_menu_view
            )
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val identifier = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名/邮箱和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginViewModel.login(identifier, password)
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeLoginState(btnLogin: Button) {
        loginViewModel.loginState.observe(this) { state ->
            when (state) {
                UiState.Idle -> btnLogin.isEnabled = true
                UiState.Loading -> {
                    btnLogin.isEnabled = false
                    Toast.makeText(this, "正在登录...", Toast.LENGTH_SHORT).show()
                }
                is UiState.Success -> {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ServiceActivity::class.java))
                    finish()
                }
                is UiState.Empty -> {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "登录失败：${state.message}", Toast.LENGTH_SHORT).show()
                }
                is UiState.Error -> {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkIfLoggedIn() {
        if (SessionManager.isLoggedIn()) {
            startActivity(Intent(this, ServiceActivity::class.java))
            finish()
        }
    }
}
