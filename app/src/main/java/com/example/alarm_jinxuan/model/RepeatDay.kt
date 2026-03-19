package com.example.alarm_jinxuan.model

data class RepeatDay(
    val id: Int,      // 唯一标识，周日为 0，周一为 1...
    val name: String,
    var isChecked: Boolean = false
)