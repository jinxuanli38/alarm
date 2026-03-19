package com.example.alarm_jinxuan.model

object AddAlarmClockManager {

    // 默认 0 表示“标准”，或者你自己定义的默认 ID
    var tempVibrationId: Int = 0

    var tempVibrationName: String = "标准 (默认)"

    // 铃声Id
    var tempRingtoneId: Int = 0

    // 铃声昵称
    var tempRingtoneName: String = "Morning Light"

    val vibrationList = listOf(
        VibrationOption(0, "标准 (默认)", longArrayOf(0, 500, 250, 500), true),
        VibrationOption(1, "鸣笛", longArrayOf(0, 1500)),
        VibrationOption(2, "闪烁", longArrayOf(0, 250)),
        VibrationOption(3, "滴答", longArrayOf(0, 250, 250, 250)),
        VibrationOption(4, "舞步", longArrayOf(0, 250, 250, 50, 350, 50)),
        VibrationOption(5, "敲门", longArrayOf(0, 100, 100, 100, 100, 100)),
        VibrationOption(
            6,
            "啄木鸟",
            longArrayOf(0, 50, 150, 50, 150, 50, 150, 50, 150, 50, 150, 50, 150, 50, 150, 50, 150)
        ),
        VibrationOption(
            7,
            "紧急",
            longArrayOf(
                0,
                150,
                150,
                150,
                150,
                150,
                350,
                500,
                150,
                500,
                150,
                500,
                350,
                150,
                150,
                150,
                150,
                150,
                150
            )
        ),
        VibrationOption(8, "无振动", longArrayOf(0, 0), false)
    )

    // 初始化（编辑页面时调用）
    fun init(id: Int) {
        tempVibrationId = id
    }

    // 重置（退出编辑/新建页面时调用）
    fun clear() {
        tempVibrationId = 0
        tempVibrationName = "标准 (默认)"
    }
}