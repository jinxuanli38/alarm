package com.example.alarm_jinxuan.model

data class DurationOption(
    val id: Int,
    val minute: Int,
    val label: String,
    var isSelected: Boolean = false
)