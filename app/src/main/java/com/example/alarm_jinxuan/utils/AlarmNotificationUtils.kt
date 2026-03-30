package com.example.alarm_jinxuan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.alarm_jinxuan.MainActivity
import com.example.alarm_jinxuan.R
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.receiver.AlarmReceiver
import com.example.alarm_jinxuan.repository.TimerRepository
import com.example.alarm_jinxuan.repository.TimerRepository.formatRemainingTime
import com.example.alarm_jinxuan.view.ring.RingActivity

object AlarmNotificationUtils {
    private var smallIcon: Int = R.drawable.ic_alarm

    const val STOPWATCH_CHANNEL = "stopwatch_channel"

    /**
     * 创建通知通道
     */
    fun createNotificationChannel(
        context: Context,
        name: String,
        channelId: String,
        importance: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                name,
                importance
            ).apply {
                setSound(null, null) // 通知需要静音
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 获取计时前开始执行的通知
     */
    fun getTimerStartNotification(
        context: Context,
        actionPI: PendingIntent,
        actionTitle: String,
        channelId: String
    ): NotificationCompat.Builder {
        // 剩余时间并进行格式化
        val remain = TimerRepository.remainingNanos
        val timeText = formatRemainingTime(remain)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle("计时器")
            .setContentText(timeText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, actionTitle, actionPI)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * 获取计时器超时的通知
     */
    fun getTimerOutNotificationBuilder(
        context: Context,
        dismissPI: PendingIntent,
        channelId: String,
        timeout: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle("计时器超时")
            // 从当前时间开始计时
            .setContentText(timeout)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
//            .setFullScreenIntent(fullScreenPI, true)
//            .setContentIntent(fullScreenPI)
            .setOngoing(true)
            .addAction(0, "关闭", dismissPI)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * 获取 Notification 构建器
     * 返回类型：NotificationCompat.Builder
     */
    fun getNotificationBuilder(
        context: Context,
        alarm: AlarmEntity,
        fullScreenPI: PendingIntent,
        dismissPI: PendingIntent,
        snoozePI: PendingIntent,
        channelId: String
    ): NotificationCompat.Builder {
        // 获取格式化时间
        val formattedTime = StringUtils.formattedTime(alarm.nextTriggerTime)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(alarm.label)
            .setContentText("${alarm.period} $formattedTime")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPI, true)
            .setContentIntent(fullScreenPI)
            .setOngoing(true)
            .addAction(0, "关闭", dismissPI)
            .addAction(0, "稍后提醒", snoozePI)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * 专门用于稍后提醒的相关后台通知
     */
    fun getSnoozeBuilder(
        context: Context,
        alarm: AlarmEntity,
        dismissPI: PendingIntent,
        channelId: String
    ): NotificationCompat.Builder {
        // 直接使用已有的最新时间戳转换
        val formattedTime = StringUtils.formattedTime(alarm.nextTriggerTime)

        // 设置主页面跳转，用户点击后可直接跳转闹钟界面
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPI = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle("${alarm.label} (稍后提醒)")
            .setContentText("${alarm.period}$formattedTime 再响 - ${alarm.label}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentPI)
            .setOngoing(true)
            .addAction(0, "关闭", dismissPI)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }


    /**
     * 获取全屏跳转的 PendingIntent
     */
    fun getFullScreenIntent(context: Context, alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, RingActivity::class.java).apply {
            putExtra("ALARM_OBJ", alarm)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        // 全屏 Intent 需要使用 FLAG_UPDATE_CURRENT 或 FLAG_IMMUTABLE
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, alarm.id, intent, flags)
    }

    /**
     * 获取广播动作的 PendingIntent (关闭/稍后提醒)
     * @param action 动作字符串
     * @param requestCodeOffset 偏移量
     */
    fun getBroadcastIntent(
        context: Context,
        alarm: AlarmEntity,
        action: String,
        requestCodeOffset: Int
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra("ALARM_OBJ", alarm)
        }
        return PendingIntent.getBroadcast(
            context, alarm.id + requestCodeOffset, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}