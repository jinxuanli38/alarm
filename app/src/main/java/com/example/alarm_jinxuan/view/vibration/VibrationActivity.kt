package com.example.alarm_jinxuan.view.vibration

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.alarm_jinxuan.databinding.ActivityVibrationBinding
import com.example.alarm_jinxuan.model.AddAlarmClockManager
import com.example.alarm_jinxuan.model.AddAlarmClockManager.vibrationList

class VibrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVibrationBinding
    private lateinit var vibrationAdapter: VibrationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVibrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentSelectedId = AddAlarmClockManager.tempVibrationId

        vibrationList.forEach { option ->
            option.isSelected = (option.id == currentSelectedId)
        }

        vibrationAdapter = VibrationAdapter(vibrationList) { selectedOption ->
            AddAlarmClockManager.tempVibrationId = selectedOption.id
            AddAlarmClockManager.tempVibrationName = selectedOption.name

            if (selectedOption.id == 8 || selectedOption.name == "无振动") {
                return@VibrationAdapter
            }

            startVibrationPreview(selectedOption.pattern)
        }

        binding.vibrationList.apply {
            adapter = vibrationAdapter
        }

        binding.back.setOnClickListener {
            finish()
        }

    }

    private fun startVibrationPreview(pattern: LongArray) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // 先停止当前的震动（防止用户连续快速点击，导致震动重叠乱套）
        vibrator.cancel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建震动效果。第二个参数 -1 表示【不循环】，只震一遍。
            val effect = VibrationEffect.createWaveform(pattern, -1)
            vibrator.vibrate(effect)
        } else {
            // 老版本 API
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}