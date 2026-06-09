package com.example.maolianzhihe

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

class ContactActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        initBottomNav(R.id.tv_nav_text3)

        try {
            initTitleBar("联系我们", showSetting = true)
            initBottomNav(R.id.nav_contact)

            // 修复：FROM_PAGE_ID 改为当前页的 nav_contact（原错误为 nav_customer）
            val ivSetting = findViewById<ImageView>(R.id.iv_setting)
            ivSetting?.setOnClickListener {
                val intent = Intent(this, SettingActivity::class.java)
                intent.putExtra("FROM_PAGE_ID", R.id.nav_contact)
                startActivity(intent)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "联系页面初始化失败：${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}