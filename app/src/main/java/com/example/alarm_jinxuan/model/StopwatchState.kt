package com.example.alarm_jinxuan.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stopwatch_state")
data class StopwatchState(
    @PrimaryKey val id: Int = 0,
    val startTime: Long = 0L,      // 总表开始时的绝对时间 (SystemClock.elapsedRealtime())
    val lapStartTime: Long = 0L,   // 上一次点击“计次”时的绝对时间
    val baseTime: Long = 0L,       // 总表在暂停前已经跑了多久
    val isRunning: Boolean = false // 是否是暂停状态
)