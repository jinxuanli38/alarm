package com.example.alarm_jinxuan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.alarm_jinxuan.MainActivity
import com.example.alarm_jinxuan.R
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.receiver.AlarmReceiver
import com.example.alarm_jinxuan.repository.TimerRepository
import com.example.alarm_jinxuan.repository.TimerRepository.formatRemainingTime
import com.example.alarm_jinxuan.service.StopWatchService
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
     * 获取计时器的通知builder
     */
    fun getStopWatchBuilder(
        context: Context,
        channelId: String,
        state: StopWatchManager.NotificationState
    ): NotificationCompat.Builder {
        // 1. 准备 PendingIntents (逻辑同前)
        val toggleIntent =
            Intent(context, StopWatchService::class.java).apply { action = "ACTION_TOGGLE" }
        val togglePending = PendingIntent.getService(
            context,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lapIntent =
            Intent(context, StopWatchService::class.java).apply { action = "ACTION_LAP" }
        val lapPending = PendingIntent.getService(
            context,
            2,
            lapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. 创建 MediaSession (MediaStyle 需要它来接管布局)
        val mediaSession = MediaSessionCompat(context, "StopWatchSession")

        // 3. 基础构建：设置左侧大图标和文本
        val builder = NotificationCompat.Builder(context, channelId)
            // 设置大图标（必须为bitmap格式）
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_stop_watch))
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(if (state.isRunning) "" else state.formattedTime) // 运行中由 Chronometer 接管，暂停显示静态时间
            .setContentText(state.lapText) // 第二行文本
            .setOngoing(state.isRunning)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            // 点击通知回 MainActivity
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    3999,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )

        // 4. 关键：应用 MediaStyle 并配置按钮位置
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                // 主要是为了下面的两个addAction做准备（即非常重要，按钮不要折叠）
                .setShowActionsInCompactView(0, 1)
        )

        // 5. 动态添加右侧的 Actions (顺序很重要)
        if (state.isRunning) {
            // --- 运行状态：显示 [暂停(蓝色圆形)] 和 [计次(灰色圆形)] ---

            // 我们需要手动用代码去 Tint (着色) 图标，实现系统原生的蓝色圆形按钮效果
            // Android 系统会自动把这两个 Action 渲染成圆形

            // 按钮 0: 暂停 (着色为蓝色)
            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_pause, "暂停", togglePending
                ).build()
            )

            // 按钮 1: 计次 (着色为灰色)
            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_flag, "计次", lapPending
                ).build()
            )

            // 核心 3：运行中，让 ContentTitle 位置显示 Chronometer
            builder.setUsesChronometer(true)
            builder.setWhen(state.baseTime)

        } else {
            // --- 暂停状态：显示 [开始(蓝色圆形)] 和 [重置(灰色圆形)] ---

            // 按钮 0: 开始 (蓝色)
            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_begin, "开始", togglePending
                ).build()
            )

            // 按钮 1: 重置 (灰色)
            val resetIntent =
                Intent(context, StopWatchService::class.java).apply { action = "ACTION_RESET" }
            val resetPending = PendingIntent.getService(
                context,
                3,
                resetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_reopen, "重置", resetPending
                ).build()
            )

            // 暂停时关闭系统计时器，setContentTitle 里的静态时间就会显示出来
            builder.setUsesChronometer(false)
        }

        return builder
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