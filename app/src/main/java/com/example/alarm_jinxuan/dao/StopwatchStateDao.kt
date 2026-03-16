package com.example.alarm_jinxuan.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.alarm_jinxuan.model.StopwatchState
import kotlinx.coroutines.flow.Flow

@Dao
interface StopwatchStateDao {

    // --- 1. 秒表运行状态 (总表和分表的锚点) ---
    @Query("SELECT * FROM stopwatch_state WHERE id = 0")
    fun getStopwatchState(): Flow<StopwatchState?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateState(state: StopwatchState)

    @Query("UPDATE stopwatch_state SET startTime = 0, lapStartTime = 0, baseTime = 0, isRunning = 0 WHERE id = 0")
    suspend fun resetState()
}