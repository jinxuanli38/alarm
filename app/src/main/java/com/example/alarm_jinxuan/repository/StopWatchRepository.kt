package com.example.alarm_jinxuan.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object StopWatchRepository {
    // 秒表运行状态
    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    // 秒表运行时间（纳秒）
    private val _elapsedNanos = MutableStateFlow(0L)
    val elapsedNanos = _elapsedNanos.asStateFlow()

    // 快计间隔时间（纳秒）
    private val _intervalNanos = MutableStateFlow(0L)
    val intervalNanos = _intervalNanos.asStateFlow()

    /**
     * 设置运行状态
     */
    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }

    /**
     * 设置运行时间
     */
    fun setElapsedNanos(nanos: Long) {
        _elapsedNanos.value = nanos
    }

    /**
     * 增加运行时间
     */
    fun addElapsedNanos(nanos: Long) {
        _elapsedNanos.value += nanos
    }

    /**
     * 设置间隔时间
     */
    fun setIntervalNanos(nanos: Long) {
        _intervalNanos.value = nanos
    }

    /**
     * 增加间隔时间
     */
    fun addIntervalNanos(nanos: Long) {
        _intervalNanos.value += nanos
    }

    /**
     * 重置所有数据
     */
    fun reset() {
        _isRunning.value = false
        _elapsedNanos.value = 0L
        _intervalNanos.value = 0L
    }
}