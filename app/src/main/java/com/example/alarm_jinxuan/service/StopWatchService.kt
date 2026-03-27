package com.example.alarm_jinxuan.service

import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.alarm_jinxuan.utils.AlarmNotificationUtils
import com.example.alarm_jinxuan.utils.StopWatchManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StopWatchService : LifecycleService() {
    private val channelId = "stopwatch_channel"

//    override fun onCreate() {
//        super.onCreate()
//        // 监听通知状态变化，自动更新通知
//        lifecycleScope.launch {
//            StopWatchManager.notificationState.collect {
//                // 只在服务运行时更新通知（使用 @Suppress 注解忽略未使用参数的警告）
//                @Suppress("UNUSED_PARAMETER")
//                updateNotification()
//            }
//        }
//    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        // 表示开始启动
        if (action != null) {
            // 发送给viewmodel执行相关逻辑
            StopWatchManager.sendCommand(action)
            // 以下service的操作主要是为了更改通知按钮
            when (action) {
                "ACTION_START" -> {
                    // 延迟等待ViewModel更新状态，再创建通知
                    lifecycleScope.launch {
                        delay(1000) // 等待100ms让ViewModel更新状态
                        createNotification()
                    }
                }

                "ACTION_RESET" -> {
                    // 销毁通知
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                "ACTION_TOGGLE", "ACTION_LAP" -> {
                    // 通知会通过监听状态自动更新，不需要额外处理
                    updateNotification()
                }

                else -> return START_NOT_STICKY
            }
        }
        // 不需要重启
        return START_NOT_STICKY
    }

    private fun createNotification() {
        // 创建秒表频道
        AlarmNotificationUtils.createNotificationChannel(
            this,
            "秒表",
            channelId,
            NotificationManager.IMPORTANCE_LOW
        )
        // 创建通知栏通知
        val notification = AlarmNotificationUtils.getStopWatchBuilder(
            this, channelId,
            StopWatchManager.notificationState.value
        )

        startForeground(1, notification.build())
    }

    private fun updateNotification() {
        val builder = AlarmNotificationUtils.getStopWatchBuilder(
            this@StopWatchService,
            channelId,
            StopWatchManager.notificationState.value
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(3999, builder.build())
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return TODO("Provide the return value")
    }
}