package com.example.alarm_jinxuan.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object StopWatchManager {

    // --- 1. 指令通信 (Service -> ViewModel) ---
    private val _commandFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val commandFlow = _commandFlow.asSharedFlow()

    fun sendCommand(action: String) {
        _commandFlow.tryEmit(action)
    }

    // --- 2. 状态同步 (ViewModel -> Service) ---
    // 定义一个数据类，封装通知所需的所有“快照”
    data class NotificationState(
        val isRunning: Boolean = false,
        val baseTime: Long = 0L,        // 传给 setWhen 的那个计算后的开机毫秒数
        val lapText: String = "暂无计次", // 比如 "计次 5"
        val formattedTime: String = "00:00.00" // 暂停时显示的静态时间
    )

    private val _notificationState = MutableStateFlow(NotificationState())
    val notificationState = _notificationState.asStateFlow()

    /**
     * 由 ViewModel 调用，每当状态变化时，把最新的“快照”推送到这里
     */
    fun updateNotification(
        isRunning: Boolean,
        baseTime: Long,
        lapText: String,
        formattedTime: String
    ) {
        _notificationState.value = NotificationState(isRunning, baseTime, lapText, formattedTime)
    }

}