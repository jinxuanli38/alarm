package com.example.alarm_jinxuan.dao

import androidx.room.*
import com.example.alarm_jinxuan.model.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    // 插入新闹钟
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    // 更新闹钟（比如在主页切换开关状态）
    @Query("UPDATE alarms SET isEnabled = :enabled WHERE id = :alarmId")
    suspend fun updateEnabledStatus(alarmId: Int, enabled: Boolean)

    // 删除闹钟
    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    // 查询所有闹钟（按时间先后排序）
    @Query("SELECT * FROM alarms ORDER BY hour24 ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    // 只查询已开启的闹钟
    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour24 ASC, minute ASC")
    fun getEnabledAlarms(): Flow<List<AlarmEntity>>

    // 根据 ID 查询单个闹钟（用于编辑页面回显）
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Int): AlarmEntity?
}