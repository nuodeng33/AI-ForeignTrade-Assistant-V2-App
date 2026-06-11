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
import com.example.maolianzhihe.ui.UiState
import com.example.maolianzhihe.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {
    private var isPwdVisible = false
    private var isPwdConfirmVisible = false
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPwdConfirm: EditText
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerViewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        etUsername = findViewById(R.id.et_account)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etPwdConfirm = findViewById(R.id.et_pwd_confirm)
        val ivPwdEye = findViewById<ImageView>(R.id.iv_pwd_eye)
        val ivPwdConfirmEye = findViewById<ImageView>(R.id.iv_pwd_confirm_eye)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvLogin = findViewById<TextView>(R.id.tv_login)

        observeRegisterState(btnRegister)

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

        ivPwdConfirmEye.setOnClickListener {
            isPwdConfirmVisible = !isPwdConfirmVisible
            etPwdConfirm.inputType = if (isPwdConfirmVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            ivPwdConfirmEye.setImageResource(
                if (isPwdConfirmVisible) android.R.drawable.ic_menu_edit else android.R.drawable.ic_menu_view
            )
            etPwdConfirm.setSelection(etPwdConfirm.text.length)
        }

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val passwordConfirm = etPwdConfirm.text.toString().trim()

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

            registerViewModel.register(username, email, password)
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeRegisterState(btnRegister: Button) {
        registerViewModel.registerState.observe(this) { state ->
            when (state) {
                UiState.Idle -> btnRegister.isEnabled = true
                UiState.Loading -> {
                    btnRegister.isEnabled = false
                    Toast.makeText(this, "正在注册...", Toast.LENGTH_SHORT).show()
                }
                is UiState.Success -> {
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ServiceActivity::class.java))
                    finish()
                }
                is UiState.Empty -> {
                    btnRegister.isEnabled = true
                    Toast.makeText(this, "注册失败：${state.message}", Toast.LENGTH_SHORT).show()
                }
                is UiState.Error -> {
                    btnRegister.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
