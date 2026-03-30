package com.example.alarm_jinxuan.view.stopWatch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.alarm_jinxuan.dao.AppDatabase
import com.example.alarm_jinxuan.model.LapRecord
import com.example.alarm_jinxuan.model.StopwatchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StopWatchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val lapDao = db.lapDao()

    private val stopWatch = db.stopWatch()

    val laps = lapDao.getAllLaps().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sw = stopWatch.getStopwatchState()
        .stateIn(viewModelScope, SharingStarted.Eagerly, StopwatchState())

    // 秒表运行状态
    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    // 秒表运行时间
    private val _elapsedNanos = MutableStateFlow(0L)
    val elapsedNanos = _elapsedNanos.asStateFlow()

    // 快计间隔时间
    private val _interval = MutableStateFlow(0L)
    val interval = _interval.asStateFlow()

    // 快记间隔实现显示
    val firstInterval = MutableLiveData(false)

    // 格式化间隔时间
    val formattedInterval = _interval.map { nanos ->
        formatNanosToTime(nanos)
    }.asLiveData()

    // 格式化计时时间
    val formattedTime = elapsedNanos.map { nanos ->
        formatNanosToTime(nanos)
    }.asLiveData()

    // 开始计时
    fun start() {
        _isRunning.value = true
    }

    // 暂停计时
    fun stop() {
        _isRunning.value = false
    }

    // 开始快记
    fun addLap() {
        viewModelScope.launch {
            val currentTime = _elapsedNanos.value
            val diff = _interval.value

            val newLap = LapRecord(
                id = laps.value.size + 1,
                lapTimeNanos = currentTime,
                durationNanos = diff
            )

            lapDao.insert(newLap)
        }
    }

    // 添加时间
    fun addNanos(nanos: Long) {
        if (_isRunning.value) {
            _elapsedNanos.value += nanos
            _interval.value += nanos
        }
    }

    // 秒表重置
    fun reset() {
        _isRunning.value = false
        _elapsedNanos.value = 0L
        _interval.value = 0L
        // 同时清除数据库时间
        viewModelScope.launch {
            stopWatch.resetState()
        }
    }

    // 秒表间隔时间清零
    fun intervalReset() {
        _interval.value = 0L
    }

    fun deleteLapRecord() {
        viewModelScope.launch {
            lapDao.deleteAll()
        }
    }

    private fun formatNanosToTime(nanos: Long): String {
        val totalMillis = nanos / 1_000_000
        val minutes = (totalMillis / 1000) / 60
        val seconds = (totalMillis / 1000) % 60
        val centiSeconds = (totalMillis % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, centiSeconds)
    }
}