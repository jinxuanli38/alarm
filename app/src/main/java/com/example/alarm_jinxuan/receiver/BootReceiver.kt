package com.example.alarm_jinxuan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm_jinxuan.repository.AlarmRepository
import com.example.alarm_jinxuan.utils.AlarmManagerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 增加对多种开机广播的判断
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.e("BootReceiver", "收到开机广播，开始准备恢复闹钟...")

            // 使用 goAsync 告诉系统：我还有点后台活没干完，先别杀我
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. 初始化数据库/Repo
                    AlarmRepository.init(context)

                    // 2. 获取所有【已开启】的闹钟（注意：这里要用非 Flow 的单次查询）
                    val enabledAlarms = AlarmRepository.getAllAlarms()

                    Log.e("BootReceiver", "数据库加载成功，准备恢复 ${enabledAlarms?.size} 个闹钟")

                    // 3. 循环重新设定
                    enabledAlarms?.forEach { alarm ->
                        // 也需要计算下一次响铃的时间戳
                        val nextTriggerTime =
                            AlarmManagerUtils.calculateNextTriggerTime(alarm)
                        AlarmManagerUtils.setAlarm(context, alarm,nextTriggerTime)
                    }

                } catch (e: Exception) {
                    Log.e("BootReceiver", "恢复过程出错: ${e.message}")
                } finally {
                    // 必须调用 finish，否则会造成内存泄漏或系统警告
                    pendingResult.finish()
                }
            }
        }
    }
}