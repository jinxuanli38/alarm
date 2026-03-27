package com.example.alarm_jinxuan.view.addAlarm

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.alarm_jinxuan.dao.AppDatabase
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.service.AlarmService
import com.example.alarm_jinxuan.utils.AlarmManagerUtils
import com.example.alarm_jinxuan.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddAlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val alarmDao = AppDatabase.getDatabase(application).alarm()

    // 对闹钟数据库监听数据
    val allAlarms: StateFlow<List<AlarmEntity>> = alarmDao.getAllAlarms().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // 监听已开启的闹钟
    val allEnabledAlarms: StateFlow<List<AlarmEntity>> = alarmDao.getEnabledAlarms().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val timeTickerFlow = flow {
        while (true) {
            // 1. 立即发射一次，让 UI 初始化
            emit(System.currentTimeMillis())

            // 2. 计算当前距离“下一分钟整点”还有多少毫秒
            val currentTime = System.currentTimeMillis()
            // (当前毫秒数 % 60000) 得到的是这一分钟里已经过去的毫秒
            val millisPassedInMinute = currentTime % 60_000
            val millisUntilNextMinute = 60_000 - millisPassedInMinute

            // 3. 动态延迟，精准卡点
            // 建议加上一个小偏移量（如 100ms），防止系统调度微差导致卡在 59.999 秒
            delay(millisUntilNextMinute + 100)
        }
    }

    val alarmCountdownFlow = combine(allEnabledAlarms, timeTickerFlow) { alarms, _ ->
        val nextAlarmEntity = alarms.firstOrNull() ?: return@combine "所有闹钟已关闭"


        val triggerTime = nextAlarmEntity.nextTriggerTime
        Log.e("flow相关数据",triggerTime.toString())
        val (d,h, m) = AlarmManagerUtils.getRemainingTime(triggerTime)

        StringUtils.formatRemainingTime(d,h,m)
    }

    /**
     * 插入（修改）闹钟到数据库
     */
    suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return withContext(Dispatchers.IO) {
            alarmDao.insertAlarm(alarm)
        }
    }

    /**
     * 修改闹钟的开关状态
     */
    fun updateAlarmEnabled(alarm: AlarmEntity,enabled: Boolean) {
        // 同时也要修改闹钟的重复响应次数以及相应的时间戳
        val nextTriggerTime = AlarmManagerUtils.calculateNextTriggerTime(alarm)

        viewModelScope.launch {
            // 关闭闹钟通知
            val nm = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(alarm.id)
            // 关闭service服务
            val intent = Intent(application, AlarmService::class.java)
            application.stopService(intent)

            // 在调用这个方法的地方打印
            Log.e("修改开关状态", "准备写入时间戳: $nextTriggerTime")
            alarmDao.updateEnabledStatus(alarm.id,enabled,nextTriggerTime)
            // 同时不要忘记去修改闹钟状态
            if (!enabled) {
                AlarmManagerUtils.cancelAlarm(application,alarm.id)
            } else {
                val triggerTime = AlarmManagerUtils.calculateNextTriggerTime(alarm)
                AlarmManagerUtils.setAlarm(application,alarm,triggerTime)
            }
        }
    }

    /**
     * 查询对应闹钟数据
     */
    suspend fun selectAlarmData(alarmId: Int): AlarmEntity? {
        return withContext(Dispatchers.IO){
            alarmDao.getAlarmById(alarmId)
        }
    }

    /**
     * 删除对应闹钟数据
     */
    suspend fun delete(alarmId: Int): Int {
        return withContext(Dispatchers.IO) {
            alarmDao.deleteAlarmById(alarmId)
        }
    }
}