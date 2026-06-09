package com.example.maolianzhihe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class SettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // 初始化标题栏（显示返回按钮）
        initTitleBar("设置", showBack = true)

        // 返回按钮点击（复用BaseActivity的默认逻辑，无需重复绑定）
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener {
            finish()
        }

        // 退出登录按钮（核心：跳转到登录页）
        findViewById<TextView>(R.id.tv_logout)?.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    /**
     * 显示退出确认弹窗
     */
    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出登录") // 标题改为“退出登录”更贴合逻辑
            .setMessage("确认退出当前账号吗？") // 提示语调整
            .setPositiveButton("确认") { _, _ ->
                logoutAndJumpToLogin() // 替换为跳登录页的方法
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 退出登录并跳转到登录页（核心修改）
     */
    private fun logoutAndJumpToLogin() {
        try {
            // 1. 清除用户登录信息（统一清理登录时保存的所有信息）
            clearUserSession()

            // 2. 跳转到登录页，并设置FLAG清除所有返回栈
            val intent = Intent(this, LoginActivity::class.java)
            // 关键：清除当前应用所有Activity的返回栈，避免从登录页返回至其他页面
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // 3. 关闭当前页面（可选，FLAG已确保栈清空）
            finish()

            Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "退出失败：${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * 清除用户登录会话（同步LoginActivity保存的key）
     */
    private fun clearUserSession() {
        // 注意：key要和LoginActivity中保存的一致（user_info是登录时的SP名称）
        val sp = getSharedPreferences("user_info", Context.MODE_PRIVATE)
        sp.edit()
            .clear() // 清空所有登录相关信息（token/userId/userName）
            .apply()
    }
}