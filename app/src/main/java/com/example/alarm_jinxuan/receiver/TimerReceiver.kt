package com.example.alarm_jinxuan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm_jinxuan.repository.TimerRepository
import com.example.alarm_jinxuan.service.TimerService
import com.example.alarm_jinxuan.utils.MediaUtils

class TimerReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // 先获取action
        val action = intent?.action

        // 2. 根据 action 分发任务
        when (action) {
            "ACTION_START" -> {
                // 检查是否是恢复操作（已经设置过总时间）
                val serviceAction = if (TimerRepository.totalNanos > 0L) {
                    "ACTION_RESUME"  // 恢复计时
                } else {
                    "ACTION_START"   // 首次启动
                }
                // 修改仓库状态为 运行
                TimerRepository.setRunning(true)
                // 发送正确的 action 到 Service
                val serviceIntent = Intent(context, TimerService::class.java).apply {
                    this.action = serviceAction
                }
                context?.startService(serviceIntent)
            }
            "ACTION_PAUSE" -> {
                // 修改仓库状态为 暂停
                TimerRepository.setRunning(false)
                // 通知 Service 刷新通知
                val serviceIntent = Intent(context, TimerService::class.java).apply {
                    this.action = "ACTION_PAUSE"
                }
                context?.startService(serviceIntent)
            }
            "ACTION_STOP" -> {
                // 停止逻辑
                TimerRepository.setRunning(false)
                // 如果需要，可以发送指令让 Service 自杀
                val stopIntent = Intent(context, TimerService::class.java).apply {
                    this.action = "ACTION_STOP_SERVICE"
                }
                context?.startService(stopIntent)
            }
        }
    }

}