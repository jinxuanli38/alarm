package com.example.alarm_jinxuan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.repository.AlarmRepository
import com.example.alarm_jinxuan.service.AlarmService
import com.example.alarm_jinxuan.utils.AlarmManagerUtils

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 先获取对应的action
        val action = intent.action
        // 获取 alarmManager 传递的数据
        val alarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("ALARM_OBJ", AlarmEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("ALARM_OBJ")
        }

        // 对action进行判断执行对应的相关逻辑
        when (action) {
            "ACTION_DISMISS" -> {
                // 关闭闹钟停止服务
                val stopIntent = Intent(context, AlarmService::class.java)
                context.stopService(stopIntent)
                // 这里关闭闹钟后，还需要根据闹钟的重复时间来设置下一个alarmManager
                if (alarm?.repeatText == "不重复") {
                    AlarmRepository.dismissAlarm(alarm, context)
                } else {
                    // 那就说明为重复，需要创建alarmManager设置下一次的闹钟
                    alarm?.let {
                        setAlarm(context, alarm)
                    }
                }
            }

            "ACTION_SNOOZE" -> {
                // 停止当前闹钟服务
                context.stopService(Intent(context, AlarmService::class.java))
                // 这里调用小睡模式
                if (alarm != null) {
                    AlarmManagerUtils.snoozeAlarm(context, alarm)
                }
            }

            else -> {
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra("ALARM_OBJ", alarm)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }

    /**
     * 关闭闹铃后还要设置下一次的alarmManager
     */
    private fun setAlarm(context: Context, alarm: AlarmEntity) {
        val triggerTime = AlarmManagerUtils.calculateNextTriggerTime(alarm)
        // 数据库也要更新它的下一次响铃时间
        AlarmRepository.updateAlarmNextTriggerTime(alarm, triggerTime)
        // 设置下一次的闹铃
        AlarmManagerUtils.setAlarm(context, alarm, triggerTime)
    }

}