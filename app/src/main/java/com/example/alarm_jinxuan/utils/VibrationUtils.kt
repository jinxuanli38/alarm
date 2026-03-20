package com.example.alarm_jinxuan.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.*

object VibrationUtils {

    /**
     * 核心震动方法
     * @param context 上下文
     * @param pattern 震动频率数组 [等待, 震动, 等待, 震动...]
     * @param repeat 是否循环。-1 不循环，0 从数组第 0 位开始循环
     */
    @SuppressLint("ObsoleteSdkInt")
    fun vibrate(context: Context, pattern: LongArray, repeat: Int = -1) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // 每次震动前先停止上一次，防止重叠导致的“麻手”感
        vibrator.cancel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, repeat)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, repeat)
        }
    }

    /**
     * 停止震动（退出页面时调用）
     */
    fun stop(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.cancel()
    }

}