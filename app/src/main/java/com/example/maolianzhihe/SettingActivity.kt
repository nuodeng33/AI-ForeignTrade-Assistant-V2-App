package com.example.maolianzhihe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.maolianzhihe.data.local.SettingsManager
import com.example.maolianzhihe.data.local.SessionManager

class SettingActivity : BaseActivity() {
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 200
    }

    private lateinit var rowNotificationMode: LinearLayout
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var tvNotificationCurrent: TextView
    private lateinit var rowLanguage: LinearLayout
    private lateinit var tvLanguageCurrent: TextView
    private var pendingNotificationMode: String? = null
    private var bindingNotificationSwitch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initTitleBar(getString(R.string.setting), showBack = true)
        bindViews()
        bindSettingActions()
        updateLanguageSummary()
        synchronizeNotificationPermissionState()
        updateNotificationSummary()
    }

    override fun onResume() {
        super.onResume()
        if (::switchNotifications.isInitialized) {
            synchronizeNotificationPermissionState()
            updateNotificationSummary()
        }
    }

    private fun bindViews() {
        rowNotificationMode = findViewById(R.id.row_notification_mode)
        switchNotifications = findViewById(R.id.switch_notifications)
        tvNotificationCurrent = findViewById(R.id.tv_notification_current)
        rowLanguage = findViewById(R.id.row_language)
        tvLanguageCurrent = findViewById(R.id.tv_language_current)
    }

    private fun bindSettingActions() {
        findViewById<ImageView>(R.id.iv_back)?.setOnClickListener { finish() }
        rowLanguage.setOnClickListener { showLanguageDialog() }
        rowNotificationMode.setOnClickListener { showNotificationModeDialog() }
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (bindingNotificationSwitch) return@setOnCheckedChangeListener
            if (isChecked) {
                applyNotificationMode(SettingsManager.NOTIFICATION_ALL)
            } else {
                applyNotificationMode(SettingsManager.NOTIFICATION_OFF)
            }
        }
        findViewById<TextView>(R.id.tv_logout)?.setOnClickListener { showLogoutConfirmDialog() }
    }

    private fun showLanguageDialog() {
        val languageModes = arrayOf(
            SettingsManager.LANGUAGE_SYSTEM,
            SettingsManager.LANGUAGE_ZH,
            SettingsManager.LANGUAGE_EN
        )
        val labels = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_cn),
            getString(R.string.language_en)
        )
        val checkedIndex = languageModes.indexOf(SettingsManager.languageMode()).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                val selectedMode = languageModes[which]
                SettingsManager.setLanguageMode(selectedMode)
                AppCompatDelegate.setApplicationLocales(SettingsManager.localeListFor(selectedMode))
                dialog.dismiss()
                Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show()
                recreate()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateLanguageSummary() {
        tvLanguageCurrent.text = languageLabel(SettingsManager.languageMode())
    }

    private fun languageLabel(mode: String): String {
        return when (mode) {
            SettingsManager.LANGUAGE_ZH -> getString(R.string.language_cn)
            SettingsManager.LANGUAGE_EN -> getString(R.string.language_en)
            else -> getString(R.string.language_system)
        }
    }

    private fun showNotificationModeDialog() {
        val modes = notificationModes()
        val labels = modes.map { notificationModeLabel(it) }.toTypedArray()
        val checkedIndex = modes.indexOf(SettingsManager.notificationMode()).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this)
            .setTitle(R.string.notification_dialog_title)
            .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                dialog.dismiss()
                applyNotificationMode(modes[which])
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun applyNotificationMode(mode: String) {
        if (mode != SettingsManager.NOTIFICATION_OFF && !hasNotificationPermission()) {
            pendingNotificationMode = mode
            requestNotificationPermission()
            return
        }

        SettingsManager.setNotificationMode(mode)
        updateNotificationSummary()
        Toast.makeText(this, R.string.notification_mode_updated, Toast.LENGTH_SHORT).show()
    }

    private fun synchronizeNotificationPermissionState() {
        if (!hasNotificationPermission() &&
            SettingsManager.notificationMode() != SettingsManager.NOTIFICATION_OFF
        ) {
            SettingsManager.setNotificationMode(SettingsManager.NOTIFICATION_OFF)
        }
    }

    private fun updateNotificationSummary() {
        val mode = SettingsManager.notificationMode()
        tvNotificationCurrent.text = notificationModeLabel(mode)
        bindingNotificationSwitch = true
        switchNotifications.isChecked = mode != SettingsManager.NOTIFICATION_OFF
        bindingNotificationSwitch = false
    }

    private fun notificationModes(): Array<String> {
        return arrayOf(
            SettingsManager.NOTIFICATION_ALL,
            SettingsManager.NOTIFICATION_ORDERS,
            SettingsManager.NOTIFICATION_SILENT,
            SettingsManager.NOTIFICATION_OFF
        )
    }

    private fun notificationModeLabel(mode: String): String {
        return when (mode) {
            SettingsManager.NOTIFICATION_ORDERS -> getString(R.string.notification_mode_orders)
            SettingsManager.NOTIFICATION_SILENT -> getString(R.string.notification_mode_silent)
            SettingsManager.NOTIFICATION_OFF -> getString(R.string.notification_mode_off)
            else -> getString(R.string.notification_mode_all)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
            Toast.makeText(this, R.string.notification_permission_needed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != NOTIFICATION_PERMISSION_REQUEST_CODE) return

        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (granted) {
            SettingsManager.setNotificationMode(pendingNotificationMode ?: SettingsManager.NOTIFICATION_ALL)
            Toast.makeText(this, R.string.notification_mode_updated, Toast.LENGTH_SHORT).show()
        } else {
            SettingsManager.setNotificationMode(SettingsManager.NOTIFICATION_OFF)
            Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_SHORT).show()
        }
        pendingNotificationMode = null
        updateNotificationSummary()
    }

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_tip)
            .setPositiveButton(R.string.confirm) { _, _ -> logoutAndJumpToLogin() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun logoutAndJumpToLogin() {
        try {
            SessionManager.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.logout_failed, e.message), Toast.LENGTH_SHORT).show()
        }
    }
}
