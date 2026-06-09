package com.example.maolianzhihe

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.log

open class BaseActivity : AppCompatActivity() {

    /**
     * 初始化标题栏
     * @param title 标题文字
     * @param showBack 是否显示返回按钮（默认不显示）
     * @param backClickListener 返回按钮点击事件（默认关闭当前页）
     * @param showSetting 是否显示设置按钮（默认不显示）
     */
    protected fun initTitleBar(
        title: String,
        showBack: Boolean = false,
        backClickListener: () -> Unit = { finish() },
        showSetting: Boolean = false
    ) {
        // 标题文字设置
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        tvTitle?.text = title

        // 返回按钮：根据showBack显示/隐藏
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        ivBack?.apply {
            visibility = if (showBack) View.VISIBLE else View.GONE
            setOnClickListener { backClickListener() }
        }

        // 设置按钮：根据showBack显示/隐藏
        val ivSetting = findViewById<ImageView>(R.id.iv_setting)
        ivSetting?.apply {
            visibility = if (showSetting) View.VISIBLE else View.GONE
            setOnClickListener {
                // 点击设置按钮跳转到设置页
                startActivity(Intent(this@BaseActivity, SettingActivity::class.java))
            }
        }
    }

    /**
     * 初始化底部导航（适配自定义LinearLayout导航）
     * @param selectId 默认选中的导航项ID（对应nav_service/nav_customer等）
     */
    protected fun initBottomNav(selectId: Int) {
        // 找到底部导航根布局（LinearLayout）
        val bnvMain = findViewById<LinearLayout>(R.id.bnv_main)
        bnvMain?.apply {
            // 1. 根据当前选中项设置导航栏背景色（仅客服页背景蓝）

            // 2. 重置所有导航项样式为默认
            resetBottomNavStyle()
            // 3. 设置默认选中项的样式（变蓝）
            setSelectedNavItem(selectId)
            // 4. 设置导航项点击事件
            setNavItemClickListener()
            setBottomNavBgColor(selectId)
        }
    }

    /**
     * 动态设置底部导航栏背景色（仅客服页背景蓝，其他页面默认白色）
     * @param selectId 当前选中的导航项ID
     */
    private fun setBottomNavBgColor(selectId: Int) {
        val bnvMain = findViewById<LinearLayout>(R.id.bnv_main) ?: return


        val bgColor = if (selectId == R.id.nav_customer) {
            // 仅客服页：导航栏背景蓝
           // resources.getColor(R.color.blue_500, theme)
            resources.getColor(R.color.white, theme)
        }
        else {
            // 其他页面（含个人中心）：导航栏背景默认白色
            resources.getColor(R.color.white, theme)
        }
        bnvMain.setBackgroundColor(bgColor)
    }

    /**
     * 重置底部导航所有项为默认样式（灰色图标+灰色文字）
     */
    private fun resetBottomNavStyle() {
        val bnvMain = findViewById<LinearLayout>(R.id.bnv_main) ?: return

        // 服务项
        findViewById<LinearLayout>(R.id.nav_service)?.apply {
            findViewById<ImageView>(R.id.iv_service)?.setImageResource(R.drawable.bg9)
            findViewById<TextView>(R.id.tv_nav_text1)?.setTextColor(resources.getColor(R.color.gray_600, theme))
        }
        // 客服项
        findViewById< LinearLayout>(R.id.nav_customer)?.apply {
            findViewById<ImageView>(R.id.iv_customer_service)?.setImageResource(R.drawable.bg16)
            findViewById<TextView>(R.id.tv_nav_text2)?.setTextColor(resources.getColor(R.color.gray_600, theme))
        }
        // 联系项
        findViewById<LinearLayout>(R.id.nav_contact)?.apply {
            findViewById<ImageView>(R.id.iv_contact)?.setImageResource(R.drawable.bg11)
            findViewById<TextView>(R.id.tv_nav_text3)?.setTextColor(resources.getColor(R.color.gray_600, theme))
        }
        // 我的项
        findViewById<LinearLayout>(R.id.nav_mine)?.apply {
            findViewById<ImageView>(R.id.iv_mine)?.setImageResource(R.drawable.bg12)
            findViewById<TextView>(R.id.tv_nav_text4)?.setTextColor(resources.getColor(R.color.gray_600, theme))
        }
    }

