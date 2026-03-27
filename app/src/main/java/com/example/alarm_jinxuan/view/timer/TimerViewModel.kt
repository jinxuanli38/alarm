package com.example.alarm_jinxuan.view.timer

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.alarm_jinxuan.repository.TimerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    // 总纳秒数（用于计算进度条比例）
    private val _totalSeconds = MutableStateFlow(0L)
    val totalSeconds = _totalSeconds.asStateFlow()

    // 当前剩余纳秒数
    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    // 是否正在计时（用于切换 UI 状态）
    val isRunning = TimerRepository.isRunning

    // 当前是否可以开始计时了
    private val _CanTime = MutableStateFlow(false)
    val canTime = _CanTime.asStateFlow()

    private val _pickerHour = MutableStateFlow(0)
    private val _pickerMin = MutableStateFlow(0)
    private val _pickerSec = MutableStateFlow(0)

    fun updatePickerValues(h: Int, m: Int, s: Int) {
        _pickerHour.value = h
        _pickerMin.value = m
        _pickerSec.value = s
        _CanTime.value = (h + m + s) > 0
    }

    /**
     * 用于开始和暂停
     */
    fun toggle(): Boolean {
        // 1. 如果当前剩余时间为 0，说明是“第一次启动”或“重新开始”
        // 此时才需要从滚轮（Picker）获取新数据
        if (_remainingSeconds.value <= 0L) {
            val total = (_pickerHour.value * 3600L) + (_pickerMin.value * 60L) + _pickerSec.value
            if (total <= 0L) return false

            val totalNs = total * 1_000_000_000L
            // 同步viewmodel和repository
            _totalSeconds.value = totalNs
            _remainingSeconds.value = totalNs
            TimerRepository.totalNanos = totalNs
            TimerRepository.remainingNanos = totalNs
        }

        // 同步仓库运行状态
        TimerRepository.setRunning(!TimerRepository.isRunning.value)

        return true
    }

    /**
     * 重置方法：彻底归零并同步仓库
     */
    fun reset() {
        // 1. 停止运行状态
        TimerRepository.setRunning(false)

        // 2. 仓库数据归零（Service 看到这里归零了，就会停止通知栏更新）
        TimerRepository.totalNanos = 0L
        TimerRepository.remainingNanos = 0L

        // 3. ViewModel 本地状态归零（驱动 UI 变回初始样子）
        _remainingSeconds.value = 0L
        _totalSeconds.value = 0L

        // 4. 滚轮状态重置
        _pickerHour.value = 0
        _pickerMin.value = 0
        _pickerSec.value = 0
        _CanTime.value = false

    }

    /**
     * 专门用于渲染UI的
     */
    fun reduceNanos(nanos: Long) {
        if (TimerRepository.isRunning.value) {
            // 最小值为0，不可为负数
            val newValue = (_remainingSeconds.value - nanos).coerceAtLeast(0L)
            _remainingSeconds.value = newValue

            // 每一帧减完，都要塞进仓库，这样 Service 随时去仓库拿都是最新的
            TimerRepository.remainingNanos = newValue

//            if (newValue <= 0L) {
//                TimerRepository.setRunning(false)
//            }
        }
    }

    // 格式化时间字符串
    @SuppressLint("DefaultLocale")
    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}