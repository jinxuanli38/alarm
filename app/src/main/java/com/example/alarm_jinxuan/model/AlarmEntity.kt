package com.example.alarm_jinxuan.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // 自动生成的唯一 ID

    val period: String,          // "上午" "下午" "中午" "晚上"
    val hour: Int,               // 12小时制 (01-12)
    val minute: Int,             // 分钟 (00-59)

    val hour24: Int,             // 24小时制的小时 (0-23)，存这个设置闹钟最稳
    val isEnabled: Boolean = true, // 闹钟开启/关闭状态

    val repeatText: String,      // "每天" / "只响一次" / "周一, 周五"
    val repeatData: String,      //  原始数据（如 "1,0,1,0,0,0,0"

    val ringtoneName: String,    // 铃声名
    val ringtoneFileName: String,// 铃声文件名（不存储资源Id）
    val vibrationName: String,   // 震动模式名
    val vibrationId: Int,        // 震动 ID

    val ringDuration: Int,       // 响铃时长

    val snoozeInterval: Int,     // 响铃间隔 (分钟)
    val snoozeCount: Int,        // 重复次数

    val label: String = "闹钟",   // 备注（闹钟名）
    val createTime: Long = System.currentTimeMillis() // 排序用
)