    /**
     * 设置底部导航选中项的样式（蓝色图标+蓝色文字）
     * @param selectId 选中项的ID
     */
    private fun setSelectedNavItem(selectId: Int) {
        val bnvMain = findViewById<LinearLayout>(R.id.bnv_main) ?: return

        when (selectId) {
            R.id.nav_service -> {
                // 服务页：仅服务项变蓝
                findViewById<LinearLayout>(R.id.nav_service)?.apply {
                    findViewById<ImageView>(R.id.iv_service)?.setImageResource(R.drawable.bg9) // 有选中态图标则替换为bg9_selected
                   // findViewById<TextView>(R.id.tv_nav_text1)?.setTextColor(resources.getColor(R.color.blue_500, theme))
                }
            }
            R.id.nav_customer -> {
                // 客服页：仅客服项变蓝（背景已单独设置为蓝）
                findViewById<LinearLayout>(R.id.nav_customer)?.apply {
                    findViewById<ImageView>(R.id.iv_customer_service)?.setImageResource(R.drawable.bg16) // 有选中态图标则替换为bg16_selected
                    findViewById<TextView>(R.id.tv_nav_text2)?.setTextColor(resources.getColor(R.color.blue_500, theme))
                }
            }
            R.id.nav_contact -> {
                // 联系页：仅联系项变蓝
                findViewById<LinearLayout>(R.id.nav_contact)?.apply {
                    findViewById<ImageView>(R.id.iv_contact)?.setImageResource(R.drawable.bg11) // 有选中态图标则替换为bg11_selected
                    //findViewById<TextView>(R.id.tv_nav_text3)?.setTextColor(resources.getColor(R.color.blue_500, theme))
                }
            }
            R.id.nav_mine -> {
                // 个人中心页：仅“我的”项变蓝，其他项保持灰色
                findViewById<LinearLayout>(R.id.nav_mine)?.apply {
                    findViewById<ImageView>(R.id.iv_mine)?.setImageResource(R.drawable.bg12) // 有选中态图标则替换为bg12_selected
                  //  findViewById<TextView>(R.id.tv_nav_text4)?.setTextColor(resources.getColor(R.color.blue_500, theme))
                }
            }
        }
    }

    /**
     * 设置底部导航项的点击事件（跳转到对应页面）
     */
    private fun setNavItemClickListener() {
        val bnvMain = findViewById<LinearLayout>(R.id.bnv_main) ?: return

        // 服务项点击
        findViewById<LinearLayout>(R.id.nav_service)?.setOnClickListener {
            if (this@BaseActivity !is ServiceActivity) {
                jumpTo(ServiceActivity::class.java)
            }
        }

        // 客服项点击
        findViewById<LinearLayout>(R.id.nav_customer)?.setOnClickListener {
            if (this@BaseActivity !is CustomerServiceActivity) {
                jumpTo(CustomerServiceActivity::class.java)
            }
        }

        // 联系项点击
        findViewById<LinearLayout>(R.id.nav_contact)?.setOnClickListener {
            if (this@BaseActivity !is ContactActivity) {
                jumpTo(ContactActivity::class.java)
            }
        }

        // 我的项点击
        findViewById<LinearLayout>(R.id.nav_mine)?.setOnClickListener {
            if (this@BaseActivity !is PersonalCenterActivity) {
                jumpTo(PersonalCenterActivity::class.java)
            }
        }
    }

    /**
     * 通用页面跳转方法
     * @param clazz 目标Activity的Class
     */
    protected fun jumpTo(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
        finish() // 关闭当前页，避免返回栈堆积
    }
}