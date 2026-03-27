package com.example.alarm_jinxuan.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

object PermissionUtils {

    /**
     * 检查是否需要请求权限，如果需要则显示对话框引导用户
     * @return true 表示权限都正常，false 表示缺少权限
     */
    fun checkAndRequestPermissions(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val missingPermissions = mutableListOf<String>()

        // 1. 检查通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            if (!notificationManager.areNotificationsEnabled()) {
                missingPermissions.add("通知权限")
            }
        }

        // 2. 检查闹钟和提醒权限 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                missingPermissions.add("闹钟和提醒权限")
            }
        }

        // 3. 检查电池优化
        if (!isIgnoringBatteryOptimizations(context)) {
            missingPermissions.add("关闭电池优化")
        }

        // 如果所有权限都正常
        if (missingPermissions.isEmpty()) {
            onComplete?.invoke(true)
            return
        }

        // 组装未打开的权限
        val basePermissions = missingPermissions.joinToString("\n") { "• $it" }

        val message = "为了确保闹钟能准时响起，请开启以下权限：\n\n" +
                "$basePermissions\n\n" +
                "--- --- --- --- --- ---\n" +
                "另外，为了提升您的体验，您还需在点击“去设置”后，在跳转到的页面中手动打开“自启动”与“后台活动”开关。"

        // 显示引导对话框
        AlertDialog.Builder(context)
            .setTitle("需要开启权限")
            .setMessage(message)
            .setPositiveButton("去设置") { _, _ ->
                openBatterySettings(context)
                onComplete?.invoke(false)
            }
            .setNegativeButton("稍后") { _, _ ->
                onComplete?.invoke(false)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * 检查是否忽略了电池优化
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }

    /**
     * 跳转到系统“忽略电池优化”设置列表页面
     */
    fun openBatterySettings(context: Context) {
        val intent = Intent().apply {
            // 修改 Action 为跳转到电池优化列表
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // 兜底方案：如果极个别魔改系统找不到该页面，跳转到应用详情页
            val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(detailIntent)
        }
    }

    /**
     * 精准跳转到荣耀/华为的应用耗电详情页
     */
    fun openHonorPowerDetail(context: Context) {
        val intent = Intent()
        val packageName = context.packageName

        // 这里的组件名必须和你 ADB 抓取的一致
        intent.component = ComponentName(
            "com.hihonor.systemmanager",
            "com.hihonor.systemmanager.power.ui.DetailOfSoftConsumptionActivity"
        )

        // 关键：必须传入包名，系统才知道显示哪个 App 的耗电
        intent.putExtra("pkg_name", packageName)
        // 兼容部分旧版本或华为机型可能用的 key
        intent.putExtra("package_name", packageName)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // 如果精准跳转失败（例如被系统拦截），则降级到通用电池页面
            try {
                val batteryIntent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                batteryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(batteryIntent)
            } catch (_: Exception) {
                // 最后的兜底：跳转到应用详情页
                val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(detailIntent)
            }
        }
    }

    /**
     * 检查闹钟权限是否正常（不弹窗，仅检查）
     */
    fun hasAllPermissions(context: Context): Boolean {
        // 检查通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            if (!notificationManager.areNotificationsEnabled()) {
                return false
            }
        }

        // 检查闹钟和提醒权限 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }

        // 检查电池优化
        if (!isIgnoringBatteryOptimizations(context)) {
            return false
        }

        return true
    }
}