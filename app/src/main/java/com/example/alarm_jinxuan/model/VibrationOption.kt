package com.example.alarm_jinxuan.model

data class VibrationOption(
    val id: Int,
    val name: String,
    val pattern: LongArray, // 震动节奏：[等待, 震动, 等待, 震动...]
    var isSelected: Boolean = false
)