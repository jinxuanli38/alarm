package com.example.alarm_jinxuan.utils

object AllCodeConstants {
    // ==================== Channel ID (通知渠道 ID) ====================
    const val ALARM_CHANNEL_ID = "ALARM_channelId"
    const val TIMER_CHANNEL_ID = "ALARM_TIMER"
    const val STOPWATCH_CHANNEL_ID = "stopwatch_channel"

    // ==================== Notification ID (通知 ID) ====================
    // 闹钟通知 ID (使用 alarm.id，这里只定义范围)
    const val ALARM_NOTIFICATION_MIN_ID = 1000

    // 计时器通知 ID
    const val TIMER_NOTIFICATION_ID = 1

    // 秒表通知 ID
    const val STOPWATCH_NOTIFICATION_ID = 1
    const val STOPWATCH_UPDATE_NOTIFICATION_ID = 3999

    // ==================== Request Code (PendingIntent 请求码) ====================
    // 闹钟相关
    const val ALARM_DISMISS_REQUEST_CODE = 1000
    const val ALARM_SNOOZE_REQUEST_CODE = 2000
    const val ALARM_FULLSCREEN_REQUEST_CODE = 999

    // 计时器相关
    const val TIMER_PAUSE_REQUEST_CODE = 0
    const val TIMER_STOP_REQUEST_CODE = 1

    // 秒表相关
    const val STOPWATCH_ACTION_STOP_REQUEST_CODE = 1
    const val STOPWATCH_ACTION_QUICK_NOTE_REQUEST_CODE = 2
    const val STOPWATCH_ACTION_RESET_REQUEST_CODE = 3
    const val STOPWATCH_ACTION_START_REQUEST_CODE = 4
}