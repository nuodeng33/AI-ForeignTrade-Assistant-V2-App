package com.example.maolianzhihe

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
        val bnvMain = findViewById<LinearLayout>(R.id.bnv_main) ?: return
        bnvMain.setBackgroundColor(resources.getColor(R.color.white, theme))
        resetBottomNavStyle()
        setSelectedNavItem(selectId)
        setNavItemClickListener()
    }

    /**
     * 重置底部导航所有项为默认样式（灰色图标+灰色文字）
     */
    private fun resetBottomNavStyle() {
        navItems().forEach { item ->
            item.container?.isSelected = false
            item.icon?.apply {
                setImageResource(item.defaultIconRes)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            item.label?.apply {
                setTextColor(resources.getColor(R.color.gray_600, theme))
                typeface = android.graphics.Typeface.DEFAULT
            }
        }
    }

    /**
     * 设置底部导航选中项的样式（蓝色图标+蓝色文字）
     * @param selectId 选中项的ID
     */
    private fun setSelectedNavItem(selectId: Int) {
        navItems().firstOrNull { it.containerId == selectId }?.let { item ->
            item.container?.isSelected = true
            item.icon?.apply {
                setImageResource(item.selectedIconRes)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            item.label?.apply {
                setTextColor(resources.getColor(R.color.blue_500, theme))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
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

    private fun navItems(): List<NavItem> {
        return listOf(
            NavItem(
                containerId = R.id.nav_service,
                container = findViewById(R.id.nav_service),
                icon = findViewById(R.id.iv_service),
                label = findViewById(R.id.tv_nav_text1),
                defaultIconRes = R.drawable.bg9,
                selectedIconRes = R.drawable.nav_service_selected
            ),
            NavItem(
                containerId = R.id.nav_customer,
                container = findViewById(R.id.nav_customer),
                icon = findViewById(R.id.iv_customer_service),
                label = findViewById(R.id.tv_nav_text2),
                defaultIconRes = R.drawable.bg16,
                selectedIconRes = R.drawable.nav_customer_selected
            ),
            NavItem(
                containerId = R.id.nav_contact,
                container = findViewById(R.id.nav_contact),
                icon = findViewById(R.id.iv_contact),
                label = findViewById(R.id.tv_nav_text3),
                defaultIconRes = R.drawable.bg11,
                selectedIconRes = R.drawable.nav_contact_selected
            ),
            NavItem(
                containerId = R.id.nav_mine,
                container = findViewById(R.id.nav_mine),
                icon = findViewById(R.id.iv_mine),
                label = findViewById(R.id.tv_nav_text4),
                defaultIconRes = R.drawable.bg12,
                selectedIconRes = R.drawable.nav_mine_selected
            )
        )
    }

    private data class NavItem(
        val containerId: Int,
        val container: LinearLayout?,
        val icon: ImageView?,
        val label: TextView?,
        val defaultIconRes: Int,
        val selectedIconRes: Int
    )
}
