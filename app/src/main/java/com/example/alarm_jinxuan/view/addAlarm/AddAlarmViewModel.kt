package com.example.alarm_jinxuan.view.addAlarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm_jinxuan.dao.AppDatabase
import com.example.alarm_jinxuan.model.AlarmEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

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

        val triggerTime = calculateNextTriggerTime(nextAlarmEntity)
        val (h, m) = getRemainingTime(triggerTime)

        "$h 小时 $m 分钟后响铃"
    }

    /**
     * 闹钟响铃时间
     */
    fun calculateNextTriggerTime(alarm: AlarmEntity): Long {
        val now = Calendar.getInstance()

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour24)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 如果已经过了 → 加一天
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis
    }

    /**
     * 和当前时间（now）计算还有多久
     */
    fun getRemainingTime(triggerTime: Long): Pair<Int, Int> {
        val diff = triggerTime - System.currentTimeMillis()

        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val remainMinutes = minutes % 60

        return Pair(hours.toInt(), remainMinutes.toInt())
    }

    /**
     * 插入闹钟到数据库
     */
    suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return withContext(Dispatchers.IO) {
            alarmDao.insertAlarm(alarm)
        }
    }

    /**
     * 修改闹钟的开关状态
     */
    fun updateAlarmEnabled(alarmId: Int,enabled: Boolean) {
        viewModelScope.launch {
            alarmDao.updateEnabledStatus(alarmId,enabled)
        }
    }
}