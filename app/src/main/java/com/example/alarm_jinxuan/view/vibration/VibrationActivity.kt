package com.example.alarm_jinxuan.view.vibration

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.alarm_jinxuan.databinding.ActivityVibrationBinding
import com.example.alarm_jinxuan.model.AddAlarmClockManager
import com.example.alarm_jinxuan.model.AddAlarmClockManager.vibrationList
import com.example.alarm_jinxuan.utils.VibrationUtils

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

            VibrationUtils.vibrate(this, selectedOption.pattern)
        }

        binding.vibrationList.apply {
            adapter = vibrationAdapter
        }

        binding.back.setOnClickListener {
            VibrationUtils.stop(this)
            finish()
        }

    }

}