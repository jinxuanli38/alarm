package com.example.alarm_jinxuan.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.alarm_jinxuan.R
import com.example.alarm_jinxuan.receiver.TimerReceiver
import com.example.alarm_jinxuan.repository.TimerRepository
import com.example.alarm_jinxuan.utils.AlarmNotificationUtils.getTimerOutNotificationBuilder
import com.example.alarm_jinxuan.utils.AlarmNotificationUtils.getTimerStartNotification
import com.example.alarm_jinxuan.utils.MediaUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : LifecycleService() {
    private val channelId = "ALARM_TIMER"

    private var timerJob: Job? = null

    // 1. 获取 Manager
    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        // 2. Service 启动时，先确保渠道已经创建好
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            // 开始启动闹钟
            "ACTION_START" -> startAlarm()
            // 闹钟暂停
            "ACTION_PAUSE" -> refreshNotification()
            // 超时处理
            "ACTION_TIMEOUT" -> startAlarmTimeOut()
            // 重置（通知栏关闭逻辑）
            "ACTION_STOP_SERVICE" -> {
                timerJob?.cancel()
                // 同时停止响铃
                MediaUtils.stop(this)
                // 清理所有通知
                notificationManager.cancel(0)
                stopSelf()
            } // 接收来自 Receiver 的自杀指令
        }

        observeRepository()
        // 响铃
//        MediaUtils.startRingtonePreview(R.raw.alarm_timer_beep, this)
        return START_NOT_STICKY
    }

    // 监听运行状态
    private fun observeRepository() {
        lifecycleScope.launch {
            // 持续观察运行状态的变化
            TimerRepository.isRunning.collect {
                refreshNotification()
            }
        }
    }

    /**
     * 创建通知通道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "计时器闹钟",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 启动闹钟
     */
    private fun startAlarm() {
        val actionIntent = Intent(this, TimerReceiver::class.java).apply { action = "ACTION_PAUSE" }

        val actionPI = PendingIntent.getBroadcast(
            this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionTitle = "暂停"

        // 重新构建并推送到通知栏
        val builder = getTimerStartNotification(this, actionPI, actionTitle, channelId)
        // 创建前台服务
        startForeground(1, builder.build())

        updateTimerJob()
    }

    private fun updateTimerJob() {
        timerJob?.cancel()

        timerJob = lifecycleScope.launch {
            var timeout = -1L
            var ring = true
            while (TimerRepository.isRunning.value) {
                refreshNotification()
                // 时间超时
                if (TimerRepository.remainingNanos <= 0) {
                    timeout++
                    val formatTimeOut = TimerRepository.formatTimeOut(timeout)

                    // 更改通知
                    stopTimer(formatTimeOut)

                    // 同时开始响铃（防止重复响铃）
                    if (ring) {
                        MediaUtils.startRingtonePreview(R.raw.alarm_timer_beep,this@TimerService)
                        ring = false
                    }
                }
                delay(1000)
            }
        }
    }

    // 在 Service 内部更新通知的方法
    private fun refreshNotification() {
        val isRunning = TimerRepository.isRunning.value

        // 根据状态准备不同的 Intent 和 文字
        val actionIntent = if (isRunning) {
            // 如果正在运行，按钮应该是“暂停”
            Intent(this, TimerReceiver::class.java).apply { action = "ACTION_PAUSE" }
        } else {
            // 如果已经暂停，按钮应该是“开始”
            Intent(this, TimerReceiver::class.java).apply { action = "ACTION_START" }
        }

        val actionPI = PendingIntent.getBroadcast(
            this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionTitle = if (isRunning) "暂停" else "开始"

        // 重新构建并推送到通知栏
        val builder = getTimerStartNotification(this, actionPI, actionTitle, channelId)
        // 销毁旧通知

        notificationManager.notify(1, builder.build())
    }

    private fun stopTimer(timeout: String) {
        // 更改通知栏
        val actionIntent = Intent(this, TimerReceiver::class.java).apply {
            action = "ACTION_STOP"
        }
        // 设置关闭广播
        val actionPI = PendingIntent.getBroadcast(
            this, 1, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 获取构造器
        val builder = getTimerOutNotificationBuilder(this, actionPI, channelId, timeout)
        notificationManager.notify(1,builder.build())
    }

    private fun startAlarmTimeOut() {

    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaUtils.stop(this)
        stopSelf()
    }

}