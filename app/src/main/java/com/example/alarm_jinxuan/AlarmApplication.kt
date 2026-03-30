package com.example.alarm_jinxuan

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import com.example.alarm_jinxuan.utils.AlarmNotificationUtils

class AlarmApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 应用启动时立即创建所有通知通道
        createAllNotificationChannels()
    }

    private fun createAllNotificationChannels() {
        // 闹钟通知通道
        AlarmNotificationUtils.createNotificationChannel(
            this,
            "闹钟",
            "ALARM_channelId",
            NotificationManager.IMPORTANCE_HIGH
        )

        // 计时器通知通道
        AlarmNotificationUtils.createNotificationChannel(
            this,
            "计时器",
            "ALARM_TIMER",
            NotificationManager.IMPORTANCE_LOW
        )

        // 秒表通知通道
        AlarmNotificationUtils.createNotificationChannel(
            this,
            "秒表",
            "stopwatch_channel",
            NotificationManager.IMPORTANCE_LOW
        )
    }
}
