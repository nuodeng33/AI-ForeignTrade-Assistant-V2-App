package com.example.maolianzhihe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.maolianzhihe.data.local.SessionManager

class SettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initTitleBar("设置", showBack = true)
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_logout)?.setOnClickListener { showLogoutConfirmDialog() }
    }

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确认退出当前账号吗？")
            .setPositiveButton("确认") { _, _ -> logoutAndJumpToLogin() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun logoutAndJumpToLogin() {
        try {
            SessionManager.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "退出失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
